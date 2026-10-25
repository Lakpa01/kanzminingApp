
package com.example.kanzminingapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.launch

private val ExtraGold = Color(0xFFE5B94F)
private val ExtraDark = Color(0xFF11130F)
private val ExtraWhite = Color(0xFFF5F5F0)
private val ExtraMuted = Color(0xFFB8B8AE)
private val ExtraCard = Color(0xFF1C1F19)

data class KanzNearbyPlace(
    val id: String,
    val name: String,
    val address: String,
    val location: LatLng
)

/* ---------------- SHARED PAGE UI ---------------- */

@Composable
private fun ExtraPageFrame(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ExtraDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun ExtraPageHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = ExtraGold)
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = ExtraGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(subtitle, color = ExtraMuted, fontSize = 12.sp)
        }
    }

    Spacer(Modifier.height(18.dp))
}

@Composable
private fun ExtraSectionTitle(title: String) {
    Text(
        title,
        color = ExtraWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun extraFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = ExtraWhite,
        unfocusedTextColor = ExtraWhite,
        focusedLabelColor = ExtraGold,
        unfocusedLabelColor = ExtraMuted,
        focusedBorderColor = ExtraGold,
        unfocusedBorderColor = ExtraMuted,
        cursorColor = ExtraGold
    )

@Composable
private fun ExtraInfoCard(
    title: String,
    subtitle: String,
    tag: String,
    description: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExtraCard
        ),
        onClick = onClick ?: {}
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                tag.uppercase(),
                color = ExtraGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                title,
                color = ExtraWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(subtitle, color = ExtraMuted, fontSize = 12.sp)

            Spacer(Modifier.height(7.dp))

            Text(description, color = ExtraWhite, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ExtraGoldButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ExtraGold,
            contentColor = ExtraDark,
            disabledContainerColor = ExtraGold.copy(alpha = 0.4f),
            disabledContentColor = ExtraDark
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

/* ---------------- CURRENT LOCATION ---------------- */

@Suppress("DEPRECATION")
private fun getKanzCurrentLocation(
    context: Context,
    onLocation: (LatLng) -> Unit
) {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted && !coarseGranted) return

    try {
        val manager = context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

        var lastLocation: Location? = null

        for (provider in listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )) {
            try {
                if (manager.isProviderEnabled(provider)) {
                    val location =
                        manager.getLastKnownLocation(provider)

                    if (
                        location != null &&
                        (
                                lastLocation == null ||
                                        location.time > lastLocation.time
                                )
                    ) {
                        lastLocation = location
                    }
                }
            } catch (_: SecurityException) {
                // Try the next provider.
            }
        }

        if (lastLocation != null) {
            onLocation(
                LatLng(
                    lastLocation.latitude,
                    lastLocation.longitude
                )
            )
            return
        }

        val provider =
            manager.getBestProvider(Criteria(), true) ?: return

        manager.requestSingleUpdate(
            provider,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocation(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                }

                @Deprecated("Deprecated by Android")
                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) = Unit

                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) = Unit
            },
            Looper.getMainLooper()
        )
    } catch (_: Exception) {
        // Location may be temporarily unavailable.
    }
}

/* ---------------- FULL-SCREEN MINES MAP ---------------- */

@Suppress("DEPRECATION")
@Composable
fun KanzMinesPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val defaultPosition = LatLng(34.9858, 126.4817)

    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    var placesClient by remember {
        mutableStateOf<PlacesClient?>(null)
    }

    var selectedCategory by remember {
        mutableStateOf("Mining sites")
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var mineSites by remember {
        mutableStateOf<List<KanzNearbyPlace>>(emptyList())
    }

    var nearbyBusinesses by remember {
        mutableStateOf<List<KanzNearbyPlace>>(emptyList())
    }

    var selectedMine by remember {
        mutableStateOf<KanzNearbyPlace?>(null)
    }

    var selectedPlace by remember {
        mutableStateOf<KanzNearbyPlace?>(null)
    }

    var isSearching by remember {
        mutableStateOf(false)
    }

    var mapType by remember {
        mutableStateOf(MapType.NORMAL)
    }

    var statusMessage by remember {
        mutableStateOf("Finding your current location...")
    }

    var showSearchPanel by remember {
        mutableStateOf(true)
    }

    val categories = listOf(
        "Mining sites",
        "All nearby",
        "Mining suppliers",
        "Restaurants",
        "Cafes",
        "Shops",
        "Fuel"
    )

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            defaultPosition,
            8f
        )
    }

    val placeFields = remember {
        listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.LOCATION,
            Place.Field.FORMATTED_ADDRESS
        )
    }

    fun animateToPlace(
        location: LatLng,
        zoom: Float
    ) {
        scope.launch {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    zoom
                ),
                durationMs = 700
            )
        }
    }

    fun convertPlaces(
        results: List<Place>
    ): List<KanzNearbyPlace> {
        return results.mapNotNull { place ->
            val point = place.location
                ?: return@mapNotNull null

            KanzNearbyPlace(
                id = place.id
                    ?: "${point.latitude},${point.longitude}",
                name = place.displayName ?: "Unnamed place",
                address = place.formattedAddress
                    ?: "Address unavailable",
                location = point
            )
        }.distinctBy { it.id }
    }

    fun openDirections(place: KanzNearbyPlace) {
        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&destination=${place.location.latitude},${place.location.longitude}" +
                    "&travelmode=driving"
        )

        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri)
            )
        } catch (_: Exception) {
            statusMessage = "Could not open Google Maps."
        }
    }

    fun searchMineSites(center: LatLng) {
        val client = placesClient

        if (client == null) {
            statusMessage =
                "Google Places is not ready. Check your API setup."
            return
        }

        isSearching = true
        statusMessage = "Searching for mines and quarries..."

        try {
            val query = if (searchText.isNotBlank()) {
                searchText.trim()
            } else {
                "mines, gold mines and quarries"
            }

            val request = SearchByTextRequest
                .builder(query, placeFields)
                .setMaxResultCount(20)
                .setLocationBias(
                    CircularBounds.newInstance(
                        center,
                        25000.0
                    )
                )
                .build()

            client.searchByText(request)
                .addOnSuccessListener { response ->
                    mineSites = convertPlaces(response.places)
                    isSearching = false

                    statusMessage = if (mineSites.isEmpty()) {
                        "No mining sites listed in this area."
                    } else {
                        "Found ${mineSites.size} possible sites."
                    }
                }
                .addOnFailureListener { error ->
                    isSearching = false
                    statusMessage =
                        "Mine search failed: " +
                                (error.localizedMessage ?: "Check Places API.")
                }
        } catch (e: Exception) {
            isSearching = false
            statusMessage =
                "Could not search: " +
                        (e.localizedMessage ?: "Unknown error.")
        }
    }

    fun searchBusinesses(
        center: LatLng,
        category: String
    ) {
        val client = placesClient

        if (client == null) {
            statusMessage =
                "Google Places is not ready. Check your API setup."
            return
        }

        isSearching = true
        nearbyBusinesses = emptyList()
        statusMessage = "Searching nearby places..."

        try {
            if (
                searchText.isNotBlank() ||
                category == "Mining suppliers"
            ) {
                val query = if (searchText.isNotBlank()) {
                    searchText.trim()
                } else {
                    "mining equipment suppliers and mining supply stores"
                }

                val request = SearchByTextRequest
                    .builder(query, placeFields)
                    .setMaxResultCount(20)
                    .setLocationBias(
                        CircularBounds.newInstance(
                            center,
                            10000.0
                        )
                    )
                    .build()

                client.searchByText(request)
                    .addOnSuccessListener { response ->
                        nearbyBusinesses =
                            convertPlaces(response.places)
                        isSearching = false
                        statusMessage =
                            "Found ${nearbyBusinesses.size} matching places."
                    }
                    .addOnFailureListener { error ->
                        isSearching = false
                        statusMessage =
                            "Search failed: " +
                                    (error.localizedMessage ?: "Check Places API.")
                    }
            } else {
                val types = when (category) {
                    "Restaurants" -> listOf("restaurant")
                    "Cafes" -> listOf("cafe")
                    "Shops" -> listOf(
                        "store",
                        "convenience_store",
                        "hardware_store"
                    )
                    "Fuel" -> listOf("gas_station")
                    else -> listOf(
                        "restaurant",
                        "cafe",
                        "store",
                        "convenience_store",
                        "gas_station",
                        "hardware_store"
                    )
                }

                val request = SearchNearbyRequest
                    .builder(
                        CircularBounds.newInstance(
                            center,
                            10000.0
                        ),
                        placeFields
                    )
                    .setIncludedTypes(types)
                    .setMaxResultCount(20)
                    .build()

                client.searchNearby(request)
                    .addOnSuccessListener { response ->
                        nearbyBusinesses =
                            convertPlaces(response.places)
                        isSearching = false
                        statusMessage =
                            "Found ${nearbyBusinesses.size} nearby places."
                    }
                    .addOnFailureListener { error ->
                        isSearching = false
                        statusMessage =
                            "Nearby search failed: " +
                                    (error.localizedMessage ?: "Check Places API.")
                    }
            }
        } catch (e: Exception) {
            isSearching = false
            statusMessage =
                "Could not search: " +
                        (e.localizedMessage ?: "Unknown error.")
        }
    }

    fun refreshCurrentLocation() {
        if (!hasLocationPermission) {
            statusMessage =
                "Location access is disabled. Enable it in Android Settings."
            return
        }

        statusMessage = "Finding your location..."

        getKanzCurrentLocation(context) { location ->
            currentLocation = location
            animateToPlace(location, 14f)
            statusMessage = "Your location is ready."
            searchMineSites(location)
        }
    }

    LaunchedEffect(Unit) {
        try {
            val appInfo =
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )

            val apiKey = appInfo.metaData
                ?.getString("com.google.android.geo.API_KEY")
                .orEmpty()

            if (apiKey.isBlank()) {
                statusMessage =
                    "Google Maps API key is missing from the manifest."
            } else {
                if (!Places.isInitialized()) {
                    Places.initializeWithNewPlacesApiEnabled(
                        context.applicationContext,
                        apiKey
                    )
                }

                placesClient = Places.createClient(context)
            }
        } catch (e: Exception) {
            statusMessage =
                "Places setup failed: " +
                        (e.localizedMessage ?: "Check API configuration.")
        }

        if (hasLocationPermission) {
            refreshCurrentLocation()
        } else {
            statusMessage =
                "Location permission is off. Enable it in Android Settings."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExtraDark)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true,
                myLocationButtonEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = true
            )
        ) {
            mineSites.forEach { mine ->
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = mine.location
                    ),
                    title = mine.name,
                    snippet = mine.address,
                    onClick = {
                        selectedMine = mine
                        selectedPlace = mine
                        showSearchPanel = false
                        animateToPlace(mine.location, 15f)
                        true
                    }
                )
            }

            nearbyBusinesses.forEach { place ->
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = place.location
                    ),
                    title = place.name,
                    snippet = place.address,
                    onClick = {
                        selectedPlace = place
                        showSearchPanel = false
                        animateToPlace(place.location, 16f)
                        true
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ExtraDark.copy(alpha = 0.94f),
                shadowElevation = 5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 4.dp,
                            end = 10.dp,
                            top = 4.dp,
                            bottom = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Text(
                            "←",
                            color = ExtraGold,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "KANZ MINING MAP",
                            color = ExtraGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            if (currentLocation != null) {
                                "Current location ready"
                            } else {
                                "Finding your location..."
                            },
                            color = ExtraMuted,
                            fontSize = 11.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            mapType =
                                if (mapType == MapType.NORMAL) {
                                    MapType.SATELLITE
                                } else {
                                    MapType.NORMAL
                                }
                        }
                    ) {
                        Text(
                            if (mapType == MapType.NORMAL) "Satellite" else "Map",
                            color = ExtraGold
                        )
                    }

                    IconButton(
                        onClick = { refreshCurrentLocation() }
                    ) {
                        Text(
                            "◎",
                            color = ExtraGold,
                            fontSize = 25.sp
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ExtraDark.copy(alpha = 0.94f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category

                                val center =
                                    currentLocation ?: cameraState.position.target

                                if (category == "Mining sites") {
                                    searchMineSites(center)
                                } else {
                                    searchBusinesses(center, category)
                                }

                                showSearchPanel = true
                            },
                            label = {
                                Text(category, fontSize = 12.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExtraGold,
                                selectedLabelColor = ExtraDark,
                                labelColor = ExtraWhite
                            )
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .heightIn(max = 370.dp),
            shape = RoundedCornerShape(22.dp),
            color = ExtraDark.copy(alpha = 0.97f),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (selectedPlace != null) {
                                "Selected destination"
                            } else {
                                "Explore mining locations"
                            },
                            color = ExtraGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            statusMessage,
                            color = ExtraMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            showSearchPanel = !showSearchPanel
                        }
                    ) {
                        Text(
                            if (showSearchPanel) "Hide" else "Search",
                            color = ExtraGold
                        )
                    }
                }

                if (selectedPlace != null) {
                    val place = selectedPlace!!

                    Text(
                        place.name,
                        color = ExtraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        place.address,
                        color = ExtraMuted,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                animateToPlace(place.location, 15f)
                            }
                        ) {
                            Text("Show on map", color = ExtraGold)
                        }

                        Button(
                            modifier = Modifier.weight(1.3f),
                            onClick = { openDirections(place) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExtraGold,
                                contentColor = ExtraDark
                            )
                        ) {
                            Text(
                                "Start navigation →",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        color = ExtraMuted.copy(alpha = 0.25f)
                    )
                }

                if (showSearchPanel) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search places or mining sites") },
                        placeholder = {
                            Text("e.g. gold mine, quarry, supplier")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = extraFieldColors()
                    )

                    Button(
                        onClick = {
                            val center =
                                currentLocation ?: cameraState.position.target

                            if (selectedCategory == "Mining sites") {
                                searchMineSites(center)
                            } else {
                                searchBusinesses(center, selectedCategory)
                            }
                        },
                        enabled = !isSearching,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExtraGold,
                            contentColor = ExtraDark
                        )
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = ExtraDark
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Searching...")
                        } else {
                            Text(
                                "Search this area",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val results =
                        if (selectedCategory == "Mining sites") {
                            mineSites
                        } else {
                            nearbyBusinesses
                        }

                    results.take(8).forEach { place ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ExtraCard,
                            onClick = {
                                selectedPlace = place

                                if (selectedCategory == "Mining sites") {
                                    selectedMine = place
                                }

                                showSearchPanel = false
                                animateToPlace(place.location, 15f)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    place.name,
                                    color = ExtraWhite,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(3.dp))

                                Text(
                                    place.address,
                                    color = ExtraMuted,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )

                                Text(
                                    "Tap to select destination →",
                                    color = ExtraGold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- JOBS PAGE ---------------- */

@Composable
fun KanzJobsPage(onBack: () -> Unit) {
    ExtraPageFrame {
        ExtraPageHeader(
            title = "Mining Jobs",
            subtitle = "Explore work opportunities",
            onBack = onBack
        )

        Text(
            "Find work in the mining industry.",
            color = ExtraWhite,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(12.dp))

        ExtraInfoCard(
            title = "Mining equipment operator",
            subtitle = "Location and salary to be confirmed",
            tag = "Example listing",
            description = "An example job card, not a confirmed vacancy."
        )

        Spacer(Modifier.height(12.dp))

        ExtraInfoCard(
            title = "Mining site assistant",
            subtitle = "Location and salary to be confirmed",
            tag = "Example listing",
            description = "An example opportunity, not a confirmed vacancy."
        )
    }
}

/* ---------------- CHATS PAGE ---------------- */

@Composable
fun KanzChatsPage(onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    var selectedProduct by remember {
        mutableStateOf("General enquiry")
    }

    ExtraPageFrame {
        ExtraPageHeader(
            title = "Chats",
            subtitle = "Your Kanz messages",
            onBack = onBack
        )

        OutlinedTextField(
            value = selectedProduct,
            onValueChange = { selectedProduct = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Product or conversation") },
            colors = extraFieldColors()
        )

        Spacer(Modifier.height(12.dp))

        if (KanzChatStore.messages.isEmpty()) {
            Text(
                "No messages yet. Messages sent from product pages will appear here.",
                color = ExtraMuted,
                fontSize = 14.sp
            )
        } else {
            KanzChatStore.messages.forEach { message ->
                ExtraInfoCard(
                    title = message.productName,
                    subtitle = if (message.isFromUser) "You" else "Seller",
                    tag = "Message",
                    description = message.message
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        ExtraSectionTitle("Write a message")

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your message") },
            placeholder = { Text("Type a message...") },
            colors = extraFieldColors()
        )

        Spacer(Modifier.height(8.dp))

        ExtraGoldButton(
            text = "Send message",
            enabled = draft.isNotBlank() && selectedProduct.isNotBlank()
        ) {
            KanzChatStore.sendMessage(
                selectedProduct.trim(),
                draft.trim()
            )
            draft = ""
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Messages are currently stored locally in the app. They are not sent to another user or saved to a server.",
            color = ExtraMuted,
            fontSize = 12.sp
        )
    }
}

/* ---------------- PROFILE PAGE ---------------- */

@Composable
fun KanzProfilePage(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "kanz_profile_details",
            Context.MODE_PRIVATE
        )
    }

    var profileName by remember {
        mutableStateOf(
            preferences.getString("profile_name", "Kanz User")
                ?: "Kanz User"
        )
    }

    var profileEmail by remember {
        mutableStateOf(
            preferences.getString("profile_email", "") ?: ""
        )
    }

    var profilePhone by remember {
        mutableStateOf(
            preferences.getString("profile_phone", "") ?: ""
        )
    }

    var profileCountry by remember {
        mutableStateOf(
            preferences.getString("profile_country", "") ?: ""
        )
    }

    var profilePhotoUri by remember {
        mutableStateOf(
            preferences.getString("profile_photo_uri", null)
        )
    }

    var savedMessage by remember {
        mutableStateOf("")
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Continue if persistent permission is unavailable.
            }

            profilePhotoUri = uri.toString()

            preferences.edit()
                .putString("profile_photo_uri", uri.toString())
                .apply()

            savedMessage = "Profile photo updated."
        }
    }

    ExtraPageFrame {
        ExtraPageHeader(
            title = "My Profile",
            subtitle = "Your Kanz account",
            onBack = onBack
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = ExtraCard
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(108.dp)
                        .clickable {
                            photoPicker.launch(arrayOf("image/*"))
                        },
                    shape = CircleShape,
                    color = ExtraGold
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profilePhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = Uri.parse(profilePhotoUri),
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = profileName
                                    .trim()
                                    .firstOrNull()
                                    ?.uppercaseChar()
                                    ?.toString() ?: "K",
                                color = ExtraDark,
                                fontSize = 46.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        photoPicker.launch(arrayOf("image/*"))
                    }
                ) {
                    Text(
                        "📷 Change profile photo",
                        color = ExtraGold
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = profileName.ifBlank { "Kanz User" },
                    color = ExtraWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Gold mining marketplace",
                    color = ExtraMuted,
                    fontSize = 13.sp
                )

                Text(
                    "Tap your photo to change it",
                    color = ExtraMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        ExtraSectionTitle("Personal details")

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = profileName,
            onValueChange = {
                profileName = it
                savedMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Full name") },
            placeholder = { Text("Enter your full name") },
            shape = RoundedCornerShape(12.dp),
            colors = extraFieldColors()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = profileEmail,
            onValueChange = {
                profileEmail = it
                savedMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Email address") },
            placeholder = { Text("Enter your email") },
            shape = RoundedCornerShape(12.dp),
            colors = extraFieldColors()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = profilePhone,
            onValueChange = {
                profilePhone = it
                savedMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Phone number") },
            placeholder = { Text("Enter your phone number") },
            shape = RoundedCornerShape(12.dp),
            colors = extraFieldColors()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = profileCountry,
            onValueChange = {
                profileCountry = it
                savedMessage = ""
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Country") },
            placeholder = { Text("Enter your country") },
            shape = RoundedCornerShape(12.dp),
            colors = extraFieldColors()
        )

        Spacer(Modifier.height(18.dp))

        ExtraGoldButton(
            text = "Save profile changes"
        ) {
            preferences.edit()
                .putString("profile_name", profileName.trim())
                .putString("profile_email", profileEmail.trim())
                .putString("profile_phone", profilePhone.trim())
                .putString("profile_country", profileCountry.trim())
                .putString("profile_photo_uri", profilePhotoUri)
                .apply()

            savedMessage = "Your profile has been saved."
        }

        if (savedMessage.isNotBlank()) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = savedMessage,
                color = ExtraGold,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        ExtraSectionTitle("Your Kanz account")

        Spacer(Modifier.height(12.dp))

        ExtraInfoCard(
            title = "Account",
            subtitle = "Your personal information",
            tag = "Profile",
            description = "Update your details above and save your changes."
        )

        Spacer(Modifier.height(12.dp))

        ExtraInfoCard(
            title = "Marketplace",
            subtitle = "Products, jobs and mining sites",
            tag = "Explore",
            description = "Browse mining products, discover job listings, and explore mining locations."
        )

        Spacer(Modifier.height(24.dp))

        ExtraGoldButton(
            text = "Log out",
            onClick = onLogout
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Your profile details and photo are stored locally on this device. They are not automatically uploaded to a server.",
            color = ExtraMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(10.dp))
    }
}

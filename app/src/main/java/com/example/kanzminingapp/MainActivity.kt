
package com.example.kanzminingapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.tooling.preview.Preview
import com.example.kanzminingapp.ui.theme.KanzMiningAppTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

private val Gold = Color(0xFFE5B94F)
private val Background = Color(0xFF11130F)
private val WhiteText = Color(0xFFF5F5F0)
private val MutedText = Color(0xFFB8B8AE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KanzMiningAppTheme {
                KanzApp()
            }
        }
    }
}

@Composable
fun KanzApp() {
    val auth = remember { FirebaseAuth.getInstance() }

    var currentScreen by remember {
        mutableStateOf(
            if (auth.currentUser != null) "home" else "welcome"
        )
    }

    when (currentScreen) {
        "welcome" -> WelcomeScreen(
            onGetStarted = { currentScreen = "location" },
            onLogin = {
                currentScreen =
                    if (auth.currentUser != null) "home" else "login"
            }
        )

        "location" -> LocationScreen(
            onConfirm = { currentScreen = "consent" },
            onBack = { currentScreen = "welcome" }
        )

        "consent" -> ConsentScreen(
            onContinue = { currentScreen = "identity" },
            onBack = { currentScreen = "location" }
        )

        "identity" -> SimpleScreen(
            title = "Verify your identity",
            description = "This step helps keep Kanz safe and secure.",
            buttonText = "Continue",
            onButtonClick = { currentScreen = "login" }
        )

        "login" -> PhoneAuthenticationScreen(
            onVerified = { currentScreen = "verified" },
            onBack = { currentScreen = "welcome" }
        )

        "verified" -> SimpleScreen(
            title = "Phone verified!",
            description = "Your phone number has been verified successfully.",
            buttonText = "Continue to marketplace",
            onButtonClick = { currentScreen = "home" }
        )

        // EXISTING MARKETPLACE: preserved
        "home" -> KanzHomeScreen(
            onOpenSection = { section ->
                currentScreen = when (section.trim().lowercase()) {
                    "all", "home" -> "home"
                    "jobs" -> "jobs"
                    "mines" -> "mines"
                    "chats" -> "chats"
                    "my profile", "profile" -> "profile"
                    else -> "home"
                }
            },
            onLogout = {
                auth.signOut()
                currentScreen = "welcome"
            }
        )

        // NEW JOBS PAGE
        "jobs" -> KanzJobsPage(
            onBack = { currentScreen = "home" }
        )

        // NEW MINES PAGE
        "mines" -> KanzMinesPage(
            onBack = { currentScreen = "home" }
        )

        // NEW CHATS PAGE
        "chats" -> KanzChatsPage(
            onBack = { currentScreen = "home" }
        )

        // NEW PROFILE PAGE
        "profile" -> KanzProfilePage(
            onBack = { currentScreen = "home" },
            onLogout = {
                auth.signOut()
                currentScreen = "welcome"
            }
        )

        // Fallback for an unexpected screen
        else -> SimpleScreen(
            title = "Kanz",
            description = "Return to the Kanz marketplace.",
            buttonText = "Back to marketplace",
            onButtonClick = { currentScreen = "home" }
        )
    }
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    AppScreen {
        Spacer(Modifier.weight(1f))

        Text(
            "KANZ",
            color = Gold,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Welcome to Kanz",
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Your all-in-one Mining App",
            color = MutedText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        GoldButton("Get Started", onClick = onGetStarted)

        Spacer(Modifier.height(16.dp))

        Text("Have an account?", color = MutedText)

        TextButton(onClick = onLogin) {
            Text("Log in", color = Gold)
        }

        Spacer(Modifier.weight(1f))
    }
}

@Suppress("DEPRECATION")
@Composable
fun LocationScreen(
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var area by remember { mutableStateOf("Mauritania") }
    var locationMessage by remember { mutableStateOf("") }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                val detectedArea = getCurrentArea(context)

                if (detectedArea != null) {
                    area = detectedArea
                    locationMessage = "Location detected successfully."
                } else {
                    locationMessage =
                        "Location unavailable. Please enter an area manually."
                }
            } else {
                locationMessage =
                    "Permission denied. You can enter an area manually."
            }
        }

    AppScreen {
        Text(
            "Choose Your Location",
            color = WhiteText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Select the area where you want to use Kanz.",
            color = MutedText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = area,
            onValueChange = {
                area = it
                locationMessage = ""
            },
            label = { Text("Search by area") },
            placeholder = { Text("Enter a city or area") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = authFieldColors()
        )

        Spacer(Modifier.height(18.dp))

        GoldButton(
            text = "Enable Location",
            onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )

        if (locationMessage.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                locationMessage,
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        GoldButton(
            text = "Confirm",
            onClick = {
                if (area.isNotBlank()) {
                    onConfirm()
                } else {
                    locationMessage = "Please enter an area first."
                }
            }
        )

        TextButton(onClick = onBack) {
            Text("Back", color = Gold)
        }
    }
}

@Suppress("DEPRECATION")
private fun getCurrentArea(context: Context): String? {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted && !coarseGranted) return null

    return try {
        val manager = context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

        val location =
            manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: return null

        val addresses = Geocoder(
            context,
            Locale.getDefault()
        ).getFromLocation(location.latitude, location.longitude, 1)

        val address = addresses?.firstOrNull()

        address?.locality
            ?: address?.subAdminArea
            ?: address?.adminArea
            ?: address?.countryName
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ConsentScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    AppScreen {
        Text(
            "Consent Required",
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Consent is required to use Kanz.",
            color = WhiteText,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Kanz collects basic information to help maintain the security " +
                    "of the app. Users must be over 18 years old.",
            color = MutedText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Gold
                )
            )

            Text(
                "I agree and confirm that I am over 18 years old.",
                color = WhiteText,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        GoldButton(
            text = "Continue",
            onClick = {
                if (agreed) {
                    onContinue()
                } else {
                    message = "Please agree before continuing."
                }
            }
        )

        if (message.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(message, color = Gold, fontSize = 13.sp)
        }

        TextButton(onClick = onBack) {
            Text("Back", color = Gold)
        }
    }
}

@Composable
fun SimpleScreen(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    AppScreen {
        Text(
            title,
            color = WhiteText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            description,
            color = MutedText,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(30.dp))

        GoldButton(
            text = buttonText,
            onClick = onButtonClick
        )
    }
}

@Composable
fun PhoneAuthenticationScreen(
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }

    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var codeSent by remember { mutableStateOf(false) }

    AppScreen {
        Text(
            "KANZ",
            color = Gold,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text(
            if (codeSent) "Verify Your Phone" else "Log in to Kanz",
            color = WhiteText,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            if (codeSent) {
                "Enter the 6-digit code sent to your phone."
            } else {
                "Enter your phone number to receive an SMS code."
            },
            color = MutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        if (!codeSent) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone number") },
                placeholder = { Text("+821012345678") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                shape = RoundedCornerShape(12.dp),
                colors = authFieldColors()
            )

            Spacer(Modifier.height(18.dp))

            GoldButton(
                text = if (isLoading) "Sending..." else "Send verification code",
                enabled = !isLoading,
                onClick = {
                    val number = phoneNumber.trim()

                    if (activity == null) {
                        message = "Unable to access the current activity."
                    } else if (
                        !number.startsWith("+") ||
                        number.count { it.isDigit() } < 8
                    ) {
                        message =
                            "Enter a valid number with country code, e.g. +821012345678."
                    } else {
                        isLoading = true
                        message = "Requesting SMS verification..."

                        val callbacks =
                            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                override fun onVerificationCompleted(
                                    credential: PhoneAuthCredential
                                ) {
                                    isLoading = true
                                    message = "Verifying your phone number..."

                                    auth.signInWithCredential(credential)
                                        .addOnCompleteListener { task ->
                                            isLoading = false

                                            if (task.isSuccessful) {
                                                onVerified()
                                            } else {
                                                message =
                                                    task.exception?.localizedMessage
                                                        ?: "Verification failed."
                                            }
                                        }
                                }

                                override fun onVerificationFailed(
                                    e: FirebaseException
                                ) {
                                    isLoading = false
                                    message = e.localizedMessage
                                        ?: "Could not send the verification code."
                                }

                                override fun onCodeSent(
                                    id: String,
                                    token: PhoneAuthProvider.ForceResendingToken
                                ) {
                                    verificationId = id
                                    codeSent = true
                                    isLoading = false
                                    message =
                                        "SMS code sent. Please check your messages."
                                }
                            }

                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(number)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(callbacks)
                            .build()

                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }
                }
            )
        } else {
            OutlinedTextField(
                value = smsCode,
                onValueChange = {
                    smsCode = it.filter(Char::isDigit).take(6)
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("6-digit SMS code") },
                placeholder = { Text("123456") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                shape = RoundedCornerShape(12.dp),
                colors = authFieldColors()
            )

            Spacer(Modifier.height(18.dp))

            GoldButton(
                text = if (isLoading) "Verifying..." else "Verify and Continue",
                enabled = !isLoading,
                onClick = {
                    val id = verificationId

                    if (id == null) {
                        message = "Please request a new verification code."
                    } else if (smsCode.length != 6) {
                        message = "Enter the complete 6-digit code."
                    } else {
                        isLoading = true
                        message = "Verifying your phone number..."

                        val credential =
                            PhoneAuthProvider.getCredential(id, smsCode)

                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isLoading = false

                                if (task.isSuccessful) {
                                    onVerified()
                                } else {
                                    message =
                                        task.exception?.localizedMessage
                                            ?: "Incorrect code or verification failed."
                                }
                            }
                    }
                }
            )

            TextButton(
                enabled = !isLoading,
                onClick = {
                    codeSent = false
                    smsCode = ""
                    verificationId = null
                    message = ""
                }
            ) {
                Text("Change phone number", color = Gold)
            }
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                color = Gold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(18.dp))

        TextButton(
            enabled = !isLoading,
            onClick = onBack
        ) {
            Text("Back", color = Gold)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "SMS rates may apply. Your phone number is used for verification.",
            color = MutedText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun authFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = WhiteText,
        unfocusedTextColor = WhiteText,
        focusedLabelColor = Gold,
        unfocusedLabelColor = MutedText,
        focusedBorderColor = Gold,
        unfocusedBorderColor = MutedText,
        cursorColor = Gold
    )

@Composable
fun AppScreen(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun GoldButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = Background,
            disabledContainerColor = Gold.copy(alpha = 0.5f),
            disabledContentColor = Background
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

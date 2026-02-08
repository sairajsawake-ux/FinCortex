package com.example.fincortex.ui.login

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fincortex.R
import com.example.fincortex.ui.theme.DarkAccent
import com.example.fincortex.ui.theme.DarkBackground
import com.example.fincortex.ui.theme.DarkPrimary
import com.example.fincortex.ui.theme.DarkText
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isPhoneAuth by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val user = authTask.result?.user
                        user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result?.token
                                if (token != null) {
                                    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                    with(sharedPreferences.edit()) {
                                        putString("auth_token", token)
                                        apply()
                                    }
                                    onLoginSuccess(token)
                                }
                            } else {
                                errorMessage = tokenTask.exception?.message ?: "Failed to get token"
                            }
                        }
                    } else {
                        errorMessage = authTask.exception?.message ?: "Google sign-in failed"
                    }
                }
        } catch (e: ApiException) {
            errorMessage = "Google sign-in failed: ${e.message}"
        }
    }

    fun signInWithEmail() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    errorMessage = ""
                    val user = task.result?.user
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val token = tokenTask.result?.token
                            if (token != null) {
                                val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    putString("auth_token", token)
                                    apply()
                                }
                                onLoginSuccess(token)
                            }
                        } else {
                            errorMessage = tokenTask.exception?.message ?: "Failed to get token"
                        }
                    }
                } else {
                    errorMessage = task.exception?.message ?: "Authentication failed"
                }
            }
    }

    fun signUpWithEmail() {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    errorMessage = ""
                    val user = task.result?.user
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val token = tokenTask.result?.token
                            if (token != null) {
                                val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    putString("auth_token", token)
                                    apply()
                                }
                                onLoginSuccess(token)
                            }
                        } else {
                            errorMessage = tokenTask.exception?.message ?: "Failed to get token"
                        }
                    }
                } else {
                    errorMessage = task.exception?.message ?: "Sign-up failed"
                }
            }
    }

    fun sendOtp() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as android.app.Activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                                    if (tokenTask.isSuccessful) {
                                        val token = tokenTask.result?.token
                                        if (token != null) {
                                            val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                            with(sharedPreferences.edit()) {
                                                putString("auth_token", token)
                                                apply()
                                            }
                                            onLoginSuccess(token)
                                        }
                                    } else {
                                        errorMessage = tokenTask.exception?.message ?: "Failed to get token"
                                    }
                                }
                            } else {
                                errorMessage = task.exception?.message ?: "Phone authentication failed"
                            }
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    errorMessage = e.message ?: "OTP verification failed"
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    errorMessage = ""
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp() {
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result?.token
                                if (token != null) {
                                    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                    with(sharedPreferences.edit()) {
                                        putString("auth_token", token)
                                        apply()
                                    }
                                    onLoginSuccess(token)
                                }
                            } else {
                                errorMessage = tokenTask.exception?.message ?: "Failed to get token"
                            }
                        }
                    } else {
                        errorMessage = task.exception?.message ?: "OTP verification failed"
                    }
                }
        }
    }

    fun startGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkPrimary)))
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.fincortex_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, DarkAccent, CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FinCortex",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = DarkAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (isPhoneAuth) {
            if (verificationId == null) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number", color = DarkText) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = TextStyle(color = DarkText),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = DarkAccent,
                        unfocusedBorderColor = DarkText,
                        cursorColor = DarkAccent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { sendOtp() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                ) {
                    Text("Send OTP", color = DarkPrimary)
                }
            } else {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("OTP", color = DarkText) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = TextStyle(color = DarkText),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = DarkAccent,
                        unfocusedBorderColor = DarkText,
                        cursorColor = DarkAccent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { verifyOtp() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                ) {
                    Text("Verify OTP", color = DarkPrimary)
                }
            }
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = DarkText) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                textStyle = TextStyle(color = DarkText),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = DarkAccent,
                    unfocusedBorderColor = DarkText,
                    cursorColor = DarkAccent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = DarkText) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = DarkAccent)
                    }
                },
                textStyle = TextStyle(color = DarkText),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = DarkAccent,
                    unfocusedBorderColor = DarkText,
                    cursorColor = DarkAccent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up",
                    color = DarkAccent,
                    modifier = Modifier.clickable { isSignUp = !isSignUp }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { if (isSignUp) signUpWithEmail() else signInWithEmail() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
            ) {
                Text(if (isSignUp) "Sign Up" else "Login", color = DarkPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { startGoogleSignIn() },
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
            ) {
                Text("Google", color = androidx.compose.ui.graphics.Color.Black)
            }

            Button(
                onClick = { isPhoneAuth = !isPhoneAuth; verificationId = null; otp = ""; phoneNumber = "" },
                colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
            ) {
                Text("Phone", color = DarkPrimary)
            }
        }
    }
}

package com.example.passionDaily.login.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.login.presentation.viewmodel.LoginViewModel
import com.example.passionDaily.login.state.AuthState
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.PrimaryColor

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: () -> Unit
) {
    val isLoading by loginViewModel.isLoading.collectAsState()
    val authState by loginViewModel.authState.collectAsState()
    val formState by loginViewModel.signupFormState.collectAsState()

    LoginScreenContent(
        email = formState.email,  // StateHolder의 email 사용
        onEmailChange = { loginViewModel.updateEmail(it) },  // StateHolder로 email 업데이트
        onLoginClick = { loginViewModel.signup() }  // 이메일은 이미 StateHolder에 있으므로 파라미터 불필요
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.RequiresConsent -> {
                onNavigateToTermsConsent()
            }
            is AuthState.Authenticated -> {
                onNavigateToQuote()
                    // TODO: signal?
            }
            is AuthState.Unauthenticated -> { }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun LoginScreenContent(
    email: String,  // StateHolder에서 받은 email
    onEmailChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground),
    ) {
        MainContent(
            email = email,
            onEmailChange = onEmailChange,
            onLoginClick = onLoginClick
        )
    }
}

@Composable
private fun MainContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()
        EmailInput(
            email = email,
            onEmailChange = onEmailChange,
            onLoginClick = onLoginClick,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LoginButton(
            onLoginClick = onLoginClick,
            modifier = Modifier
        )
    }
}

@Composable
private fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.app_logo_with_text),
        contentDescription = "App Logo",
        modifier = Modifier
            .padding(bottom = 70.dp)
    )
}

@Composable
private fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = { Text("이메일", fontSize = 14.sp) },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onLoginClick() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White,
            focusedBorderColor = Color(0xFF1A3C96)
        )
    )
}

@Composable
private fun LoginButton(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onLoginClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1A3C96),
            contentColor = Color.White
        )
    ) {
        Text("로그인")
    }
}


//@Composable
//fun LoginScreen(
//    loginViewModel: LoginViewModel,
//    onNavigateToQuote: () -> Unit,
//    onNavigateToSignup: () -> Unit,
//) {
//    val isLoading by loginViewModel.isLoading.collectAsState()
//    val authState by loginViewModel.authState.collectAsState()
//
//    LoginScreenContent(
//        onNavigateToSignup = onNavigateToSignup
//    )
//
//    LaunchedEffect(authState) {
//        if (authState is AuthState.Authenticated) {
//            // TODO: signal?
//            onNavigateToQuote()
//        }
//    }
//
//    if (isLoading) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f))
//                .clickable(enabled = false) {},
//            contentAlignment = Alignment.Center,
//        ) {
//            CircularProgressIndicator(
//                color = PrimaryColor,
//                modifier = Modifier.size(48.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun LoginScreenContent(
//    onNavigateToSignup: () -> Unit
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(BlackBackground),
//    ) {
//        MainContent(
//            email = email,
//            onEmailChange = { email = it },
//            password = password,
//            onPasswordChange = { password = it },
//            passwordVisible = passwordVisible,
//            onPasswordVisibilityChange = { passwordVisible = it },
//            onLoginClick = {  }
//        )
//
//        SignUpButton(
//            onSignupClick = onNavigateToSignup,
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 70.dp)
//        )
//    }
//}
//
//@Composable
//private fun MainContent(
//    email: String,
//    onEmailChange: (String) -> Unit,
//    password: String,
//    onPasswordChange: (String) -> Unit,
//    passwordVisible: Boolean,
//    onPasswordVisibilityChange: (Boolean) -> Unit,
//    onLoginClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 24.dp)
//            .padding(top = 150.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        AppLogo()
//        EmailInput(
//            email = email,
//            onEmailChange = onEmailChange,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//        PasswordInput(
//            password = password,
//            onPasswordChange = onPasswordChange,
//            passwordVisible = passwordVisible,
//            onPasswordVisibilityChange = onPasswordVisibilityChange,
//            modifier = Modifier.padding(bottom = 24.dp)
//        )
//        LoginButton(
//            onClick = onLoginClick
//        )
//        FindPasswordButton(onFindPasswordClick = {})
//    }
//}
//
//@Composable
//private fun AppLogo() {
//    Image(
//        painter = painterResource(id = R.drawable.app_logo_with_text),
//        contentDescription = "App Logo",
//        modifier = Modifier
//            .padding(bottom = 70.dp)
//    )
//}
//
//@Composable
//private fun EmailInput(
//    email: String,
//    onEmailChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = email,
//        onValueChange = onEmailChange,
//        placeholder = {
//            Text(
//                "이메일",
//                fontSize = 14.sp
//            )
//        },
//        modifier = modifier
//            .fillMaxWidth()
//            .defaultMinSize(minHeight = 50.dp),  // height 대신 defaultMinSize 사용
//        singleLine = true,
//        textStyle = TextStyle(
//            fontSize = 14.sp
//        ),
//        keyboardOptions = KeyboardOptions(
//            keyboardType = KeyboardType.Email,
//            imeAction = ImeAction.Next
//        ),
//        colors = OutlinedTextFieldDefaults.colors(
//            unfocusedBorderColor = Color.White,
//            focusedBorderColor = Color(0xFF1A3C96)
//        )
//    )
//}
//
//@Composable
//private fun PasswordInput(
//    password: String,
//    onPasswordChange: (String) -> Unit,
//    passwordVisible: Boolean,
//    onPasswordVisibilityChange: (Boolean) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = password,
//        onValueChange = onPasswordChange,
//        placeholder = {
//            Text(
//                "비밀번호",
//                fontSize = 14.sp
//            )
//        },
//        visualTransformation = if (passwordVisible) VisualTransformation.None
//        else PasswordVisualTransformation(),
//        trailingIcon = {
//            IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
//                Icon(
//                    painter = painterResource(
//                        id = if (passwordVisible) R.drawable.visibility_icon
//                        else R.drawable.visibility_off_icon
//                    ),
//                    contentDescription = if (passwordVisible) "Hide password"
//                    else "Show password"
//                )
//            }
//        },
//        modifier = modifier
//            .fillMaxWidth()
//            .defaultMinSize(minHeight = 50.dp),  // height 대신 defaultMinSize 사용
//        singleLine = true,
//        textStyle = TextStyle(
//            fontSize = 14.sp
//        ),
//        keyboardOptions = KeyboardOptions(
//            keyboardType = KeyboardType.Password,
//            imeAction = ImeAction.Done
//        ),
//        colors = OutlinedTextFieldDefaults.colors(
//            unfocusedBorderColor = Color.White,
//            focusedBorderColor = Color(0xFF1A3C96)
//        )
//    )
//}
//
//@Composable
//private fun LoginButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Button(
//        onClick = onClick,
//        modifier = modifier
//            .fillMaxWidth()
//            .height(48.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color(0xFF1A3C96),
//            contentColor = Color.White
//        )
//    ) {
//        Text("로그인")
//    }
//}
//
//@Composable
//private fun FindPasswordButton(
//    onFindPasswordClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    TextButton(
//        onClick = { },
//        modifier = modifier
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "비밀번호를 잊어버리셨나요? ",
//                color = Color(0xFFCCCCCC)
//            )
//            Text(
//                text = "비밀번호 찾기",
//                color = Color(0xFF1A3C96),
//                modifier = Modifier.clickable { onFindPasswordClick() }
//            )
//        }
//    }
//}
//
//@Composable
//private fun SignUpButton(
//    onSignupClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    TextButton(
//        onClick = { },
//        modifier = modifier
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "아직 회원이 아니신가요? ",
//                color = Color(0xFFCCCCCC)
//            )
//            Text(
//                text = "회원가입 하기",
//                color = Color(0xFF1A3C96),
//                modifier = Modifier.clickable { onSignupClick() }
//            )
//        }
//    }
//}
package org.society.appname.authentication.presentation.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Theme colors - Customize these for your app
object WelcomeTheme {
    val primaryDark = Color(0xFF1A1A2E)
    val primaryAccent = Color(0xFF6366F1) // Indigo
    val secondaryAccent = Color(0xFF8B5CF6) // Purple
    val surfaceLight = Color(0xFFFAFAFA)
    val textPrimary = Color(0xFF1F2937)
    val textSecondary = Color(0xFF6B7280)
    val gradientStart = Color(0xFF6366F1)
    val gradientEnd = Color(0xFFA855F7)
}

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    // Optional: Pass your app's background image resource
    // backgroundImage: DrawableResource? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(300)
        showBottomSheet = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WelcomeTheme.primaryDark)
    ) {
        // Background - Replace with your app image
        // If you have an image resource:
        // Image(
        //     painter = painterResource(backgroundImage),
        //     contentDescription = null,
        //     modifier = Modifier.fillMaxSize(),
        //     contentScale = ContentScale.Crop
        // )

        // Placeholder gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WelcomeTheme.primaryDark,
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        )

        // Decorative elements
        DecorativeBackground()

        // App Logo & Tagline
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(
                initialOffsetY = { -100 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // App Logo placeholder - Replace with your logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    WelcomeTheme.gradientStart,
                                    WelcomeTheme.gradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "AppName",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Découvrez une nouvelle façon\nde connecter et partager",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        // Bottom Sheet
        AnimatedVisibility(
            visible = showBottomSheet,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            WelcomeBottomSheet(
                onGetStarted = onGetStarted,
                onLogin = onLogin
            )
        }
    }
}

@Composable
private fun DecorativeBackground() {
    // Floating orbs for visual interest
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()

    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(3000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(2500),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Orb 1
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = 200.dp + floatOffset1.dp)
                .size(200.dp)
                .blur(60.dp)
                .background(
                    WelcomeTheme.primaryAccent.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(100.dp)
                )
        )

        // Orb 2
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = (-50).dp + floatOffset2.dp)
                .size(180.dp)
                .blur(50.dp)
                .background(
                    WelcomeTheme.secondaryAccent.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(90.dp)
                )
        )

        // Orb 3
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-200).dp + floatOffset1.dp)
                .size(120.dp)
                .blur(40.dp)
                .background(
                    WelcomeTheme.gradientEnd.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(60.dp)
                )
        )
    }
}

@Composable
private fun WelcomeBottomSheet(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 20.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle indicator
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Bienvenue",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WelcomeTheme.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Créez votre compte ou connectez-vous pour commencer votre aventure",
                fontSize = 15.sp,
                color = WelcomeTheme.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Get Started Button (Primary)
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    WelcomeTheme.gradientStart,
                                    WelcomeTheme.gradientEnd
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button (Secondary)
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            WelcomeTheme.gradientStart,
                            WelcomeTheme.gradientEnd
                        )
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WelcomeTheme.primaryAccent
                )
            ) {
                Text(
                    text = "J'ai déjà un compte",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms text
            Text(
                text = "En continuant, vous acceptez nos Conditions d'utilisation et notre Politique de confidentialité",
                fontSize = 12.sp,
                color = WelcomeTheme.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
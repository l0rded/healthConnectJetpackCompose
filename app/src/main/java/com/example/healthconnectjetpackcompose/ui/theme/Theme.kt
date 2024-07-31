package com.example.healthconnectjetpackcompose.ui.theme

import android.icu.text.CaseMap.Title
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.healthconnectjetpackcompose.R

val mPlusRounded1cFamily = FontFamily(
    Font(R.font.m_plus_rounded_1c_bold, FontWeight.Normal)
)

val mPlusRounded1c = Typography(
    titleMedium = TextStyle(
        fontFamily = mPlusRounded1cFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = mPlusRounded1cFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    ,
    bodySmall = TextStyle(
        fontFamily = mPlusRounded1cFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )

    // Define other text styles as needed
)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun HealthConnectJetpackComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = mPlusRounded1c,
        content = content
    )
}
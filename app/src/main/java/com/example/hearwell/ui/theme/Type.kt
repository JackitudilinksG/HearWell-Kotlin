package com.example.hearwell.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.example.hearwell.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val customFont = FontFamily(
    Font(R.font.publicsans_black, weight = FontWeight.Black),
    Font(R.font.publicsans_bold, weight = FontWeight.Bold),
    Font(R.font.publicsans_extrabold, weight = FontWeight.ExtraBold),
    Font(R.font.publicsans_extralight, weight = FontWeight.ExtraLight),
    Font(R.font.publicsans_light, weight = FontWeight.Light),
    Font(R.font.publicsans_medium, weight = FontWeight.Medium),
    Font(R.font.publicsans_regular, weight = FontWeight.Normal),
    Font(R.font.publicsans_semibold, weight = FontWeight.SemiBold),
    Font(R.font.publicsans_thin, weight = FontWeight.Thin)
)
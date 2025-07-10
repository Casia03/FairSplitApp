package com.example.fairsplit.ui.theme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.fairsplit.R
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

val Manrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

val AppTypography = Typography(
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Light,
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.ExtraLight,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Light,
        fontSize = 24.sp,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
    )
    // Weitere TextStyles hier anpassen, falls nötig
)


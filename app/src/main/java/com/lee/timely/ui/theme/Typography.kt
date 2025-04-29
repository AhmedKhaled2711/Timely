package com.lee.timely.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.lee.timely.R

// Example font families (you must place these fonts in res/font)
val EnglishFontFamily = FontFamily(
    Font(R.font.winkyrough_mediumitalic, FontWeight.Normal),
    Font(R.font.winkyrough_mediumitalic, FontWeight.Bold)
)

val ArabicFontFamily = FontFamily(
    Font(R.font.winkyrough_mediumitalic, FontWeight.Normal),
    Font(R.font.winkyrough_mediumitalic, FontWeight.Bold)
)

val EnglishTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    // Add other styles as needed
)

val ArabicTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = ArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    // Add other styles as needed
)
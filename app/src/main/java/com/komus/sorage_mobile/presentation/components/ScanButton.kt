package com.komus.sorage_mobile.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.komus.sorage_mobile.presentation.theme.Primary

@Composable
fun ScanButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Primary,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = null
        )
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
} 
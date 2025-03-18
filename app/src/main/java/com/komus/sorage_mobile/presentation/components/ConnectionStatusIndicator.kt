package com.komus.sorage_mobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConnectionStatusIndicator(isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    color = if (isConnected) Color.Green else Color.Red,
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = if (isConnected) "Онлайн" else "Оффлайн",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isConnected) Color.Green else Color.Red
        )
    }
} 
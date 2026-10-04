package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun VolumeRocker(
    title: String,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    testTagPrefix: String = "rocker"
) {
    Box(
        modifier = modifier
            .width(68.dp)
            .height(148.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, ButtonBorder, RoundedCornerShape(34.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.height(148.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Plus button
            TactileButton(
                onClick = onPlus,
                icon = Icons.Default.Add,
                backgroundColor = Color.Transparent,
                borderColor = Color.Transparent,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                modifier = Modifier
                    .size(width = 68.dp, height = 50.dp),
                testTag = "${testTagPrefix}_plus"
            )

            // Center Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (statusText != null) {
                    Text(
                        text = statusText,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Minus button
            TactileButton(
                onClick = onMinus,
                icon = Icons.Default.Remove,
                backgroundColor = Color.Transparent,
                borderColor = Color.Transparent,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp),
                modifier = Modifier
                    .size(width = 68.dp, height = 50.dp),
                testTag = "${testTagPrefix}_minus"
            )
        }
    }
}

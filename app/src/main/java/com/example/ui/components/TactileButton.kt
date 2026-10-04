package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BraviaBlueGlow
import com.example.ui.theme.ButtonBackground
import com.example.ui.theme.ButtonBackgroundPressed
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.TextPrimary

@Composable
fun TactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    label: String? = null,
    backgroundColor: Color = ButtonBackground,
    contentColor: Color = TextPrimary,
    borderColor: Color = ButtonBorder,
    shape: Shape = RoundedCornerShape(16.dp),
    minSize: Dp = 48.dp,
    testTag: String = "tactile_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        label = "button_scale"
    )

    val actualBg = if (isPressed) ButtonBackgroundPressed else backgroundColor

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .clip(shape)
            .background(actualBg)
            .border(
                width = if (isPressed) 1.5.dp else 1.dp,
                color = if (isPressed) BraviaBlueGlow else borderColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = if (label != null) 12.dp else 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label ?: "Button icon",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (icon != null && label != null) {
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (label != null) {
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

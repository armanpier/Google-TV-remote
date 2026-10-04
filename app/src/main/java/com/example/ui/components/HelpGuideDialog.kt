package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BraviaBlue
import com.example.ui.theme.BraviaBlueGlow
import com.example.ui.theme.ButtonBackground
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HelpGuideDialog(
    onDismiss: () -> Unit,
    onMakeDiscoverable: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Bluetooth Mouse, 1: Wi-Fi Setup

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connection & Setup Guide",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tab Switcher Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) ButtonBackground else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mouse,
                                contentDescription = null,
                                tint = if (selectedTab == 0) TextPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mouse & Cursor",
                                color = if (selectedTab == 0) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) ButtonBackground else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (selectedTab == 1) TextPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wi-Fi Remote",
                                color = if (selectedTab == 1) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    // Bluetooth HID Mouse & Keyboard instructions
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "How to see Native Mouse Cursor on your TV:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        GuideStepItem(
                            number = 1,
                            title = "Make phone discoverable",
                            description = "Tap the button below to broadcast your phone as a Bluetooth Mouse & Keyboard for 3 minutes."
                        )

                        TactileButton(
                            onClick = onMakeDiscoverable,
                            icon = Icons.Default.Visibility,
                            label = "Make Phone Discoverable Now",
                            backgroundColor = ButtonBackground,
                            borderColor = ButtonBorder,
                            contentColor = TextPrimary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        GuideStepItem(
                            number = 2,
                            title = "Open TV Bluetooth Settings",
                            description = "On your Sony Bravia / Google TV remote, open Settings ➔ Remotes & Accessories ➔ Add accessory / Pair Bluetooth."
                        )

                        GuideStepItem(
                            number = 3,
                            title = "Select your phone",
                            description = "Your TV will scan and display 'Sony Bravia Remote'. Tap it to pair."
                        )

                        GuideStepItem(
                            number = 4,
                            title = "Move the Trackpad",
                            description = "Google TV OS will now display the native mouse cursor arrow! Move your finger on the trackpad to navigate."
                        )
                    }
                } else {
                    // Wi-Fi Setup instructions
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "How to enable Sony IP Control on your TV:",
                            color = BraviaBlueGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        GuideStepItem(
                            number = 1,
                            title = "Connect to same Wi-Fi",
                            description = "Ensure your phone and Sony TV are on the same Wi-Fi network."
                        )

                        GuideStepItem(
                            number = 2,
                            title = "Enable IP Control on TV",
                            description = "On TV: Go to Settings ➔ Network & Internet ➔ Home network ➔ IP control."
                        )

                        GuideStepItem(
                            number = 3,
                            title = "Set Pre-Shared Key to 0000",
                            description = "Set 'Authentication' to 'Pre-Shared Key' and enter 0000 (default), then enable 'Simple IP control'."
                        )

                        GuideStepItem(
                            number = 4,
                            title = "Ready to Control!",
                            description = "All buttons (Power, D-Pad, Channels, Volume slider, Inputs) will work instantly with zero delay."
                        )
                    }
                }
            }
        },
        confirmButton = {
            TactileButton(
                onClick = onDismiss,
                label = "Got It",
                backgroundColor = DarkSurfaceElevated,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(12.dp)
            )
        }
    )
}

@Composable
private fun GuideStepItem(
    number: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BraviaBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = BraviaBlueGlow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

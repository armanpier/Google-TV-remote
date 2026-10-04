package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RemoteCommand
import com.example.ui.components.TactileButton
import com.example.ui.theme.BraviaBlue
import com.example.ui.theme.BraviaBlueGlow
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.PowerRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RemoteUiState

data class InputItem(
    val id: Int,
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
    val command: RemoteCommand
)

data class AppItem(
    val name: String,
    val category: String,
    val icon: ImageVector,
    val accentColor: Color,
    val command: RemoteCommand
)

@Composable
fun InputsAndAppsScreen(
    uiState: RemoteUiState,
    onSwitchInput: (Int, Context) -> Unit,
    onSendCommand: (RemoteCommand, Context) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val inputList = listOf(
        InputItem(1, "HDMI 1", "4K 120Hz / eARC Soundbar", Icons.Default.LiveTv, RemoteCommand.HDMI1),
        InputItem(2, "HDMI 2", "4K HDR (PlayStation / Xbox)", Icons.Default.Games, RemoteCommand.HDMI2),
        InputItem(3, "HDMI 3", "4K 60Hz / Apple TV / PC", Icons.Default.Movie, RemoteCommand.HDMI3),
        InputItem(4, "HDMI 4", "4K 120Hz VRR / Console", Icons.Default.Input, RemoteCommand.HDMI4),
        InputItem(5, "TV Tuner", "Digital Antenna / DVB-T2", Icons.Default.Tv, RemoteCommand.TV),
        InputItem(6, "Video / AV", "Composite Video Input", Icons.Default.Radio, RemoteCommand.VIDEO1)
    )

    val appList = listOf(
        AppItem("YouTube", "Video & 4K Streaming", Icons.Default.PlayCircle, PowerRed, RemoteCommand.APP_YOUTUBE),
        AppItem("Netflix", "Movies & 4K Series", Icons.Default.Movie, Color(0xFFE50914), RemoteCommand.APP_NETFLIX),
        AppItem("Prime Video", "Amazon Originals", Icons.Default.Subscriptions, Color(0xFF00A8E1), RemoteCommand.APP_PRIME_VIDEO),
        AppItem("Disney+", "Disney, Marvel, Star Wars", Icons.Default.Cast, Color(0xFF113CCF), RemoteCommand.APP_DISNEY),
        AppItem("Google Play", "Apps & Games Store", Icons.Default.OndemandVideo, ElectricIndigo, RemoteCommand.APP_GOOGLE_PLAY),
        AppItem("Spotify", "Music & Podcasts", Icons.Default.MusicNote, EmeraldConnected, RemoteCommand.APP_YOUTUBE)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: TV Inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "External Inputs",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Instant 4K HDMI & Video port switching",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Input Cycle button
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.INPUT_TOGGLE, context) },
                icon = Icons.Default.Input,
                label = "Cycle Input",
                backgroundColor = DarkSurfaceCard,
                shape = RoundedCornerShape(16.dp),
                testTag = "cycle_input_button"
            )
        }

        // Grid of Inputs
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            inputList.forEach { input ->
                val isActive = uiState.currentInput.equals(input.name, ignoreCase = true)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) DarkSurfaceElevated else DarkSurfaceCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isActive) 1.5.dp else 1.dp,
                            color = if (isActive) BraviaBlue else ButtonBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (input.id in 1..4) {
                                onSwitchInput(input.id, context)
                            } else {
                                onSendCommand(input.command, context)
                            }
                        }
                        .testTag("input_${input.name.replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) BraviaBlue.copy(alpha = 0.2f) else DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = input.icon,
                                    contentDescription = input.name,
                                    tint = if (isActive) BraviaBlueGlow else TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = input.name,
                                    color = if (isActive) BraviaBlueGlow else TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = input.subtitle,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isActive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldConnected)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTIVE",
                                    color = EmeraldConnected,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section: Streaming Apps
        Text(
            text = "Google TV Apps",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            appList.chunked(2).forEach { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowApps.forEach { app ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, ButtonBorder, RoundedCornerShape(16.dp))
                                .clickable { onSendCommand(app.command, context) }
                                .testTag("app_${app.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(app.accentColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = app.icon,
                                        contentDescription = app.name,
                                        tint = app.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = app.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = app.category,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RemoteCommand
import com.example.ui.components.DirectionalPad
import com.example.ui.components.TactileButton
import com.example.ui.components.VolumeRocker
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BraviaBlue
import com.example.ui.theme.BraviaBlueGlow
import com.example.ui.theme.ButtonBackground
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PowerRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RemoteUiState

@Composable
fun RemoteScreen(
    uiState: RemoteUiState,
    onSendCommand: (RemoteCommand, Context) -> Unit,
    onSetVolume: (Int, Context) -> Unit,
    onNavigateToInputs: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    var showNumpad by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top System Controls: Power, Input, Guide, Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power Button (Glowing Red)
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.POWER, context) },
                icon = Icons.Default.PowerSettingsNew,
                backgroundColor = PowerRed.copy(alpha = 0.2f),
                borderColor = PowerRed,
                contentColor = PowerRed,
                shape = CircleShape,
                minSize = 52.dp,
                testTag = "remote_power"
            )

            // Input Switcher (Icon Only)
            TactileButton(
                onClick = onNavigateToInputs,
                icon = Icons.Default.Input,
                backgroundColor = DarkSurfaceCard,
                contentColor = TextPrimary,
                shape = CircleShape,
                minSize = 48.dp,
                testTag = "remote_input_switch"
            )

            // TV Mode / Guide
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.TV, context) },
                icon = Icons.Default.Tv,
                backgroundColor = DarkSurfaceCard,
                contentColor = TextPrimary,
                shape = CircleShape,
                minSize = 48.dp,
                testTag = "remote_tv_guide"
            )

            // Numpad Toggle Button
            TactileButton(
                onClick = { showNumpad = !showNumpad },
                label = "123",
                backgroundColor = if (showNumpad) ElectricIndigo.copy(alpha = 0.3f) else DarkSurfaceCard,
                borderColor = if (showNumpad) ElectricIndigo else ButtonBackground,
                contentColor = if (showNumpad) Color.White else TextSecondary,
                shape = RoundedCornerShape(16.dp),
                testTag = "remote_toggle_numpad"
            )
        }

        // Expandable Numpad
        AnimatedVisibility(
            visible = showNumpad,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rows = listOf(
                        listOf(RemoteCommand.NUM_1, RemoteCommand.NUM_2, RemoteCommand.NUM_3),
                        listOf(RemoteCommand.NUM_4, RemoteCommand.NUM_5, RemoteCommand.NUM_6),
                        listOf(RemoteCommand.NUM_7, RemoteCommand.NUM_8, RemoteCommand.NUM_9),
                        listOf(RemoteCommand.COLOR_RED, RemoteCommand.NUM_0, RemoteCommand.COLOR_BLUE)
                    )
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { cmd ->
                                val isRed = cmd == RemoteCommand.COLOR_RED
                                val isBlue = cmd == RemoteCommand.COLOR_BLUE
                                TactileButton(
                                    onClick = { onSendCommand(cmd, context) },
                                    label = if (isRed) "RED" else if (isBlue) "BLUE" else cmd.title,
                                    backgroundColor = when {
                                        isRed -> PowerRed.copy(alpha = 0.3f)
                                        isBlue -> BraviaBlue.copy(alpha = 0.3f)
                                        else -> DarkSurfaceElevated
                                    },
                                    contentColor = when {
                                        isRed -> PowerRed
                                        isBlue -> BraviaBlueGlow
                                        else -> TextPrimary
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    testTag = "numpad_${cmd.name}"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Navigation D-Pad Cluster
        DirectionalPad(
            onUp = { onSendCommand(RemoteCommand.UP, context) },
            onDown = { onSendCommand(RemoteCommand.DOWN, context) },
            onLeft = { onSendCommand(RemoteCommand.LEFT, context) },
            onRight = { onSendCommand(RemoteCommand.RIGHT, context) },
            onConfirm = { onSendCommand(RemoteCommand.CONFIRM, context) }
        )

        // Navigation Action Bar: Back, Home, Menu (Icons Only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.BACK, context) },
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                shape = CircleShape,
                minSize = 52.dp,
                testTag = "remote_back"
            )
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.HOME, context) },
                icon = Icons.Default.Home,
                backgroundColor = DarkSurfaceCard,
                contentColor = TextPrimary,
                shape = CircleShape,
                minSize = 52.dp,
                testTag = "remote_home"
            )
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.ACTION_MENU, context) },
                icon = Icons.Default.Menu,
                shape = CircleShape,
                minSize = 52.dp,
                testTag = "remote_menu"
            )
        }

        // Audio & Channel Rockers Section with Mute & Direct Volume Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Volume Rocker
                    VolumeRocker(
                        title = "VOL",
                        statusText = "${uiState.volume}%",
                        onPlus = { onSendCommand(RemoteCommand.VOLUME_UP, context) },
                        onMinus = { onSendCommand(RemoteCommand.VOLUME_DOWN, context) },
                        testTagPrefix = "volume"
                    )

                    // Center Audio Controls: Mute Toggle and Quick Settings (Icons Only)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Mute button
                        TactileButton(
                            onClick = { onSendCommand(RemoteCommand.MUTE, context) },
                            icon = if (uiState.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            backgroundColor = if (uiState.isMuted) AmberWarning.copy(alpha = 0.25f) else DarkSurfaceCard,
                            borderColor = if (uiState.isMuted) AmberWarning else ButtonBorder,
                            contentColor = if (uiState.isMuted) AmberWarning else TextPrimary,
                            shape = CircleShape,
                            minSize = 52.dp,
                            testTag = "remote_mute"
                        )

                        // Quick Settings
                        TactileButton(
                            onClick = { onSendCommand(RemoteCommand.QUICK_SETTINGS, context) },
                            icon = Icons.Default.Settings,
                            backgroundColor = DarkSurfaceCard,
                            shape = CircleShape,
                            minSize = 52.dp,
                            testTag = "remote_settings_btn"
                        )
                    }

                    // Channel Rocker
                    VolumeRocker(
                        title = "CH",
                        statusText = "Tuner",
                        onPlus = { onSendCommand(RemoteCommand.CHANNEL_UP, context) },
                        onMinus = { onSendCommand(RemoteCommand.CHANNEL_DOWN, context) },
                        testTagPrefix = "channel"
                    )
                }

                // Smooth direct volume slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Volume",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(55.dp)
                    )
                    Slider(
                        value = uiState.volume.toFloat().coerceIn(0f, 100f),
                        onValueChange = { onSetVolume(it.toInt(), context) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = BraviaBlueGlow,
                            activeTrackColor = BraviaBlue,
                            inactiveTrackColor = ButtonBackground
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${uiState.volume}",
                        color = BraviaBlueGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(36.dp)
                            .padding(start = 6.dp)
                    )
                }
            }
        }

        // Media Playback Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.REWIND, context) },
                icon = Icons.Default.FastRewind,
                shape = CircleShape,
                minSize = 48.dp,
                testTag = "media_rewind"
            )
            TactileButton(
                onClick = {
                    isPlaying = !isPlaying
                    onSendCommand(if (isPlaying) RemoteCommand.PLAY else RemoteCommand.PAUSE, context)
                },
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                backgroundColor = BraviaBlueGlow,
                contentColor = Color.Black,
                shape = CircleShape,
                minSize = 56.dp,
                testTag = "media_play_pause"
            )
            TactileButton(
                onClick = { onSendCommand(RemoteCommand.FAST_FORWARD, context) },
                icon = Icons.Default.FastForward,
                shape = CircleShape,
                minSize = 48.dp,
                testTag = "media_forward"
            )
        }

        // Quick Streaming App Launch Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickApps = listOf(
                Pair("YouTube", RemoteCommand.APP_YOUTUBE),
                Pair("Netflix", RemoteCommand.APP_NETFLIX),
                Pair("Prime", RemoteCommand.APP_PRIME_VIDEO),
                Pair("Disney+", RemoteCommand.APP_DISNEY)
            )
            quickApps.forEach { (name, cmd) ->
                TactileButton(
                    onClick = { onSendCommand(cmd, context) },
                    label = name,
                    backgroundColor = DarkSurfaceCard,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_app_$name"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

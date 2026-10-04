package com.example.ui.screens

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.bluetooth.BluetoothHidState
import com.example.data.model.RemoteCommand
import com.example.ui.components.TactileButton
import com.example.ui.theme.ButtonBackground
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.PowerRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrackpadGrid
import com.example.ui.theme.TrackpadSurface
import com.example.ui.viewmodel.RemoteUiState

@Composable
fun TrackpadMouseScreen(
    uiState: RemoteUiState,
    onMove: (Float, Float, Context) -> Unit,
    onClick: (Boolean, Context) -> Unit,
    onScroll: (Float, Context) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onSendCommand: (RemoteCommand, Context) -> Unit,
    onConnectBluetooth: (BluetoothDevice) -> Unit,
    onDisconnectBluetooth: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onOpenHelp: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val isBtConnected = uiState.bluetoothState == BluetoothHidState.CONNECTED
    var showDeviceList by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Bluetooth Mouse Connection Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isBtConnected) DarkSurfaceElevated else DarkSurfaceCard
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isBtConnected) EmeraldConnected.copy(alpha = 0.5f) else ButtonBorder,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isBtConnected) EmeraldConnected.copy(alpha = 0.15f) else DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBtConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = "Bluetooth Status",
                                tint = if (isBtConnected) EmeraldConnected else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBtConnected) "Mouse Cursor Active" else "Trackpad Mode",
                                color = if (isBtConnected) EmeraldConnected else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBtConnected) {
                                    "${uiState.bluetoothDeviceName ?: uiState.currentTv.name}"
                                } else {
                                    uiState.currentTv.name
                                },
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action buttons (Icon Only)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isBtConnected) {
                            TactileButton(
                                onClick = onDisconnectBluetooth,
                                icon = Icons.Default.BluetoothDisabled,
                                backgroundColor = PowerRed.copy(alpha = 0.15f),
                                borderColor = PowerRed.copy(alpha = 0.6f),
                                contentColor = PowerRed,
                                shape = CircleShape,
                                minSize = 38.dp,
                                testTag = "disconnect_bt_button"
                            )
                        } else {
                            TactileButton(
                                onClick = onMakeDiscoverable,
                                icon = Icons.Default.Visibility,
                                backgroundColor = DarkSurfaceElevated,
                                contentColor = TextPrimary,
                                shape = CircleShape,
                                minSize = 38.dp,
                                testTag = "make_discoverable_btn"
                            )
                            TactileButton(
                                onClick = { showDeviceList = !showDeviceList },
                                icon = Icons.Default.Tv,
                                backgroundColor = DarkSurfaceElevated,
                                contentColor = if (showDeviceList) EmeraldConnected else TextSecondary,
                                shape = CircleShape,
                                minSize = 38.dp,
                                testTag = "select_tv_bt_btn"
                            )
                            IconButton(
                                onClick = onOpenHelp,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Help Guide",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Paired TV Device picker dropdown (if requested)
                AnimatedVisibility(visible = showDeviceList && !isBtConnected) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (uiState.pairedBluetoothDevices.isNotEmpty()) {
                            uiState.pairedBluetoothDevices.forEach { dev ->
                                val devName = try { dev.name ?: "TV" } catch (_: Throwable) { "TV" }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceElevated)
                                        .clickable { onConnectBluetooth(dev) }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = devName,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = "Connect",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Put TV in 'Add accessory' mode, then tap eye icon to make phone discoverable.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Clean & Large Trackpad Touch Surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TrackpadSurface)
                .border(
                    width = 1.dp,
                    color = if (isBtConnected) EmeraldConnected.copy(alpha = 0.4f) else ButtonBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("trackpad_touch_surface")
                .pointerInput(uiState.cursorSensitivity, isBtConnected) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onMove(dragAmount.x, dragAmount.y, context)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick(false, context) },
                        onLongPress = { onClick(true, context) }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 45.dp.toPx()
                var x = step
                while (x < size.width) {
                    drawLine(
                        color = TrackpadGrid.copy(alpha = 0.4f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    x += step
                }
                var y = step
                while (y < size.height) {
                    drawLine(
                        color = TrackpadGrid.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    y += step
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = null,
                    tint = if (isBtConnected) EmeraldConnected.copy(alpha = 0.7f) else TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Scroll Bar Strip (Icon Only)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .width(36.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.85f))
                    .border(1.dp, ButtonBorder, RoundedCornerShape(18.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onScroll(dragAmount.y, context)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Scroll",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tactile Left & Right Mouse Click Buttons (Icon Only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TactileButton(
                onClick = { onClick(false, context) },
                icon = Icons.Default.TouchApp,
                backgroundColor = DarkSurfaceCard,
                borderColor = if (isBtConnected) EmeraldConnected.copy(alpha = 0.5f) else ButtonBorder,
                contentColor = if (isBtConnected) EmeraldConnected else TextPrimary,
                shape = RoundedCornerShape(14.dp),
                minSize = 48.dp,
                modifier = Modifier.weight(1.3f),
                testTag = "mouse_left_click"
            )

            TactileButton(
                onClick = { onClick(true, context) },
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                backgroundColor = DarkSurfaceCard,
                borderColor = ButtonBorder,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(14.dp),
                minSize = 48.dp,
                modifier = Modifier.weight(1f),
                testTag = "mouse_right_click"
            )
        }

        // Navigation Controls on Mouse Screen (Icon Only)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Back, Up, Home, Vol -, Vol + (Icons Only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.BACK, context) },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_back"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.UP, context) },
                        icon = Icons.Default.ArrowUpward,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_up"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.HOME, context) },
                        icon = Icons.Default.Home,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_home"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.VOLUME_DOWN, context) },
                        icon = Icons.AutoMirrored.Filled.VolumeDown,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_voldown"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.VOLUME_UP, context) },
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_volup"
                    )
                }

                // Row 2: Left, Down, Right, OK (Icons Only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.LEFT, context) },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_left"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.DOWN, context) },
                        icon = Icons.Default.ArrowDownward,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_down"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.RIGHT, context) },
                        icon = Icons.Default.ArrowForward,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "mouse_nav_right"
                    )

                    TactileButton(
                        onClick = { onSendCommand(RemoteCommand.CONFIRM, context) },
                        icon = Icons.Default.Check,
                        backgroundColor = ButtonBackground,
                        borderColor = ButtonBorder,
                        contentColor = TextPrimary,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f),
                        testTag = "mouse_nav_ok"
                    )
                }
            }
        }

        // Pointer Speed Slider (Icon & Value Only)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Mouse,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Slider(
                value = uiState.cursorSensitivity.coerceIn(0.5f, 2.5f),
                onValueChange = onSensitivityChange,
                valueRange = 0.5f..2.5f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = TextSecondary,
                    inactiveTrackColor = ButtonBackground
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format(java.util.Locale.US, "%.1fx", uiState.cursorSensitivity),
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(36.dp)
                    .padding(start = 6.dp)
            )
        }
    }
}

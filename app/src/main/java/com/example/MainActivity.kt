package com.example

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ConnectionStatus
import com.example.ui.components.HelpGuideDialog
import com.example.ui.screens.DeviceSettingsScreen
import com.example.ui.screens.InputsAndAppsScreen
import com.example.ui.screens.KeyboardSearchScreen
import com.example.ui.screens.RemoteScreen
import com.example.ui.screens.TrackpadMouseScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PowerRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TvRemoteViewModel

enum class RemoteTab(val title: String, val icon: ImageVector) {
    REMOTE("Remote", Icons.Default.Tv),
    MOUSE("Mouse", Icons.Default.Mouse),
    KEYBOARD("Keyboard", Icons.Default.Keyboard),
    INPUTS("Inputs", Icons.Default.Input),
    SETTINGS("TV Setup", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TvRemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BraviaRemoteApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BraviaRemoteApp(viewModel: TvRemoteViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedTvs by viewModel.savedTvs.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(RemoteTab.REMOTE) }

    // Request Bluetooth permissions gracefully on Android 12+
    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true) {
            viewModel.refreshBluetoothDevices()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    btPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE
                        )
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    // Discoverable launcher anchored to Activity result lifecycle (Prevents popup freeze)
    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val granted = result.resultCode != Activity.RESULT_CANCELED
        viewModel.onDiscoverableResult(granted)
    }

    val triggerMakeDiscoverable: () -> Unit = {
        try {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180)
            }
            discoverableLauncher.launch(intent)
        } catch (_: Throwable) {
            viewModel.onDiscoverableResult(false)
        }
    }

    // Support Android system back button to return to Remote tab
    BackHandler(enabled = currentTab != RemoteTab.REMOTE) {
        currentTab = RemoteTab.REMOTE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopTvStatusBar(
                tvName = uiState.currentTv.name,
                status = uiState.connectionStatus,
                onHeaderClick = { currentTab = RemoteTab.SETTINGS },
                onHelpClick = { viewModel.setHelpDialogOpen(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(0.5.dp, ButtonBorder.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                RemoteTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            currentTab = tab
                            viewModel.onTabSelected(tab)
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = TextPrimary,
                            indicatorColor = TextPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                RemoteTab.REMOTE -> {
                    RemoteScreen(
                        uiState = uiState,
                        onSendCommand = { cmd, ctx -> viewModel.sendCommand(cmd, ctx, fromMouseScreen = false) },
                        onSetVolume = viewModel::setVolume,
                        onNavigateToInputs = { currentTab = RemoteTab.INPUTS },
                        context = context
                    )
                }
                RemoteTab.MOUSE -> {
                    TrackpadMouseScreen(
                        uiState = uiState,
                        onMove = viewModel::onTrackpadMove,
                        onClick = viewModel::onTrackpadClick,
                        onScroll = viewModel::onTrackpadScroll,
                        onSensitivityChange = viewModel::setSensitivity,
                        onSendCommand = { cmd, ctx -> viewModel.sendCommand(cmd, ctx, fromMouseScreen = true) },
                        onConnectBluetooth = viewModel::connectBluetoothDevice,
                        onDisconnectBluetooth = viewModel::disconnectBluetooth,
                        onMakeDiscoverable = triggerMakeDiscoverable,
                        onOpenHelp = { viewModel.setHelpDialogOpen(true) },
                        context = context
                    )
                }
                RemoteTab.KEYBOARD -> {
                    KeyboardSearchScreen(
                        uiState = uiState,
                        onInputChange = viewModel::updateKeyboardInput,
                        onSendText = viewModel::sendVirtualText,
                        onSendSpecialKey = viewModel::sendKeyboardSpecialKey,
                        onTriggerTvSearch = viewModel::triggerTvSearch,
                        onSendCommand = viewModel::sendCommand,
                        context = context
                    )
                }
                RemoteTab.INPUTS -> {
                    InputsAndAppsScreen(
                        uiState = uiState,
                        onSwitchInput = viewModel::switchInput,
                        onSendCommand = viewModel::sendCommand,
                        context = context
                    )
                }
                RemoteTab.SETTINGS -> {
                    DeviceSettingsScreen(
                        uiState = uiState,
                        savedTvs = savedTvs,
                        onSelectTv = viewModel::selectTv,
                        onSaveTv = viewModel::saveTv,
                        onDeleteTv = viewModel::deleteTv,
                        onStartScan = viewModel::startScan,
                        onTestConnection = viewModel::testCurrentConnection,
                        context = context
                    )
                }
            }

            // Quick Status Toast overlay
            AnimatedVisibility(
                visible = uiState.statusMessage != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.95f))
                        .border(1.dp, ButtonBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TextPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.statusMessage ?: "",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Help Guide Dialog
            if (uiState.isHelpDialogOpen) {
                HelpGuideDialog(
                    onDismiss = { viewModel.setHelpDialogOpen(false) },
                    onMakeDiscoverable = triggerMakeDiscoverable
                )
            }
        }
    }
}

/**
 * Clean & Authentic Sony Bravia Status Bar
 */
@Composable
fun TopTvStatusBar(
    tvName: String,
    status: ConnectionStatus,
    onHeaderClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // TV Title & Connection status dot (clickable to switch TV)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onHeaderClick)
                .padding(vertical = 4.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "TV",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = tvName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (status) {
                                    ConnectionStatus.CONNECTED -> EmeraldConnected
                                    ConnectionStatus.CONNECTING -> AmberWarning
                                    else -> PowerRed
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when (status) {
                            ConnectionStatus.CONNECTED -> "Connected"
                            ConnectionStatus.CONNECTING -> "Connecting..."
                            else -> "Offline"
                        },
                        color = when (status) {
                            ConnectionStatus.CONNECTED -> EmeraldConnected
                            ConnectionStatus.CONNECTING -> AmberWarning
                            else -> PowerRed
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Help Icon button
        IconButton(
            onClick = onHelpClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(DarkSurfaceCard)
                .size(38.dp)
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

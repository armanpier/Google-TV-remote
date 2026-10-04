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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.DiscoveredTv
import com.example.data.model.TvDevice
import com.example.ui.components.TactileButton
import com.example.ui.theme.AmberWarning
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

@Composable
fun DeviceSettingsScreen(
    uiState: RemoteUiState,
    savedTvs: List<TvDevice>,
    onSelectTv: (TvDevice, Context) -> Unit,
    onSaveTv: (TvDevice) -> Unit,
    onDeleteTv: (TvDevice) -> Unit,
    onStartScan: () -> Unit,
    onTestConnection: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TV Connection & Settings",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sony Bravia IP Control & Google TV setup",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            TactileButton(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                label = "Add TV",
                backgroundColor = BraviaBlue,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                testTag = "add_new_tv_button"
            )
        }

        // Active TV Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, BraviaBlue, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BraviaBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Active TV",
                                tint = BraviaBlueGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = uiState.currentTv.name,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.currentTv.modelName,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Connection Status Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (uiState.connectionStatus) {
                                    ConnectionStatus.CONNECTED -> EmeraldConnected.copy(alpha = 0.2f)
                                    ConnectionStatus.CONNECTING -> AmberWarning.copy(alpha = 0.2f)
                                    else -> PowerRed.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (uiState.connectionStatus) {
                                        ConnectionStatus.CONNECTED -> EmeraldConnected
                                        ConnectionStatus.CONNECTING -> AmberWarning
                                        else -> PowerRed
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (uiState.connectionStatus) {
                                ConnectionStatus.CONNECTED -> "CONNECTED"
                                ConnectionStatus.CONNECTING -> "LINKING..."
                                else -> "OFFLINE"
                            },
                            color = when (uiState.connectionStatus) {
                                ConnectionStatus.CONNECTED -> EmeraldConnected
                                ConnectionStatus.CONNECTING -> AmberWarning
                                else -> PowerRed
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Simple Test Connection Button
                TactileButton(
                    onClick = onTestConnection,
                    icon = Icons.Default.NetworkCheck,
                    label = "Test Connection",
                    backgroundColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "test_connection_btn"
                )
            }
        }

        // Section: Network Auto-Discovery Scanner (NSD / mDNS)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = "Router",
                                tint = BraviaBlueGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NSD / mDNS Network Discovery",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Auto-scans _googlecast._tcp & Sony Bravia mDNS",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (uiState.isScanning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = BraviaBlueGlow,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Scanning...",
                                color = BraviaBlueGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        TactileButton(
                            onClick = onStartScan,
                            icon = Icons.Default.Refresh,
                            label = "Scan",
                            backgroundColor = DarkSurfaceElevated,
                            contentColor = TextPrimary,
                            shape = RoundedCornerShape(12.dp),
                            testTag = "scan_network_btn"
                        )
                    }
                }

                if (uiState.discoveredTvs.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.discoveredTvs.forEach { discovered ->
                            val isCurrent = discovered.ipAddress == uiState.currentTv.ipAddress
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isCurrent) DarkSurfaceElevated else DarkBackground)
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = if (isCurrent) BraviaBlue else ButtonBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        val newTv = TvDevice(
                                            name = discovered.name,
                                            ipAddress = discovered.ipAddress,
                                            port = 80,
                                            psk = "0000",
                                            modelName = discovered.model,
                                            is4K = true
                                        )
                                        onSaveTv(newTv)
                                        onSelectTv(newTv, context)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (discovered.isSonyBravia) BraviaBlue.copy(alpha = 0.2f)
                                                else ElectricIndigo.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = "TV",
                                            tint = if (discovered.isSonyBravia) BraviaBlueGlow else Color(0xFFC7D2FE),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = discovered.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (discovered.isSonyBravia) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(BraviaBlue.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "BRAVIA",
                                                        color = BraviaBlueGlow,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${discovered.ipAddress} • ${discovered.model}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = discovered.discoverySource,
                                            color = TextMuted,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (isCurrent) {
                                        Text(
                                            text = "ACTIVE",
                                            color = EmeraldConnected,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "Connect",
                                            color = BraviaBlueGlow,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (!uiState.isScanning) {
                    Text(
                        text = "No TV devices detected yet. Tap 'Scan' to broadcast mDNS discovery on your Wi-Fi network.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Section: Saved TVs List
        if (savedTvs.isNotEmpty()) {
            Text(
                text = "Saved TV Profiles",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                savedTvs.forEach { tv ->
                    val isCurrent = tv.id == uiState.currentTv.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) DarkSurfaceElevated else DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) BraviaBlue else ButtonBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectTv(tv, context) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = tv.name,
                                    color = if (isCurrent) BraviaBlueGlow else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${tv.ipAddress} • ${if (tv.is4K) "4K UHD" else "1080p"}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = EmeraldConnected,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteTv(tv) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Step-by-Step Sony Bravia TV Setup Guide
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = BraviaBlueGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to Enable IP Control on Sony Bravia",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val steps = listOf(
                    "1. On your Sony TV remote, press Settings [⚙] or Quick Settings.",
                    "2. Navigate to [Network & Internet] -> [Home Network] -> [IP Control].",
                    "3. Turn ON [Simple IP Control] (this enables instant LAN command response).",
                    "4. Set [Authentication] to [Pre-Shared Key] and type '0000'.",
                    "5. Check TV IP address under [Network Status] and enter it above."
                )

                steps.forEach { step ->
                    Text(
                        text = step,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Add TV Dialog
    if (showAddDialog) {
        var tvName by remember { mutableStateOf("Living Room Bravia") }
        var ipAddress by remember { mutableStateOf("192.168.1.100") }
        var psk by remember { mutableStateOf("0000") }
        var is4K by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Text(text = "Add Sony Bravia / Google TV", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tvName,
                        onValueChange = { tvName = it },
                        label = { Text("TV Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BraviaBlue,
                            unfocusedBorderColor = ButtonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("TV IP Address (e.g. 192.168.1.105)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BraviaBlue,
                            unfocusedBorderColor = ButtonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = psk,
                        onValueChange = { psk = it },
                        label = { Text("Pre-Shared Key (default 0000)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BraviaBlue,
                            unfocusedBorderColor = ButtonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "4K Ultra HD Model", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = is4K,
                            onCheckedChange = { is4K = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BraviaBlueGlow,
                                checkedTrackColor = BraviaBlue.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newTv = TvDevice(
                            name = tvName.ifBlank { "Sony Bravia TV" },
                            ipAddress = ipAddress.ifBlank { "192.168.1.100" },
                            psk = psk.ifBlank { "0000" },
                            is4K = is4K,
                            resolutionWidth = if (is4K) 3840 else 1920,
                            resolutionHeight = if (is4K) 2160 else 1080
                        )
                        onSaveTv(newTv)
                        onSelectTv(newTv, context)
                        showAddDialog = false
                    }
                ) {
                    Text("Save & Connect", color = BraviaBlueGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

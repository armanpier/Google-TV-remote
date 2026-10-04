package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.bluetooth.BluetoothHidState
import com.example.data.model.RemoteCommand
import com.example.ui.components.TactileButton
import com.example.ui.theme.BraviaBlue
import com.example.ui.theme.BraviaBlueGlow
import com.example.ui.theme.ButtonBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RemoteUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeyboardSearchScreen(
    uiState: RemoteUiState,
    onInputChange: (String) -> Unit,
    onSendText: (String, Context) -> Unit,
    onSendSpecialKey: (Byte, Context) -> Unit,
    onTriggerTvSearch: (Context) -> Unit,
    onSendCommand: (RemoteCommand, Context) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isBtConnected = uiState.bluetoothState == BluetoothHidState.CONNECTED

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
                    text = "Virtual Keyboard & Search",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBtConnected) "Bluetooth HID Keyboard Active • Real Keystrokes"
                    else "Transmit text directly to Sony Bravia & Google TV",
                    color = if (isBtConnected) EmeraldConnected else TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Quick TV Search Trigger
            TactileButton(
                onClick = { onTriggerTvSearch(context) },
                icon = Icons.Default.Search,
                label = "TV Search",
                backgroundColor = BraviaBlue.copy(alpha = 0.2f),
                borderColor = BraviaBlue,
                contentColor = BraviaBlueGlow,
                shape = RoundedCornerShape(18.dp),
                testTag = "trigger_tv_search"
            )
        }

        // Bluetooth HID Active Badge
        if (isBtConnected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldConnected.copy(alpha = 0.15f))
                    .border(1.dp, EmeraldConnected, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothConnected,
                    contentDescription = "Bluetooth Connected",
                    tint = EmeraldConnected,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Phone is paired as real Bluetooth Keyboard to ${uiState.bluetoothDeviceName ?: "TV"}",
                    color = EmeraldConnected,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Text Input Field with Clear & Send Buttons
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.keyboardInput,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("virtual_keyboard_input_field"),
                    placeholder = {
                        Text(
                            text = "Type movie, show, YouTube video, or URL...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Keyboard",
                            tint = if (isBtConnected) EmeraldConnected else BraviaBlueGlow
                        )
                    },
                    trailingIcon = {
                        if (uiState.keyboardInput.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear text",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .clickable { onInputChange("") }
                                    .size(20.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isBtConnected) EmeraldConnected else BraviaBlue,
                        unfocusedBorderColor = ButtonBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (uiState.keyboardInput.isNotBlank()) {
                                onSendText(uiState.keyboardInput, context)
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                // Virtual Remote Control Action Keys Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TactileButton(
                        onClick = {
                            if (uiState.keyboardInput.isNotBlank()) {
                                onSendText(uiState.keyboardInput, context)
                                keyboardController?.hide()
                            }
                        },
                        icon = Icons.Default.Send,
                        label = "Send",
                        backgroundColor = if (isBtConnected) EmeraldConnected else BraviaBlue,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f),
                        testTag = "send_text_to_tv"
                    )

                    TactileButton(
                        onClick = { onSendSpecialKey(0x28.toByte(), context) }, // Enter
                        icon = Icons.Default.KeyboardReturn,
                        label = "Enter",
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "keyboard_enter_key"
                    )

                    TactileButton(
                        onClick = {
                            onInputChange(uiState.keyboardInput + " ")
                            if (isBtConnected) {
                                onSendSpecialKey(0x2C.toByte(), context) // Space
                            }
                        },
                        icon = Icons.Default.SpaceBar,
                        label = "Space",
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        testTag = "keyboard_space_key"
                    )

                    TactileButton(
                        onClick = {
                            if (uiState.keyboardInput.isNotEmpty()) {
                                onInputChange(uiState.keyboardInput.dropLast(1))
                            }
                            onSendSpecialKey(0x2A.toByte(), context) // Backspace
                        },
                        icon = Icons.AutoMirrored.Filled.Backspace,
                        backgroundColor = DarkSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        minSize = 48.dp,
                        testTag = "keyboard_backspace_key"
                    )
                }

                // Paste from Android Clipboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TactileButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clipText.isNullOrBlank()) {
                                onInputChange(clipText)
                            }
                        },
                        icon = Icons.Default.ContentPaste,
                        label = "Paste Clipboard",
                        backgroundColor = Color.Transparent,
                        borderColor = ButtonBorder,
                        contentColor = TextSecondary,
                        shape = RoundedCornerShape(10.dp),
                        minSize = 36.dp,
                        testTag = "paste_clipboard_button"
                    )
                }
            }
        }

        // Quick Preset Search Queries
        Text(
            text = "Popular 4K & Google TV Searches",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        val presetQueries = listOf(
            "4K HDR OLED Demo",
            "Stranger Things",
            "YouTube 4K 60fps",
            "Formula 1 4K",
            "Interstellar 4K",
            "Lo-Fi Chill Beats",
            "BBC Earth 4K",
            "Twitch Live Stream"
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetQueries.forEach { query ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceCard)
                        .clickable {
                            onInputChange(query)
                            onSendText(query, context)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BraviaBlueGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = query,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Recent Searches
        if (uiState.recentSearches.isNotEmpty()) {
            Text(
                text = "Recent Searches",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.recentSearches.forEach { search ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceElevated)
                            .clickable {
                                onInputChange(search)
                                onSendText(search, context)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = search,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Information Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Info",
                    tint = BraviaBlueGlow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tip: When typing into any search bar, browser, or login field on your Sony Bravia / Google TV, tap 'Send' to inject text with zero input lag.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

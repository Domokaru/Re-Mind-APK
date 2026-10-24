package com.hallauer.remind.ui.screens.phrases

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hallauer.remind.core.util.LoveGuardrailValidator
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.PhraseEntity
import com.hallauer.remind.ui.theme.toColor

@Composable
fun PhrasesScreen(
    phrases: List<PhraseEntity>,
    onSavePhrase: (PhraseEntity) -> Unit = {},
    onDeletePhrase: (PhraseEntity) -> Unit = {}
) {
    var selectedBucket by remember { mutableStateOf(GroupBucket.SPOUSE) }
    var editingPhrase by remember { mutableStateOf<PhraseEntity?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    val bucketPhrases = phrases.filter { it.bucket == selectedBucket }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 5 Group Bucket Tab Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(GroupBucket.values()) { bucket ->
                    val isSelected = selectedBucket == bucket
                    val count = phrases.count { it.bucket == bucket }
                    val bucketColor = bucket.toColor(isDarkTheme = true)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) bucketColor.copy(alpha = 0.25f) else Color(0xFF1E293B))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) bucketColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedBucket = bucket }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${bucket.displayName} ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) bucketColor else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phrase Pool Feed
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bucketPhrases, key = { it.id }) { phrase ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "\"${phrase.text}\"",
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (phrase.isSeed) "Preloaded Seed Phrase" else "Custom Phrase",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Row {
                                    IconButton(onClick = { editingPhrase = phrase }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Phrase", tint = Color(0xFF94A3B8))
                                    }
                                    IconButton(onClick = { onDeletePhrase(phrase) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Phrase", tint = Color(0xFFF87171))
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }

        // Add Phrase FAB
        FloatingActionButton(
            onClick = { isAddingNew = true },
            containerColor = Color(0xFF08DAC9),
            contentColor = Color(0xFF04201D),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Phrase")
        }
    }

    if (isAddingNew) {
        PhraseDialog(
            initialText = "",
            bucket = selectedBucket,
            title = "Add Phrase to ${selectedBucket.displayName}",
            onDismiss = { isAddingNew = false },
            onConfirm = { text ->
                onSavePhrase(PhraseEntity(bucket = selectedBucket, text = text))
                isAddingNew = false
            }
        )
    }

    editingPhrase?.let { phrase ->
        PhraseDialog(
            initialText = phrase.text,
            bucket = phrase.bucket,
            title = "Edit Phrase",
            onDismiss = { editingPhrase = null },
            onConfirm = { text ->
                onSavePhrase(phrase.copy(text = text))
                editingPhrase = null
            }
        )
    }
}

@Composable
fun PhraseDialog(
    initialText: String,
    bucket: GroupBucket,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var guardrailError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        val validation = LoveGuardrailValidator.validate(bucket, it)
                        guardrailError = if (validation is LoveGuardrailValidator.ValidationResult.Invalid) {
                            validation.errorMessage
                        } else null
                    },
                    label = { Text("Phrase Text", color = Color(0xFF94A3B8)) },
                    isError = guardrailError != null,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                if (guardrailError != null) {
                    Text(
                        text = guardrailError!!,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim()) },
                enabled = guardrailError == null && text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF08DAC9))
            ) {
                Text("Save", color = Color(0xFF04201D), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

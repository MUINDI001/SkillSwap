package com.skillswap.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SkillChip(skill: String) {
    AssistChip(
        onClick = { /* TODO: Filter */ },
        label = { Text(skill) }
    )
}

@Preview(showBackground = true)
@Composable
fun SkillChipPreview() {
    SkillChip(skill = "Kotlin")
}
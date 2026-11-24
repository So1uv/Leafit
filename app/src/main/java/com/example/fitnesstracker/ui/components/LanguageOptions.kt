package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.AppLanguage

@Composable
fun languageName(language: AppLanguage): String = stringResource(when (language) {
    AppLanguage.UKRAINIAN -> R.string.settings_language_uk
    AppLanguage.RUSSIAN -> R.string.settings_language_ru
    AppLanguage.ENGLISH -> R.string.settings_language_en
    AppLanguage.CHINESE -> R.string.settings_language_zh
    AppLanguage.CHINESE_TRADITIONAL -> R.string.settings_language_zh_tw
})

@Composable
fun LanguageFlag(language: AppLanguage, modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = 48.dp, height = 32.dp).clip(RoundedCornerShape(9.dp))) {
        val white = androidx.compose.ui.graphics.Color(0xFFFFFCF6)
        when (language) {
            AppLanguage.UKRAINIAN -> {
                drawRect(androidx.compose.ui.graphics.Color(0xFF2872C5))
                drawRect(androidx.compose.ui.graphics.Color(0xFFF6D15C),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height / 2))
            }
            AppLanguage.RUSSIAN -> {
                drawRect(white)
                drawRect(androidx.compose.ui.graphics.Color(0xFF365EB4),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height / 3),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height / 3))
                drawRect(androidx.compose.ui.graphics.Color(0xFFC65056),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 2 / 3),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height / 3))
            }
            AppLanguage.ENGLISH -> {
                drawRect(white)
                val stripe = size.height / 13
                repeat(7) { i -> drawRect(androidx.compose.ui.graphics.Color(0xFFB94F5B),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, stripe * i * 2),
                    size = androidx.compose.ui.geometry.Size(size.width, stripe)) }
                val cantonW = size.width * .45f
                val cantonH = stripe * 7
                drawRect(androidx.compose.ui.graphics.Color(0xFF344D80), size = androidx.compose.ui.geometry.Size(cantonW, cantonH))
                repeat(9) { row ->
                    val count = if (row % 2 == 0) 6 else 5
                    repeat(count) { column ->
                        val cx = cantonW * (column + if (count == 6) .5f else 1f) / 6f
                        val cy = cantonH * (row + .5f) / 9f
                        val radius = cantonH / 23f
                        val star = androidx.compose.ui.graphics.Path()
                        repeat(10) { point ->
                            val angle = -kotlin.math.PI / 2 + point * kotlin.math.PI / 5
                            val r = if (point % 2 == 0) radius else radius * .42f
                            val x = cx + kotlin.math.cos(angle).toFloat() * r
                            val y = cy + kotlin.math.sin(angle).toFloat() * r
                            if (point == 0) star.moveTo(x, y) else star.lineTo(x, y)
                        }
                        star.close(); drawPath(star, white)
                    }
                }
            }
            AppLanguage.CHINESE, AppLanguage.CHINESE_TRADITIONAL -> {
                val red = androidx.compose.ui.graphics.Color(0xFFDE2910)
                val yellow = androidx.compose.ui.graphics.Color(0xFFFFDE00)
                drawRect(red)
                fun star(cx: Float, cy: Float, r: Float) {
                    val path = androidx.compose.ui.graphics.Path()
                    repeat(5) { point ->
                        val angle = -kotlin.math.PI / 2 + point * 2 * kotlin.math.PI / 5
                        val x = cx + kotlin.math.cos(angle).toFloat() * r
                        val y = cy + kotlin.math.sin(angle).toFloat() * r
                        if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close(); drawPath(path, yellow)
                }
                val unit = size.height / 12f
                star(size.width * .2f, size.height * .26f, unit * 1.6f)
                star(size.width * .38f, size.height * .12f, unit * .6f)
                star(size.width * .5f, size.height * .25f, unit * .6f)
                star(size.width * .5f, size.height * .44f, unit * .6f)
                star(size.width * .38f, size.height * .55f, unit * .6f)
            }
        }
    }
}

@Composable
fun LanguageOptions(current: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val languages = listOf(AppLanguage.UKRAINIAN, AppLanguage.RUSSIAN, AppLanguage.ENGLISH, AppLanguage.CHINESE, AppLanguage.CHINESE_TRADITIONAL)
    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        languages.forEachIndexed { index, language ->
            val selected = current == language
            val interactions = remember { MutableInteractionSource() }
            val tone by animateColorAsState(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                spring(1f, 500f), label = "languageTone")
            val innerCorner by animateDpAsState(if (selected) 22.dp else 10.dp, spring(.9f, 500f), label = "languageShape")
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(
                topStart = if (index == 0) 28.dp else innerCorner, topEnd = if (index == 0) 28.dp else innerCorner,
                bottomStart = if (index == languages.lastIndex) 28.dp else innerCorner, bottomEnd = if (index == languages.lastIndex) 28.dp else innerCorner))
                .background(tone)
                .selectable(selected, role = Role.RadioButton, interactionSource = interactions, indication = null, onClick = { if (!selected) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onSelect(language) } })
                .heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LanguageFlag(language)
                Text(languageName(language), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))

            }
        }
    }
}

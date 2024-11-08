/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.core.ui.widget.enhanced

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import ru.tech.imageresizershrinker.core.settings.domain.model.SliderType
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.shapes.MaterialStarShape
import ru.tech.imageresizershrinker.core.ui.widget.sliders.FancySlider
import ru.tech.imageresizershrinker.core.ui.widget.sliders.M2Slider
import ru.tech.imageresizershrinker.core.ui.widget.sliders.M3Slider

@Composable
fun EnhancedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
    colors: SliderColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val settingsState = LocalSettingsState.current
    val realColors = colors ?: when (settingsState.sliderType) {
        SliderType.Fancy -> {
            SliderDefaults.colors(
                activeTickColor = MaterialTheme.colorScheme.inverseSurface,
                inactiveTickColor = MaterialTheme.colorScheme.surface,
                activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                inactiveTrackColor = SwitchDefaults.colors().disabledCheckedTrackColor,
                disabledThumbColor = SwitchDefaults.colors().disabledCheckedThumbColor,
                thumbColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        SliderType.MaterialYou -> {
            SliderDefaults.colors()
        }

        SliderType.Material -> {
            SliderDefaults.colors()
        }
    }

    if (steps != 0) {
        var compositions by remember {
            mutableIntStateOf(0)
        }
        val haptics = LocalHapticFeedback.current
        val updatedValue by rememberUpdatedState(newValue = value)

        LaunchedEffect(updatedValue) {
            if (compositions > 0) {
                haptics.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )
            }
            compositions++
        }
    }

    AnimatedContent(
        targetState = settingsState.sliderType,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(false)
        }
    ) { animatedSliderType ->
        when (animatedSliderType) {
            SliderType.Fancy -> {
                FancySlider(
                    value = value,
                    enabled = enabled,
                    colors = realColors,
                    interactionSource = interactionSource,
                    thumbShape = MaterialStarShape,
                    modifier = modifier,
                    onValueChange = onValueChange,
                    onValueChangeFinished = onValueChangeFinished,
                    valueRange = valueRange,
                    steps = steps
                )
            }

            SliderType.MaterialYou -> {
                M3Slider(
                    value = value,
                    enabled = enabled,
                    colors = realColors,
                    interactionSource = interactionSource,
                    modifier = modifier,
                    onValueChange = onValueChange,
                    onValueChangeFinished = onValueChangeFinished,
                    valueRange = valueRange,
                    steps = steps
                )
            }

            SliderType.Material -> {
                M2Slider(
                    value = value,
                    enabled = enabled,
                    colors = realColors,
                    interactionSource = interactionSource,
                    modifier = modifier,
                    onValueChange = onValueChange,
                    onValueChangeFinished = onValueChangeFinished,
                    valueRange = valueRange,
                    steps = steps
                )
            }
        }
    }
}
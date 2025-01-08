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

package ru.tech.imageresizershrinker.color_tools.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Blender
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.colordetector.parser.ColorNameParser
import com.t8rin.dynamic.theme.ColorTuple
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.theme.inverse
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.copyToClipboard
import ru.tech.imageresizershrinker.core.ui.utils.helper.toHex
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ColorRowSelector
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedSliderItem
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.core.ui.widget.other.ExpandableItem
import ru.tech.imageresizershrinker.core.ui.widget.other.LocalToastHostState
import ru.tech.imageresizershrinker.core.ui.widget.saver.ColorSaver
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem
import kotlin.math.roundToInt

@Composable
internal fun ColorMixing(
    selectedColor: Color,
    appColorTuple: ColorTuple,
    parser: ColorNameParser
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val toastHostState = LocalToastHostState.current

    var mixingVariation by rememberSaveable {
        mutableIntStateOf(3)
    }
    var colorToMix by rememberSaveable(
        stateSaver = ColorSaver
    ) {
        mutableStateOf(appColorTuple.tertiary ?: Color.Companion.Yellow)
    }
    val mixedColors by remember(selectedColor, mixingVariation, colorToMix) {
        derivedStateOf {
            selectedColor.mixWith(
                color = colorToMix,
                variations = mixingVariation,
                maxPercent = 1f
            )
        }
    }

    ExpandableItem(
        visibleContent = {
            TitleItem(
                text = stringResource(R.string.color_mixing),
                icon = Icons.Rounded.Blender
            )
        },
        expandableContent = {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
            ) {
                ColorRowSelector(
                    value = colorToMix,
                    onValueChange = { colorToMix = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .container(
                            color = MaterialTheme.colorScheme.surface,
                            shape = ContainerShapeDefaults.topShape
                        ),
                    title = stringResource(R.string.color_to_mix)
                )
                Spacer(modifier = Modifier.height(4.dp))
                EnhancedSliderItem(
                    value = mixingVariation,
                    title = stringResource(R.string.variation),
                    valueRange = 2f..15f,
                    onValueChange = { mixingVariation = it.roundToInt() },
                    internalStateTransformation = { it.roundToInt() },
                    shape = ContainerShapeDefaults.bottomShape,
                    behaveAsContainer = true,
                    color = MaterialTheme.colorScheme.surface,
                    steps = 12
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    mixedColors.forEachIndexed { index, color ->
                        val boxColor by animateColorAsState(color)
                        val contentColor = boxColor.inverse(
                            fraction = { cond ->
                                if (cond) 0.8f
                                else 0.5f
                            },
                            darkMode = boxColor.luminance() < 0.3f
                        )
                        Box(
                            modifier = Modifier
                                .heightIn(min = 100.dp)
                                .fillMaxWidth()
                                .clip(
                                    ContainerShapeDefaults.shapeForIndex(
                                        index = index,
                                        size = mixedColors.size
                                    )
                                )
                                .transparencyChecker()
                                .background(boxColor)
                                .clickable {
                                    context.copyToClipboard(
                                        label = context.getString(R.string.color),
                                        value = getFormattedColor(color)
                                    )
                                    scope.launch {
                                        toastHostState.showToast(
                                            icon = Icons.Rounded.ContentPaste,
                                            message = context.getString(R.string.color_copied)
                                        )
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = stringResource(R.string.copy),
                                tint = contentColor,
                                modifier = Modifier
                                    .align(Alignment.Companion.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(
                                        color = boxColor.copy(alpha = 1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(2.dp)
                            )

                            Text(
                                text = color.toHex(),
                                color = contentColor,
                                modifier = Modifier
                                    .align(Alignment.Companion.BottomStart)
                                    .padding(4.dp)
                                    .background(
                                        color = boxColor.copy(alpha = 1f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .padding(horizontal = 4.dp),
                                fontSize = 12.sp
                            )

                            Text(
                                text = remember(color) {
                                    derivedStateOf {
                                        parser.parseColorName(color)
                                    }
                                }.value,
                                color = contentColor,
                                modifier = Modifier
                                    .align(Alignment.Companion.BottomEnd)
                                    .padding(4.dp)
                                    .background(
                                        color = boxColor.copy(alpha = 1f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .padding(horizontal = 4.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        initialState = false
    )
}
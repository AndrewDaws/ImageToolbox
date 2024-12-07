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

package ru.tech.imageresizershrinker.feature.root.presentation.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.detectPointerTransformGestures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.ui.theme.blend
import ru.tech.imageresizershrinker.core.ui.theme.takeColorFromScheme
import ru.tech.imageresizershrinker.core.ui.utils.animation.FancyTransitionEasing
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalSheetDragHandle
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.toShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.withLayoutCorners
import ru.tech.imageresizershrinker.feature.settings.presentation.SettingsContent
import ru.tech.imageresizershrinker.feature.settings.presentation.screenLogic.SettingsComponent
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal fun SettingsBackdropWrapper(
    currentScreen: Screen?,
    concealBackdropFlow: Flow<Boolean>,
    settingsComponent: SettingsComponent,
    children: @Composable BoxScope.() -> Unit
) {
    var shape by remember { mutableStateOf<RoundedCornerShape>(RoundedCornerShape(0.dp)) }
    val scaffoldState = rememberBackdropScaffoldState(
        initialValue = BackdropValue.Concealed,
        animationSpec = tween(
            durationMillis = 400,
            easing = FancyTransitionEasing
        )
    )
    val canExpandSettings =
        (currentScreen?.id ?: -1) >= 0 //TODO: && settingsComponent.settingsState.addFastSettings

    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }
    val animatedPredictiveBackProgress by animateFloatAsState(predictiveBackProgress)

    val clean = {
        predictiveBackProgress = 0f
    }

    LaunchedEffect(canExpandSettings) {
        if (!canExpandSettings) {
            clean()
            scaffoldState.conceal()
        }
    }

    LaunchedEffect(concealBackdropFlow) {
        concealBackdropFlow
            .debounce(200)
            .collectLatest {
                if (it) {
                    clean()
                    scaffoldState.conceal()
                }
            }
    }

    val scope = rememberCoroutineScope()

    BackdropScaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.withLayoutCorners {
            shape = it.toShape(1f)
            this
        },
        appBar = {},
        frontLayerContent = {
            val alpha by animateFloatAsState(
                if (scaffoldState.targetValue == BackdropValue.Revealed) 1f else 0f
            )
            val color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha / 2f)
            var isWantOpenSettings by remember {
                mutableStateOf(false)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(color)
                    }
            ) {
                Box(
                    modifier = Modifier.pointerInput(isWantOpenSettings) {
                        detectPointerTransformGestures(
                            consume = false,
                            onGestureEnd = {},
                            onGestureStart = {
                                isWantOpenSettings = false
                            },
                            onGesture = { _, _, _, _, _, _ -> }
                        )
                    },
                    content = children
                )

                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(
                            height = 64.dp,
                            width = animateDpAsState(
                                if (isWantOpenSettings) 48.dp
                                else 24.dp
                            ).value
                        )
                        .clickable(
                            indication = null,
                            interactionSource = null
                        ) {
                            if (isWantOpenSettings) {
                                scope.launch {
                                    scaffoldState.reveal()
                                    isWantOpenSettings = false
                                }
                            } else {
                                isWantOpenSettings = true
                            }
                        }
                        .alpha(
                            animateFloatAsState(
                                if (canExpandSettings) 1f
                                else 0f
                            ).value
                        )
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(
                                    animateDpAsState(
                                        if (isWantOpenSettings) 48.dp
                                        else 4.dp
                                    ).value
                                )
                                .height(64.dp)
                                .container(
                                    shape = RoundedCornerShape(
                                        topStart = 8.dp,
                                        bottomStart = 8.dp
                                    ),
                                    resultPadding = 0.dp,
                                    color = takeColorFromScheme {
                                        tertiary.blend(primary, 0.8f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedVisibility(
                                visible = isWantOpenSettings,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = takeColorFromScheme {
                                        onTertiary.blend(onPrimary, 0.8f)
                                    }
                                )
                            }
                        }
                    }
                }

                EnhancedModalSheetDragHandle(
                    color = Color.Transparent,
                    drawStroke = false,
                    modifier = Modifier.alpha(alpha)
                )
            }
        },
        backLayerContent = {
            if (scaffoldState.targetValue == BackdropValue.Revealed) {
                PredictiveBackHandler { progress ->
                    try {
                        progress.collect { event ->
                            if (event.progress <= 0.05f) {
                                clean()
                            }
                            predictiveBackProgress = event.progress * 1.3f
                        }
                        scope.launch {
                            scaffoldState.conceal()
                        }
                        clean()
                    } catch (_: CancellationException) {
                        clean()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .alpha(1f - animatedPredictiveBackProgress)
            ) {
                SettingsContent(
                    component = settingsComponent
                )
            }
        },
        peekHeight = 0.dp,
        headerHeight = 70.dp,
        persistentAppBar = false,
        frontLayerElevation = 0.dp,
        backLayerBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        frontLayerBackgroundColor = MaterialTheme.colorScheme.surface,
        frontLayerScrimColor = Color.Transparent,
        frontLayerShape = shape,
        gesturesEnabled = scaffoldState.isRevealed
    )
}
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

package com.t8rin.imagetoolbox.core.ui.widget.sheets

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.domain.model.ExtraDataType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.getStringLocalized
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.image.UrisCarousel
import com.t8rin.imagetoolbox.core.ui.widget.preferences.ScreenPreference
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.RoundedTextField
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import com.t8rin.imagetoolbox.core.ui.widget.utils.screenList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProcessImagesPreferenceSheet(
    uris: List<Uri>,
    extraDataType: ExtraDataType? = null,
    visible: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val canSearchScreens = LocalSettingsState.current.screensSearchEnabled

    var isSearching by rememberSaveable {
        mutableStateOf(false)
    }
    var searchKeyword by rememberSaveable(isSearching) {
        mutableStateOf("")
    }

    val rawScreenList by uris.screenList(extraDataType)

    val urisCorrespondingScreens by remember(rawScreenList, searchKeyword, canSearchScreens) {
        derivedStateOf {
            if (searchKeyword.isNotEmpty() && canSearchScreens) {
                rawScreenList.filter {
                    val string =
                        context.getString(it.title) + " " + context.getString(it.subtitle)
                    val stringEn = context.getStringLocalized(it.title, Locale.ENGLISH)
                        .plus(" ")
                        .plus(context.getStringLocalized(it.subtitle, Locale.ENGLISH))
                    stringEn.contains(other = searchKeyword, ignoreCase = true).or(
                        string.contains(other = searchKeyword, ignoreCase = true)
                    )
                }
            } else rawScreenList
        }
    }

    EnhancedModalBottomSheet(
        title = {
            AnimatedContent(
                targetState = isSearching
            ) { searching ->
                if (searching) {
                    BackHandler {
                        searchKeyword = ""
                        isSearching = false
                    }
                    ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                        RoundedTextField(
                            maxLines = 1,
                            hint = { Text(stringResource(id = R.string.search_here)) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search,
                                autoCorrectEnabled = null
                            ),
                            value = searchKeyword,
                            onValueChange = {
                                searchKeyword = it
                            },
                            startIcon = {
                                EnhancedIconButton(
                                    onClick = {
                                        searchKeyword = ""
                                        isSearching = false
                                    },
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.exit),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            endIcon = {
                                AnimatedVisibility(
                                    visible = searchKeyword.isNotEmpty(),
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    EnhancedIconButton(
                                        onClick = {
                                            searchKeyword = ""
                                        },
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.close),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            },
                            shape = CircleShape
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TitleItem(
                            text = stringResource(R.string.image),
                            icon = Icons.Rounded.Image
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (canSearchScreens) {
                            EnhancedIconButton(
                                onClick = { isSearching = true },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ManageSearch,
                                    contentDescription = stringResource(R.string.search_here)
                                )
                            }
                        }
                        EnhancedButton(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = onDismiss
                        ) {
                            AutoSizeText(stringResource(R.string.close))
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        },
        sheetContent = {
            AnimatedContent(
                targetState = urisCorrespondingScreens.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { isNotEmpty ->
                if (isNotEmpty) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(250.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (extraDataType == null || extraDataType == ExtraDataType.Gif) {
                            item(
                                span = StaggeredGridItemSpan.FullLine
                            ) {
                                UrisCarousel(uris)
                            }
                        }
                        items(
                            items = urisCorrespondingScreens,
                            key = { it.toString() }
                        ) { screen ->
                            ScreenPreference(
                                screen = screen,
                                navigate = {
                                    scope.launch {
                                        onDismiss()
                                        delay(200)
                                        onNavigate(screen)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.nothing_found_by_search),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            modifier = Modifier
                                .weight(2f)
                                .sizeIn(maxHeight = 140.dp, maxWidth = 140.dp)
                                .fillMaxSize()
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {},
        enableBottomContentWeight = false,
        visible = visible,
        onDismiss = {
            if (!it) onDismiss()
        }
    )
}
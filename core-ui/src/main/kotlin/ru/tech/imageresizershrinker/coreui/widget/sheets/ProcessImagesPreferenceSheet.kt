package ru.tech.imageresizershrinker.coreui.widget.sheets

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.coreresources.R
import ru.tech.imageresizershrinker.coreui.theme.White
import ru.tech.imageresizershrinker.coreui.utils.navigation.LocalNavController
import ru.tech.imageresizershrinker.coreui.utils.navigation.NavController
import ru.tech.imageresizershrinker.coreui.utils.navigation.Screen
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedButton
import ru.tech.imageresizershrinker.coreui.widget.image.Picture
import ru.tech.imageresizershrinker.coreui.widget.modifier.container
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.BackgroundRemoverPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.BasicFilterPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.BytesResizePreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.CipherPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.ComparePreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.CropPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.DeleteExifPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.DrawPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.GeneratePalettePreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.ImagePreviewPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.ImageStitchingPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.ImagesToPdfPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.LimitsPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.MaskFilterPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.PdfToImagesPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.PickColorPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.PreviewPdfPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.ResizeAndConvertPreference
import ru.tech.imageresizershrinker.coreui.widget.preferences.screens.SingleEditPreference
import ru.tech.imageresizershrinker.coreui.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.coreui.widget.text.TitleItem

@Composable
fun ProcessImagesPreferenceSheet(
    uris: List<Uri>,
    hasPdf: Boolean = false,
    visible: MutableState<Boolean>,
    navController: NavController = LocalNavController.current,
    navigate: (Screen) -> Unit = { screen ->
        navController.apply {
            this.navigate(screen)
            visible.value = false
        }
    }
) {
    SimpleSheet(
        title = {
            TitleItem(
                text = stringResource(R.string.image),
                icon = Icons.Rounded.Image
            )
        },
        confirmButton = {
            EnhancedButton(
                containerColor = Color.Transparent,
                onClick = {
                    visible.value = false
                },
            ) {
                AutoSizeText(stringResource(id = R.string.cancel))
            }
        },
        sheetContent = {
            val color = MaterialTheme.colorScheme.secondaryContainer
            Box(Modifier.fillMaxWidth()) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(250.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(
                        span = StaggeredGridItemSpan.FullLine
                    ) {
                        val pic: @Composable (Uri?, Dp, Int) -> Unit =
                            { uri, height, extra ->
                                Box(
                                    Modifier
                                        .padding(
                                            bottom = 8.dp,
                                            start = 4.dp,
                                            end = 4.dp
                                        )
                                        .height(height)
                                        .width(120.dp)
                                        .container(
                                            shape = MaterialTheme.shapes.extraLarge,
                                            resultPadding = 0.dp
                                        )
                                ) {
                                    Picture(
                                        model = uri,
                                        shape = RectangleShape,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (extra > 0) {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.scrim.copy(
                                                        alpha = 0.6f
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+$extra",
                                                color = White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        if (!hasPdf) {
                            if (uris.size in 1..2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(uris.size) {
                                        pic(uris.getOrNull(it), 100.dp, 0)
                                    }
                                }
                            } else if (uris.size >= 3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    pic(uris.getOrNull(0), 100.dp, 0)
                                    Column {
                                        pic(uris.getOrNull(1), 60.dp, 0)
                                        pic(uris.getOrNull(2), 60.dp, uris.size - 3)
                                    }
                                }
                            }
                        }
                    }
                    if (hasPdf) {
                        item {
                            CipherPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.Cipher(uris.firstOrNull())) },
                                color = color
                            )
                        }
                        item {
                            PreviewPdfPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.PdfTools(
                                            Screen.PdfTools.Type.Preview(
                                                uris.firstOrNull()
                                            )
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            PdfToImagesPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.PdfTools(
                                            Screen.PdfTools.Type.PdfToImages(
                                                uris.firstOrNull()
                                            )
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                    } else if (uris.size <= 1) {
                        item {
                            SingleEditPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.SingleEdit(uris.firstOrNull())
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            ResizeAndConvertPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.ResizeAndConvert(uris)
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            BytesResizePreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.ResizeByBytes(uris)
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            CropPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.Crop(uris.firstOrNull())) },
                                color = color
                            )
                        }
                        item {
                            BasicFilterPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.Filter(
                                            type = Screen.Filter.Type.Basic(uris)
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            DrawPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.Draw(uris.firstOrNull())) },
                                color = color
                            )
                        }
                        item {
                            BackgroundRemoverPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.EraseBackground(uris.firstOrNull())) },
                                color = color
                            )
                        }
                        item {
                            MaskFilterPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.Filter(
                                            type = Screen.Filter.Type.Masking(uris.firstOrNull())
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            ImagesToPdfPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.PdfTools(
                                            Screen.PdfTools.Type.ImagesToPdf(
                                                uris
                                            )
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            CipherPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.Cipher(uris.firstOrNull())) },
                                color = color
                            )
                        }
                        item {
                            ImagePreviewPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.ImagePreview(uris)
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            PickColorPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.PickColorFromImage(uris.firstOrNull())
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            GeneratePalettePreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.GeneratePalette(uris.firstOrNull())
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            DeleteExifPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.DeleteExif(uris)) },
                                color = color
                            )
                        }
                        item {
                            LimitsPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.LimitResize(uris)) },
                                color = color
                            )
                        }
                    } else {
                        item {
                            ResizeAndConvertPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.ResizeAndConvert(uris)
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            BytesResizePreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.ResizeByBytes(uris)
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            BasicFilterPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.Filter(
                                            type = Screen.Filter.Type.Basic(uris)
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                        if (uris.size >= 2) {
                            item {
                                ImageStitchingPreference(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navigate(Screen.ImageStitching(uris))
                                    },
                                    color = color
                                )
                            }
                        }
                        item {
                            ImagesToPdfPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.PdfTools(
                                            Screen.PdfTools.Type.ImagesToPdf(
                                                uris
                                            )
                                        )
                                    )
                                },
                                color = color
                            )
                        }
                        if (uris.size == 2) {
                            item {
                                ComparePreference(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navigate(
                                            Screen.Compare(uris)
                                        )
                                    },
                                    color = color
                                )
                            }
                        }
                        item {
                            ImagePreviewPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigate(
                                        Screen.ImagePreview(uris)
                                    )
                                },
                                color = color
                            )
                        }
                        item {
                            LimitsPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.LimitResize(uris)) },
                                color = color
                            )
                        }
                        item {
                            DeleteExifPreference(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navigate(Screen.DeleteExif(uris)) },
                                color = color
                            )
                        }
                    }
                }
            }
        },
        visible = visible,
    )
}
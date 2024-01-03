package ru.tech.imageresizershrinker.feature.pick_color.presentation

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.ImageColorDetector
import com.smarttoolfactory.colordetector.parser.rememberColorParser
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import dev.olshevski.navigation.reimagined.hilt.hiltViewModel
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.coreresources.R
import ru.tech.imageresizershrinker.coreui.icons.material.PaletteSwatch
import ru.tech.imageresizershrinker.coreui.theme.outlineVariant
import ru.tech.imageresizershrinker.coreui.utils.helper.ContextUtils.copyToClipboard
import ru.tech.imageresizershrinker.coreui.utils.helper.Picker
import ru.tech.imageresizershrinker.coreui.utils.helper.localImagePickerMode
import ru.tech.imageresizershrinker.coreui.utils.helper.rememberImagePicker
import ru.tech.imageresizershrinker.coreui.utils.helper.toHex
import ru.tech.imageresizershrinker.coreui.utils.navigation.LocalNavController
import ru.tech.imageresizershrinker.coreui.utils.navigation.Screen
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedIconButton
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedSwitch
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedSwitchDefaults
import ru.tech.imageresizershrinker.coreui.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.coreui.widget.modifier.container
import ru.tech.imageresizershrinker.coreui.widget.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.coreui.widget.modifier.navBarsPaddingOnlyIfTheyAtTheBottom
import ru.tech.imageresizershrinker.coreui.widget.modifier.navBarsPaddingOnlyIfTheyAtTheEnd
import ru.tech.imageresizershrinker.coreui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.coreui.widget.other.LoadingDialog
import ru.tech.imageresizershrinker.coreui.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.coreui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.coreui.widget.other.showError
import ru.tech.imageresizershrinker.coreui.widget.text.Marquee
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalSettingsState
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalWindowSizeClass
import ru.tech.imageresizershrinker.feature.pick_color.presentation.viewModel.PickColorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickColorFromImageScreen(
    uriState: Uri?,
    onGoBack: () -> Unit,
    viewModel: PickColorViewModel = hiltViewModel()
) {
    val settingsState = LocalSettingsState.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val toastHostState = LocalToastHost.current
    val themeState = LocalDynamicThemeState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val parser = rememberColorParser()

    val scope = rememberCoroutineScope()

    var canZoom by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uriState) {
        uriState?.let {
            viewModel.setUri(it)
            viewModel.decodeBitmapByUri(
                uri = it,
                originalSize = false,
                onGetMimeType = {},
                onGetExif = {},
                onGetBitmap = viewModel::updateBitmap,
                onError = {
                    scope.launch {
                        toastHostState.showError(context, it)
                    }
                }
            )
        }
    }

    LaunchedEffect(viewModel.bitmap) {
        viewModel.bitmap?.let {
            if (allowChangeColor) {
                themeState.updateColorByImage(it)
            }
        }
    }

    LaunchedEffect(viewModel.color) {
        if (!viewModel.color.isUnspecified) {
            if (allowChangeColor) themeState.updateColor(viewModel.color)
        }
    }

    val pickImageLauncher =
        rememberImagePicker(
            mode = localImagePickerMode(Picker.Single)
        ) { uris ->
            uris.takeIf { it.isNotEmpty() }?.firstOrNull()?.let {
                viewModel.setUri(it)
                viewModel.decodeBitmapByUri(
                    uri = it,
                    originalSize = false,
                    onGetMimeType = {},
                    onGetExif = {},
                    onGetBitmap = viewModel::updateBitmap,
                    onError = {
                        scope.launch {
                            toastHostState.showError(context, it)
                        }
                    }
                )
            }
        }

    val pickImage = {
        pickImageLauncher.pickImage()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()

    val portrait =
        LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE || LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact

    val switch = @Composable {
        EnhancedSwitch(
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = EnhancedSwitchDefaults.uncheckableColors(),
            checked = !canZoom,
            onCheckedChange = { canZoom = !canZoom },
            thumbIcon = if (!canZoom) {
                Icons.Rounded.Colorize
            } else Icons.Rounded.ZoomIn,
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        val color = viewModel.color
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(
                modifier = Modifier.drawHorizontalStroke(),
                targetState = viewModel.bitmap == null,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { noBmp ->
                if (noBmp) {
                    LargeTopAppBar(
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            EnhancedIconButton(
                                containerColor = Color.Transparent,
                                contentColor = LocalContentColor.current,
                                enableAutoShadowAndBorder = false,
                                onClick = {
                                    onGoBack()
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                            }
                        },
                        title = {
                            Marquee(
                                edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            ) {
                                Text(stringResource(R.string.pick_color))
                            }
                        },
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ),
                        actions = {
                            if (viewModel.bitmap == null) {
                                TopAppBarEmoji()
                            }
                        }
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        modifier = Modifier.animateContentSize(),
                    ) {
                        Column {
                            Column(Modifier.navBarsPaddingOnlyIfTheyAtTheEnd()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    EnhancedIconButton(
                                        containerColor = Color.Transparent,
                                        contentColor = LocalContentColor.current,
                                        enableAutoShadowAndBorder = false,
                                        onClick = {
                                            onGoBack()
                                        },
                                        modifier = Modifier.statusBarsPadding()
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                                    }
                                    if (!portrait) {
                                        ProvideTextStyle(
                                            value = LocalTextStyle.current.merge(
                                                MaterialTheme.typography.headlineSmall
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(
                                                        start = 16.dp,
                                                        end = 16.dp
                                                    )
                                                    .statusBarsPadding()
                                            ) {
                                                Text(stringResource(R.string.color))

                                                Text(
                                                    modifier = Modifier
                                                        .padding(horizontal = 8.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            context.copyToClipboard(
                                                                context.getString(R.string.color),
                                                                color.toHex()
                                                            )
                                                            scope.launch {
                                                                toastHostState.showToast(
                                                                    icon = Icons.Rounded.ContentPaste,
                                                                    message = context.getString(R.string.color_copied)
                                                                )
                                                            }
                                                        }
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .border(
                                                            settingsState.borderWidth,
                                                            MaterialTheme.colorScheme.outlineVariant(
                                                                onTopOf = MaterialTheme.colorScheme.secondaryContainer
                                                            ),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 6.dp),
                                                    text = color.toHex(),
                                                    style = LocalTextStyle.current.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                )

                                                Text(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(2.dp),
                                                    text = remember(color) {
                                                        derivedStateOf {
                                                            parser.parseColorName(color)
                                                        }
                                                    }.value
                                                )

                                                Box(
                                                    Modifier
                                                        .padding(
                                                            vertical = 4.dp,
                                                            horizontal = 16.dp
                                                        )
                                                        .background(
                                                            color = animateColorAsState(color).value,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .height(40.dp)
                                                        .width(72.dp)
                                                        .border(
                                                            width = settingsState.borderWidth,
                                                            color = MaterialTheme.colorScheme.outlineVariant(
                                                                onTopOf = animateColorAsState(color).value
                                                            ),
                                                            shape = RoundedCornerShape(11.dp)
                                                        )
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            context.copyToClipboard(
                                                                context.getString(R.string.color),
                                                                color.toHex()
                                                            )
                                                            scope.launch {
                                                                toastHostState.showToast(
                                                                    icon = Icons.Rounded.ContentPaste,
                                                                    message = context.getString(R.string.color_copied)
                                                                )
                                                            }
                                                        }
                                                )

                                                if (viewModel.uri != null) {
                                                    EnhancedIconButton(
                                                        containerColor = Color.Transparent,
                                                        contentColor = LocalContentColor.current,
                                                        enableAutoShadowAndBorder = false,
                                                        onClick = {
                                                            navController.pop()
                                                            navController.navigate(
                                                                Screen.GeneratePalette(
                                                                    viewModel.uri
                                                                )
                                                            )
                                                        },
                                                        modifier = Modifier.statusBarsPadding()
                                                    ) {
                                                        Icon(Icons.Rounded.PaletteSwatch, null)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(
                                        Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp)
                                    )
                                    if (viewModel.uri != null && portrait) {
                                        EnhancedIconButton(
                                            containerColor = Color.Transparent,
                                            contentColor = LocalContentColor.current,
                                            enableAutoShadowAndBorder = false,
                                            onClick = {
                                                navController.pop()
                                                navController.navigate(
                                                    Screen.GeneratePalette(
                                                        viewModel.uri
                                                    )
                                                )
                                            },
                                            modifier = Modifier.statusBarsPadding()
                                        ) {
                                            Icon(Icons.Rounded.PaletteSwatch, null)
                                        }
                                    }
                                }
                                if (portrait) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ProvideTextStyle(
                                        value = LocalTextStyle.current.merge(
                                            MaterialTheme.typography.headlineSmall
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                        ) {
                                            Text(stringResource(R.string.color))

                                            Text(
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        context.copyToClipboard(
                                                            context.getString(R.string.color),
                                                            color.toHex()
                                                        )
                                                        scope.launch {
                                                            toastHostState.showToast(
                                                                icon = Icons.Rounded.ContentPaste,
                                                                message = context.getString(R.string.color_copied)
                                                            )
                                                        }
                                                    }
                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    .border(
                                                        settingsState.borderWidth,
                                                        MaterialTheme.colorScheme.outlineVariant(
                                                            onTopOf = MaterialTheme.colorScheme.secondaryContainer
                                                        ),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 6.dp),
                                                text = color.toHex(),
                                                style = LocalTextStyle.current.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            )

                                            Spacer(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(2.dp)
                                            )

                                            Box(
                                                Modifier
                                                    .padding(vertical = 4.dp)
                                                    .background(
                                                        color = animateColorAsState(color).value,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .height(40.dp)
                                                    .width(72.dp)
                                                    .border(
                                                        width = settingsState.borderWidth,
                                                        color = MaterialTheme.colorScheme.outlineVariant(
                                                            onTopOf = animateColorAsState(color).value
                                                        ),
                                                        shape = RoundedCornerShape(11.dp)
                                                    )
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        context.copyToClipboard(
                                                            context.getString(R.string.color),
                                                            color.toHex()
                                                        )
                                                        scope.launch {
                                                            toastHostState.showToast(
                                                                icon = Icons.Rounded.ContentPaste,
                                                                message = context.getString(R.string.color_copied)
                                                            )
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                viewModel.bitmap?.let {
                    if (portrait) {
                        AnimatedContent(
                            targetState = it
                        ) { bitmap ->
                            ImageColorDetector(
                                canZoom = canZoom,
                                imageBitmap = bitmap.asImageBitmap(),
                                color = viewModel.color,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .navBarsPaddingOnlyIfTheyAtTheEnd()
                                    .container(resultPadding = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .transparencyChecker(),
                                onColorChange = viewModel::updateColor
                            )
                        }
                    } else {
                        Row(Modifier.navBarsPaddingOnlyIfTheyAtTheEnd()) {
                            Box(
                                Modifier
                                    .weight(0.8f)
                                    .padding(20.dp)
                            ) {
                                Box(Modifier.align(Alignment.Center)) {
                                    AnimatedContent(
                                        targetState = it
                                    ) { bitmap ->
                                        ImageColorDetector(
                                            canZoom = canZoom,
                                            imageBitmap = bitmap.asImageBitmap(),
                                            color = viewModel.color,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .navBarsPaddingOnlyIfTheyAtTheBottom()
                                                .container(resultPadding = 8.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .transparencyChecker(),
                                            onColorChange = viewModel::updateColor
                                        )
                                    }
                                }
                            }
                            Column(
                                Modifier
                                    .container(
                                        shape = RectangleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        resultPadding = 0.dp
                                    )
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp)
                                    .navigationBarsPadding(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                switch()
                                Spacer(modifier = Modifier.height(16.dp))
                                EnhancedFloatingActionButton(
                                    onClick = pickImage
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddPhotoAlternate,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                } ?: Column(Modifier.verticalScroll(scrollState)) {
                    ImageNotPickedWidget(
                        onPickImage = pickImage,
                        modifier = Modifier
                            .padding(bottom = 88.dp, top = 20.dp, start = 20.dp, end = 20.dp)
                            .navigationBarsPadding()
                    )
                }
            }
            if (viewModel.bitmap != null && portrait) {
                BottomAppBar(
                    modifier = Modifier
                        .drawHorizontalStroke(true),
                    actions = {
                        switch()
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .offset(x = (-10).dp),
                            text = remember(color) {
                                derivedStateOf {
                                    parser.parseColorName(color)
                                }
                            }.value,
                            textAlign = TextAlign.Center
                        )
                    },
                    floatingActionButton = {
                        EnhancedFloatingActionButton(
                            onClick = pickImage
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }

        if (viewModel.bitmap == null) {
            EnhancedFloatingActionButton(
                onClick = pickImage,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .align(settingsState.fabAlignment),
                content = {
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Rounded.AddPhotoAlternate, null)
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.pick_image_alt))
                    Spacer(Modifier.width(16.dp))
                }
            )
        }
    }

    if (viewModel.isImageLoading) LoadingDialog(false) {}

    BackHandler {
        onGoBack()
    }
}
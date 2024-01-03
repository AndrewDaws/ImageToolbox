package ru.tech.imageresizershrinker.presentation.crash_screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.BuildConfig
import ru.tech.imageresizershrinker.coreresources.R
import ru.tech.imageresizershrinker.coredomain.AUTHOR_TG
import ru.tech.imageresizershrinker.coredomain.ISSUE_TRACKER
import ru.tech.imageresizershrinker.presentation.crash_screen.viewModel.CrashViewModel
import ru.tech.imageresizershrinker.presentation.MainActivity
import ru.tech.imageresizershrinker.coreui.icons.material.Github
import ru.tech.imageresizershrinker.coreui.icons.material.Robot
import ru.tech.imageresizershrinker.coreui.icons.material.Telegram
import ru.tech.imageresizershrinker.coreui.model.toUiState
import ru.tech.imageresizershrinker.coreui.theme.Black
import ru.tech.imageresizershrinker.coreui.theme.Blue
import ru.tech.imageresizershrinker.coreui.theme.ImageToolboxTheme
import ru.tech.imageresizershrinker.coreui.theme.White
import ru.tech.imageresizershrinker.coreui.theme.outlineVariant
import ru.tech.imageresizershrinker.presentation.CrashHandler
import ru.tech.imageresizershrinker.coreui.utils.helper.ContextUtils.copyToClipboard
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedButton
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedIconButton
import ru.tech.imageresizershrinker.coreui.widget.modifier.container
import ru.tech.imageresizershrinker.coreui.widget.other.ToastHost
import ru.tech.imageresizershrinker.coreui.widget.other.rememberToastHostState
import ru.tech.imageresizershrinker.coreui.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalSettingsState

@AndroidEntryPoint
class CrashActivity : CrashHandler() {


    private val viewModel by viewModels<CrashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashReason = getCrashReason()
        val exName = crashReason.split("\n\n")[0].trim()
        val ex = crashReason.split("\n\n").drop(1).joinToString("\n\n")

        val title = "[Bug] App Crash: $exName"
        val deviceInfo =
            "Device: ${Build.MODEL} (${Build.BRAND} - ${Build.DEVICE}), SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE}), App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n\n"
        val body = "$deviceInfo$ex"


        setContent {
            val toastHostState = rememberToastHostState()
            val scope = rememberCoroutineScope()

            val newClip: (String) -> Unit = {
                copyToClipboard(
                    label = getString(R.string.exception),
                    value = it
                )
                scope.launch {
                    toastHostState.showToast(
                        icon = Icons.Rounded.ContentCopy,
                        message = getString(R.string.copied),
                    )
                }
            }

            CompositionLocalProvider(
                LocalSettingsState provides viewModel.settingsState.toUiState()
            ) {
                ImageToolboxTheme {
                    val conf = LocalConfiguration.current
                    val size = min(conf.screenWidthDp.dp, conf.screenHeightDp.dp)
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Robot,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(size * 0.3f)
                                        .statusBarsPadding()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.something_went_wrong_emphasis),
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(Modifier.padding(horizontal = 16.dp)) {
                                    EnhancedButton(
                                        onClick = {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(AUTHOR_TG + "_imagetoolbox")
                                                )
                                            )
                                            newClip(title + "\n\n" + body)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        containerColor = Blue,
                                        contentColor = White,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant(
                                            onTopOf = Blue
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Telegram,
                                                null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AutoSizeText(
                                                text = stringResource(id = R.string.contact_me),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    EnhancedButton(
                                        onClick = {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("$ISSUE_TRACKER/new?title=$title&body=$body")
                                                )
                                            )
                                            newClip(title + "\n\n" + body)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        containerColor = Black,
                                        contentColor = White,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant(
                                            onTopOf = Black
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Rounded.Github, null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            AutoSizeText(
                                                text = stringResource(id = R.string.create_issue),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .navigationBarsPadding()
                                        .container(RoundedCornerShape(24.dp), resultPadding = 0.dp)
                                        .animateContentSize()
                                ) {
                                    var showError by rememberSaveable {
                                        mutableStateOf(false)
                                    }
                                    val rotation by animateFloatAsState(if (showError) 180f else 0f)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(24.dp)
                                            )
                                            .clickable { showError = !showError }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.BugReport,
                                            contentDescription = null,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                        AutoSizeText(
                                            text = exName,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .weight(1f)
                                        )
                                        EnhancedIconButton(
                                            containerColor = Color.Transparent,
                                            contentColor = LocalContentColor.current,
                                            enableAutoShadowAndBorder = false,
                                            onClick = { showError = !showError },
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                                contentDescription = null,
                                                modifier = Modifier.rotate(rotation)
                                            )
                                        }
                                    }
                                    AnimatedVisibility(visible = showError) {
                                        SelectionContainer {
                                            Text(
                                                text = ex,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                            Row(
                                Modifier
                                    .padding(8.dp)
                                    .navigationBarsPadding()
                                    .align(Alignment.BottomCenter)
                            ) {
                                EnhancedFloatingActionButton(
                                    modifier = Modifier
                                        .weight(1f, false),
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                this@CrashActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                    },
                                    content = {
                                        Spacer(Modifier.width(16.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.RestartAlt,
                                            contentDescription = null
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        AutoSizeText(
                                            text = stringResource(R.string.restart_app),
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.width(16.dp))
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                EnhancedFloatingActionButton(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    onClick = {
                                        newClip(title + "\n\n" + body)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }

                    ToastHost(hostState = toastHostState)
                }
            }
        }
    }

}

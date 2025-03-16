/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package ru.tech.imageresizershrinker.core.ui.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import ru.tech.imageresizershrinker.core.di.entryPoint
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.model.SystemBarsVisibility
import ru.tech.imageresizershrinker.core.domain.remote.AnalyticsManager
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.settings.di.SettingsStateEntryPoint
import ru.tech.imageresizershrinker.core.settings.domain.SettingsProvider
import ru.tech.imageresizershrinker.core.settings.domain.model.SettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.model.asColorTuple
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.adjustFontSize
import ru.tech.imageresizershrinker.core.ui.utils.provider.setContentWithWindowSizeClass
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import javax.inject.Inject

@AndroidEntryPoint
abstract class M3Activity : AppCompatActivity() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    @Inject
    lateinit var dispatchersHolder: DispatchersHolder

    private lateinit var settingsProvider: SettingsProvider

    private val activityScope: CoroutineScope
        get() = lifecycleScope + dispatchersHolder.defaultDispatcher

    private val windowInsetsController: WindowInsetsControllerCompat?
        get() = window?.let {
            WindowCompat.getInsetsController(it, it.decorView)
        }

    private val _settingsState = mutableStateOf(SettingsState.Default)
    private val settingsState: SettingsState by _settingsState

    @Composable
    abstract fun Content()

    open fun onFirstLaunch() = Unit

    override fun attachBaseContext(newBase: Context) {
        newBase.entryPoint<SettingsStateEntryPoint> {
            settingsProvider = settingsManager
            _settingsState.update {
                runBlocking {
                    settingsProvider.getSettingsState()
                }
            }
            handleSystemBarsBehavior()
            handleSecureMode()
        }
        val newOverride = Configuration(newBase.resources?.configuration)
        settingsState.fontScale?.let { newOverride.fontScale = it }
        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsProvider
            .getSettingsStateFlow()
            .onEach { state ->
                _settingsState.update { state }
                handleSystemBarsBehavior()
                handleSecureMode()
                updateFirebaseParams()
                val colorTuple = state.appColorTuple.asColorTuple()
                DynamicColors.applyToActivitiesIfAvailable(
                    this@M3Activity.application,
                    DynamicColorsOptions.Builder()
                        .setContentBasedSource(colorTuple.primary.toArgb())
                        .build()
                )
            }
            .launchIn(activityScope)

        adjustFontSize(settingsState.fontScale)

        updateFirebaseParams()

        handleSystemBarsBehavior()

        handleSecureMode()

        if (savedInstanceState == null) onFirstLaunch()

        setContentWithWindowSizeClass { Content() }
    }

    private fun updateFirebaseParams() = analyticsManager.apply {
        updateAllowCollectCrashlytics(settingsState.allowCollectCrashlytics)
        updateAnalyticsCollectionEnabled(settingsState.allowCollectCrashlytics)
    }

    private var recreationJob: Job? by smartJob()

    override fun recreate() {
        recreationJob = activityScope.launch {
            delay(200L)
            super.recreate()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) handleSystemBarsBehavior()
        handleSecureMode()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handleSecureMode()
    }

    private fun handleSystemBarsBehavior() = runOnUiThread {
        windowInsetsController?.apply {
            when (settingsState.systemBarsVisibility) {
                SystemBarsVisibility.Auto -> {
                    val orientation = resources.configuration.orientation

                    show(STATUS_BARS)

                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        hide(NAV_BARS)
                    } else {
                        show(NAV_BARS)
                    }
                }

                SystemBarsVisibility.HideAll -> {
                    hide(SYSTEM_BARS)
                }

                SystemBarsVisibility.ShowAll -> {
                    show(SYSTEM_BARS)
                }

                SystemBarsVisibility.HideNavigationBar -> {
                    show(STATUS_BARS)
                    hide(NAV_BARS)
                }

                SystemBarsVisibility.HideStatusBar -> {
                    show(NAV_BARS)
                    hide(STATUS_BARS)
                }
            }

            systemBarsBehavior = if (settingsState.isSystemBarsVisibleBySwipe) {
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        }
    }

    private fun handleSecureMode() = runOnUiThread {
        if (settingsState.isSecureMode) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    companion object {
        private val NAV_BARS = WindowInsetsCompat.Type.navigationBars()
        private val SYSTEM_BARS = WindowInsetsCompat.Type.systemBars()
        private val STATUS_BARS = WindowInsetsCompat.Type.statusBars()
    }
}
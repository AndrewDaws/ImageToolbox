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

package ru.tech.imageresizershrinker.feature.settings.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LogoDev
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.logger.Logger
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.SaveConfirm
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.theme.mixedContainer
import ru.tech.imageresizershrinker.core.ui.theme.onMixedContainer
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRowSwitch

@Composable
fun EnableToolExitConfirmationSettingItem(
    onClick: () -> Unit,
    shape: Shape = ContainerShapeDefaults.centerShape,
    modifier: Modifier = Modifier.padding(horizontal = 8.dp)
) {
    val settingsState = LocalSettingsState.current
    PreferenceRowSwitch(
        modifier = modifier,
        shape = shape,
        title = stringResource(R.string.tool_exit_confirmation),
        subtitle = stringResource(R.string.tool_exit_confirmation_sub),
        checked = settingsState.enableToolExitConfirmation,
        onClick = {
            onClick()
        },
        startIcon = Icons.Outlined.SaveConfirm
    )
}

@Composable
fun SendLogsSettingItem(
    modifier: Modifier = Modifier.padding(horizontal = 8.dp),
    shape: Shape = ContainerShapeDefaults.centerShape,
    color: Color = MaterialTheme.colorScheme.mixedContainer.copy(0.9f),
    contentColor: Color = MaterialTheme.colorScheme.onMixedContainer
) {
    PreferenceItem(
        contentColor = contentColor,
        shape = shape,
        onClick = Logger::shareLogs,
        startIcon = Icons.Rounded.LogoDev,
        title = stringResource(R.string.send_logs),
        subtitle = stringResource(R.string.send_logs_sub),
        color = color,
        modifier = modifier
    )
}
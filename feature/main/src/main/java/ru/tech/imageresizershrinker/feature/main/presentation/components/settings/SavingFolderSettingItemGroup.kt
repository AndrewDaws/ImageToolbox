package ru.tech.imageresizershrinker.feature.main.presentation.components.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.coreresources.R
import ru.tech.imageresizershrinker.coreui.icons.material.CreateAlt
import ru.tech.imageresizershrinker.coreui.utils.helper.toUiPath
import ru.tech.imageresizershrinker.coreui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.coreui.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.coreui.widget.other.ToastDuration
import ru.tech.imageresizershrinker.coreui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalSettingsState

@Composable
fun SavingFolderSettingItemGroup(
    modifier: Modifier = Modifier,
    updateSaveFolderUri: (Uri?) -> Unit
) {
    Column(modifier) {
        val context = LocalContext.current
        val toastHostState = LocalToastHost.current
        val scope = rememberCoroutineScope()
        val settingsState = LocalSettingsState.current
        val currentFolderUri = settingsState.saveFolderUri
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            onResult = { uri ->
                uri?.let {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    updateSaveFolderUri(it)
                }
            }
        )
        PreferenceItem(
            shape = ContainerShapeDefaults.topShape,
            onClick = { updateSaveFolderUri(null) },
            title = stringResource(R.string.def),
            subtitle = stringResource(R.string.default_folder),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = animateFloatAsState(
                    if (currentFolderUri == null) 0.7f
                    else 0.2f
                ).value
            ),
            endIcon = if (currentFolderUri != null) Icons.Outlined.FolderSpecial else Icons.Rounded.FolderSpecial,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(
                    width = settingsState.borderWidth,
                    color = animateColorAsState(
                        if (currentFolderUri == null) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.5f
                        )
                        else Color.Transparent
                    ).value,
                    shape = ContainerShapeDefaults.topShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        PreferenceItem(
            shape = ContainerShapeDefaults.bottomShape,
            onClick = {
                kotlin.runCatching {
                    launcher.launch(currentFolderUri)
                }.getOrNull() ?: scope.launch {
                    toastHostState.showToast(
                        message = context.getString(R.string.activate_files),
                        icon = Icons.Outlined.FolderOff,
                        duration = ToastDuration.Long
                    )
                }
            },
            title = stringResource(R.string.custom),
            subtitle = currentFolderUri.toUiPath(
                context = context,
                default = stringResource(R.string.unspecified)
            ),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = animateFloatAsState(
                    if (currentFolderUri != null) 0.7f
                    else 0.2f
                ).value
            ),
            endIcon = if (currentFolderUri != null) Icons.Rounded.CreateAlt else Icons.Rounded.AddCircleOutline,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(
                    width = settingsState.borderWidth,
                    color = animateColorAsState(
                        if (currentFolderUri != null) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.5f
                        )
                        else Color.Transparent
                    ).value,
                    shape = ContainerShapeDefaults.bottomShape
                )
        )
    }
}
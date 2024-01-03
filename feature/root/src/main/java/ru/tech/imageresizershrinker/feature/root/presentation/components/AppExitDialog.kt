package ru.tech.imageresizershrinker.feature.root.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoorBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ru.tech.imageresizershrinker.coreresources.R
import ru.tech.imageresizershrinker.coreui.utils.helper.ContextUtils.findActivity
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedButton
import ru.tech.imageresizershrinker.coreui.widget.modifier.alertDialogBorder

@Composable
fun AppExitDialog(
    onDismiss: () -> Unit,
    visible: Boolean
) {
    val activity = LocalContext.current.findActivity()

    if (visible) {
        AlertDialog(
            modifier = Modifier.alertDialogBorder(),
            onDismissRequest = onDismiss,
            dismissButton = {
                EnhancedButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        activity?.finishAffinity()
                    }
                ) {
                    Text(stringResource(R.string.close))
                }
            },
            confirmButton = {
                EnhancedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.stay))
                }
            },
            title = { Text(stringResource(R.string.app_closing)) },
            text = {
                Text(
                    stringResource(R.string.app_closing_sub),
                    textAlign = TextAlign.Center
                )
            },
            icon = { Icon(Icons.Outlined.DoorBack, null) }
        )
    }
}
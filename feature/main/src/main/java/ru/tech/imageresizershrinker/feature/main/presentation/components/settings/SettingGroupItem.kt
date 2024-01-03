package ru.tech.imageresizershrinker.feature.main.presentation.components.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.coreui.widget.other.ExpandableItem
import ru.tech.imageresizershrinker.coreui.widget.text.TitleItem

@Composable
fun SettingGroupItem(
    icon: ImageVector,
    text: String,
    initialState: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    ExpandableItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        visibleContent = {
            TitleItem(
                modifier = Modifier.padding(start = 8.dp),
                icon = icon,
                text = text
            )
        },
        expandableContent = { content() },
        initialState = initialState
    )
}
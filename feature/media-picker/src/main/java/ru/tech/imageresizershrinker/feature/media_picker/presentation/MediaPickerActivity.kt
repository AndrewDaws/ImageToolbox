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
package ru.tech.imageresizershrinker.feature.media_picker.presentation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.retainedComponent
import dagger.hilt.android.AndroidEntryPoint
import ru.tech.imageresizershrinker.core.crash.components.M3Activity
import ru.tech.imageresizershrinker.feature.media_picker.presentation.components.MediaPickerRootContent
import ru.tech.imageresizershrinker.feature.media_picker.presentation.screenLogic.MediaPickerComponent
import javax.inject.Inject

@AndroidEntryPoint
class MediaPickerActivity : M3Activity() {

    @Inject
    lateinit var componentFactory: MediaPickerComponent.Factory

    private val component: MediaPickerComponent by lazy {
        retainedComponent(factory = componentFactory::invoke)
    }

    @Composable
    override fun Content() = MediaPickerRootContent(component)

}
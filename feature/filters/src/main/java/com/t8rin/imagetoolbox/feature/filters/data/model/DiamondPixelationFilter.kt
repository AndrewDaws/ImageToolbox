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

package com.t8rin.imagetoolbox.feature.filters.data.model

import android.graphics.Bitmap
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.transformation.Transformation
import com.t8rin.imagetoolbox.core.filters.domain.model.Filter
import com.t8rin.imagetoolbox.feature.filters.data.utils.pixelation.Pixelate
import com.t8rin.imagetoolbox.feature.filters.data.utils.pixelation.PixelationLayer

internal class DiamondPixelationFilter(
    override val value: Float = 24f,
) : Transformation<Bitmap>, Filter.DiamondPixelation {
    override val cacheKey: String
        get() = (value).hashCode().toString()

    override suspend fun transform(
        input: Bitmap,
        size: IntegerSize
    ): Bitmap {
        return Pixelate.fromBitmap(
            input = input,
            layers = arrayOf(
                PixelationLayer.Builder(PixelationLayer.Shape.Diamond)
                    .setResolution(value)
                    .setSize(value + 1)
                    .build(),
                PixelationLayer.Builder(PixelationLayer.Shape.Diamond)
                    .setResolution(value)
                    .setOffset(value / 2)
                    .build(),
                PixelationLayer.Builder(PixelationLayer.Shape.Square)
                    .setResolution(value)
                    .setAlpha(0.6f)
                    .build()
            )
        )
    }
}
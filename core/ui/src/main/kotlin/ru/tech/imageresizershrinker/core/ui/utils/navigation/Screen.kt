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

package ru.tech.imageresizershrinker.core.ui.utils.navigation

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.BrandingWatermark
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.FilterHdr
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Gif
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material.icons.rounded.Texture
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Apng
import ru.tech.imageresizershrinker.core.resources.icons.ApngBox
import ru.tech.imageresizershrinker.core.resources.icons.CropSmall
import ru.tech.imageresizershrinker.core.resources.icons.Encrypted
import ru.tech.imageresizershrinker.core.resources.icons.Exif
import ru.tech.imageresizershrinker.core.resources.icons.ImageCombine
import ru.tech.imageresizershrinker.core.resources.icons.ImageConvert
import ru.tech.imageresizershrinker.core.resources.icons.ImageDownload
import ru.tech.imageresizershrinker.core.resources.icons.ImageEdit
import ru.tech.imageresizershrinker.core.resources.icons.ImageLimit
import ru.tech.imageresizershrinker.core.resources.icons.ImageSync
import ru.tech.imageresizershrinker.core.resources.icons.ImageText
import ru.tech.imageresizershrinker.core.resources.icons.ImageWeight
import ru.tech.imageresizershrinker.core.resources.icons.Jpg
import ru.tech.imageresizershrinker.core.resources.icons.Jxl
import ru.tech.imageresizershrinker.core.resources.icons.MultipleImageEdit
import ru.tech.imageresizershrinker.core.resources.icons.PaletteSwatch
import ru.tech.imageresizershrinker.core.resources.icons.Svg
import ru.tech.imageresizershrinker.core.resources.icons.Toolbox
import ru.tech.imageresizershrinker.core.resources.icons.Transparency

@Serializable
@Parcelize
sealed class Screen(
    open val id: Int,
    @StringRes val title: Int,
    @StringRes val subtitle: Int
) : Parcelable {

    @Suppress("unused")
    val simpleName: String?
        get() = when (this) {
            is ApngTools -> "APNG_Tools"
            is Cipher -> "Cipher"
            is Compare -> "Compare"
            is Crop -> "Crop"
            is DeleteExif -> "Delete_Exif"
            is Draw -> "Draw"
            EasterEgg -> "Easter_Egg"
            is EraseBackground -> "Erase_Background"
            is Filter -> "Filter"
            is GeneratePalette -> "Generate_Palette"
            is GifTools -> "GIF_Tools"
            is GradientMaker -> "Gradient_Maker"
            is ImagePreview -> "Image_Preview"
            is ImageStitching -> "Image_Stitching"
            is JxlTools -> "JXL_Tools"
            is LimitResize -> "Limit_Resize"
            is LoadNetImage -> "Load_Net_Image"
            Main -> null
            is PdfTools -> "PDF_Tools"
            is PickColorFromImage -> "Pick_Color_From_Image"
            is RecognizeText -> "Recognize_Text"
            is ResizeAndConvert -> "Resize_And_Convert"
            is ResizeByBytes -> "Resize_By_Bytes"
            Settings -> "Settings"
            is SingleEdit -> "Single_Edit"
            is Watermarking -> "Watermarking"
            is Zip -> "Zip"
            is Svg -> "Svg"
            is Convert -> "Convert"
        }

    val icon: ImageVector?
        get() = when (this) {
            EasterEgg,
            Main,
            Settings -> null

            is SingleEdit -> Icons.Outlined.ImageEdit
            is ApngTools -> Icons.Rounded.ApngBox
            is Cipher -> Icons.Outlined.Encrypted
            is Compare -> Icons.Rounded.Compare
            is Crop -> Icons.Rounded.CropSmall
            is DeleteExif -> Icons.Outlined.Exif
            is Draw -> Icons.Outlined.Draw
            is EraseBackground -> Icons.Filled.Transparency
            is Filter -> Icons.Outlined.AutoFixHigh
            is GeneratePalette -> Icons.Outlined.PaletteSwatch
            is GifTools -> Icons.Outlined.GifBox
            is GradientMaker -> Icons.Outlined.Gradient
            is ImagePreview -> Icons.Outlined.Photo
            is ImageStitching -> Icons.Rounded.ImageCombine
            is JxlTools -> Icons.Filled.Jxl
            is LimitResize -> Icons.Outlined.ImageLimit
            is LoadNetImage -> Icons.Outlined.ImageDownload
            is PdfTools -> Icons.Outlined.PictureAsPdf
            is PickColorFromImage -> Icons.Outlined.Colorize
            is RecognizeText -> Icons.Outlined.ImageText
            is ResizeAndConvert -> Icons.Rounded.MultipleImageEdit
            is ResizeByBytes -> Icons.Rounded.ImageWeight
            is Watermarking -> Icons.AutoMirrored.Outlined.BrandingWatermark
            is Zip -> Icons.Outlined.FolderZip
            is Svg -> Icons.Outlined.Svg
            is Convert -> Icons.Outlined.ImageConvert
        }

    @Serializable
    data object Settings : Screen(
        id = -3,
        title = 0,
        subtitle = 0
    )

    @Serializable
    data object EasterEgg : Screen(
        id = -2,
        title = 0,
        subtitle = 0
    )

    @Serializable
    data object Main : Screen(
        id = -1,
        title = 0,
        subtitle = 0
    )

    @Serializable
    data class SingleEdit(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 0,
        title = R.string.single_edit,
        subtitle = R.string.single_edit_sub
    )

    @Serializable
    data class ResizeAndConvert(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 1,
        title = R.string.resize_and_convert,
        subtitle = R.string.resize_and_convert_sub
    )

    @Serializable
    data class ResizeByBytes(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 2,
        title = R.string.by_bytes_resize,
        subtitle = R.string.by_bytes_resize_sub
    )

    @Serializable
    data class Crop(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 3,
        title = R.string.crop,
        subtitle = R.string.crop_sub
    )

    @Serializable
    data class Filter(
        val type: Type? = null
    ) : Screen(
        id = 4,
        title = R.string.filter,
        subtitle = R.string.filter_sub
    ) {
        @Serializable
        @Parcelize
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) : Parcelable {

            val icon: ImageVector
                get() = when (this) {
                    is Masking -> Icons.Rounded.Texture
                    is Basic -> Icons.Outlined.AutoFixHigh
                }

            @Serializable
            data class Masking(
                @Serializable(UriSerializer::class)
                val uri: Uri? = null
            ) : Type(
                title = R.string.mask_filter,
                subtitle = R.string.mask_filter_sub
            )

            @Serializable
            data class Basic(
                val uris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.full_filter,
                subtitle = R.string.full_filter_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        Basic(),
                        Masking()
                    )
                }
            }
        }
    }

    @Serializable
    data class Draw(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 5,
        title = R.string.draw,
        subtitle = R.string.draw_sub
    )

    @Serializable
    data class Cipher(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 6,
        title = R.string.cipher,
        subtitle = R.string.cipher_sub
    )

    @Serializable
    data class EraseBackground(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 7,
        title = R.string.background_remover,
        subtitle = R.string.background_remover_sub
    )

    @Serializable
    data class ImagePreview(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 8,
        title = R.string.image_preview,
        subtitle = R.string.image_preview_sub
    )

    @Serializable
    data class ImageStitching(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 9,
        title = R.string.image_stitching,
        subtitle = R.string.image_stitching_sub
    )

    @Serializable
    data class LoadNetImage(
        val url: String = ""
    ) : Screen(
        id = 10,
        title = R.string.load_image_from_net,
        subtitle = R.string.load_image_from_net_sub
    )

    @Serializable
    data class PickColorFromImage(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 11,
        title = R.string.pick_color,
        subtitle = R.string.pick_color_sub
    )

    @Serializable
    data class GeneratePalette(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 12,
        title = R.string.generate_palette,
        subtitle = R.string.palette_sub
    )

    @Serializable
    data class DeleteExif(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 13,
        title = R.string.delete_exif,
        subtitle = R.string.delete_exif_sub
    )

    @Serializable
    data class Compare(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 14,
        title = R.string.compare,
        subtitle = R.string.compare_sub
    )

    @Serializable
    data class LimitResize(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 15,
        title = R.string.limits_resize,
        subtitle = R.string.limits_resize_sub
    )

    @Serializable
    data class PdfTools(
        val type: Type? = null
    ) : Screen(
        id = 16,
        title = R.string.pdf_tools,
        subtitle = R.string.pdf_tools_sub
    ) {
        @Serializable
        @Parcelize
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) : Parcelable {

            val icon: ImageVector
                get() = when (this) {
                    is ImagesToPdf -> Icons.Outlined.PictureAsPdf
                    is PdfToImages -> Icons.Outlined.Collections
                    is Preview -> Icons.Rounded.Preview
                }

            @Serializable
            data class Preview(
                @Serializable(UriSerializer::class)
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.preview_pdf,
                subtitle = R.string.preview_pdf_sub
            )

            @Serializable
            data class PdfToImages(
                @Serializable(UriSerializer::class)
                val pdfUri: Uri? = null
            ) : Type(
                title = R.string.pdf_to_images,
                subtitle = R.string.pdf_to_images_sub
            )

            @Serializable
            data class ImagesToPdf(
                val imageUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.images_to_pdf,
                subtitle = R.string.images_to_pdf_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        Preview(),
                        PdfToImages(),
                        ImagesToPdf()
                    )
                }
            }
        }
    }

    @Serializable
    data class RecognizeText(
        @Serializable(UriSerializer::class)
        val uri: Uri? = null
    ) : Screen(
        id = 17,
        title = R.string.recognize_text,
        subtitle = R.string.recognize_text_sub
    )

    @Serializable
    data class GradientMaker(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 18,
        title = R.string.gradient_maker,
        subtitle = R.string.gradient_maker_sub,
    )

    @Serializable
    data class Watermarking(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 19,
        title = R.string.watermarking,
        subtitle = R.string.watermarking_sub,
    )

    @Serializable
    data class GifTools(
        val type: Type? = null
    ) : Screen(
        id = 20,
        title = R.string.gif_tools,
        subtitle = R.string.gif_tools_sub
    ) {
        @Serializable
        @Parcelize
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) : Parcelable {

            val icon: ImageVector
                get() = when (this) {
                    is GifToImage -> Icons.Outlined.Collections
                    is GifToJxl -> Icons.Filled.Jxl
                    is ImageToGif -> Icons.Rounded.Gif
                }

            @Serializable
            data class GifToImage(
                @Serializable(UriSerializer::class)
                val gifUri: Uri? = null
            ) : Type(
                title = R.string.gif_type_to_image,
                subtitle = R.string.gif_type_to_image_sub
            )

            @Serializable
            data class ImageToGif(
                val imageUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.gif_type_to_gif,
                subtitle = R.string.gif_type_to_gif_sub
            )

            @Serializable
            data class GifToJxl(
                val gifUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.gif_type_to_jxl,
                subtitle = R.string.gif_type_to_jxl_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        ImageToGif(),
                        GifToImage(),
                        GifToJxl()
                    )
                }
            }
        }
    }

    @Serializable
    data class ApngTools(
        val type: Type? = null
    ) : Screen(
        id = 21,
        title = R.string.apng_tools,
        subtitle = R.string.apng_tools_sub
    ) {
        @Serializable
        @Parcelize
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) : Parcelable {

            val icon: ImageVector
                get() = when (this) {
                    is ApngToImage -> Icons.Outlined.Collections
                    is ApngToJxl -> Icons.Filled.Jxl
                    is ImageToApng -> Icons.Rounded.Apng
                }

            @Serializable
            data class ApngToImage(
                @Serializable(UriSerializer::class)
                val apngUri: Uri? = null
            ) : Type(
                title = R.string.apng_type_to_image,
                subtitle = R.string.apng_type_to_image_sub
            )

            @Serializable
            data class ImageToApng(
                val imageUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.apng_type_to_apng,
                subtitle = R.string.apng_type_to_apng_sub
            )

            @Serializable
            data class ApngToJxl(
                val apngUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.apng_type_to_jxl,
                subtitle = R.string.apng_type_to_jxl_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        ImageToApng(),
                        ApngToImage(),
                        ApngToJxl()
                    )
                }
            }
        }
    }

    @Serializable
    data class Zip(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 22,
        title = R.string.zip,
        subtitle = R.string.zip_sub
    )

    @Serializable
    data class JxlTools(
        val type: Type? = null
    ) : Screen(
        id = 23,
        title = R.string.jxl_tools,
        subtitle = R.string.jxl_tools_sub
    ) {
        @Serializable
        @Parcelize
        sealed class Type(
            @StringRes val title: Int,
            @StringRes val subtitle: Int
        ) : Parcelable {

            val icon: ImageVector
                get() = when (this) {
                    is ImageToJxl -> Icons.Rounded.Animation
                    is JpegToJxl -> Icons.Filled.Jxl
                    is JxlToImage -> Icons.Outlined.Collections
                    is JxlToJpeg -> Icons.Outlined.Jpg
                }

            @Serializable
            data class JxlToJpeg(
                val jxlImageUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.jxl_type_to_jpeg,
                subtitle = R.string.jxl_type_to_jpeg_sub
            )

            @Serializable
            data class JpegToJxl(
                val jpegImageUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.jpeg_type_to_jxl,
                subtitle = R.string.jpeg_type_to_jxl_sub
            )

            @Serializable
            data class JxlToImage(
                @Serializable(UriSerializer::class)
                val jxlUri: Uri? = null
            ) : Type(
                title = R.string.jxl_type_to_images,
                subtitle = R.string.jxl_type_to_images_sub
            )

            @Serializable
            data class ImageToJxl(
                val imageUris: List<@Serializable(UriSerializer::class) Uri>? = null
            ) : Type(
                title = R.string.jxl_type_to_jxl,
                subtitle = R.string.jxl_type_to_jxl_sub
            )

            companion object {
                val entries by lazy {
                    listOf(
                        JpegToJxl(),
                        JxlToJpeg(),
                        JxlToImage(),
                        ImageToJxl()
                    )
                }
            }
        }
    }

    @Serializable
    data class Svg(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 24,
        title = R.string.images_to_svg,
        subtitle = R.string.images_to_svg_sub
    )

    @Serializable
    data class Convert(
        val uris: List<@Serializable(UriSerializer::class) Uri>? = null
    ) : Screen(
        id = 25,
        title = R.string.convert,
        subtitle = R.string.convert_sub
    )

    companion object {
        val typedEntries by lazy {
            listOf(
                listOf(
                    SingleEdit(),
                    ResizeAndConvert(),
                    Convert(),
                    Crop(),
                    ResizeByBytes(),
                    LimitResize(),
                ) to Triple(
                    R.string.edit,
                    Icons.Rounded.ImageSync,
                    Icons.Outlined.ImageSync
                ),
                listOf(
                    Filter(),
                    Draw(),
                    EraseBackground(),
                    ImageStitching(),
                    Watermarking(),
                    GradientMaker(),
                    DeleteExif(),
                ) to Triple(
                    R.string.create,
                    Icons.Filled.AutoAwesome,
                    Icons.Outlined.AutoAwesome
                ),
                listOf(
                    PickColorFromImage(),
                    RecognizeText(),
                    Compare(),
                    ImagePreview(),
                    Svg(),
                    GeneratePalette(),
                    LoadNetImage(),
                ) to Triple(
                    R.string.image,
                    Icons.Filled.FilterHdr,
                    Icons.Outlined.FilterHdr
                ),
                listOf(
                    PdfTools(),
                    GifTools(),
                    JxlTools(),
                    ApngTools(),
                    Cipher(),
                    Zip()
                ) to Triple(
                    R.string.tools,
                    Icons.Rounded.Toolbox,
                    Icons.Outlined.Toolbox
                )
            )
        }
        val entries by lazy {
            typedEntries.flatMap { it.first }.sortedBy { it.id }
        }

        const val FEATURES_COUNT = 39
    }
}
/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.metadata

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.t8rin.exif.ExifInterface
import com.t8rin.imagetoolbox.core.data.image.toMetadata
import com.t8rin.imagetoolbox.core.domain.image.toMap
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Verifies the real public ImageToolbox EXIF path:
 *
 * 1. A test image is copied from androidTest assets to a writable file.
 * 2. A first [ExifInterface] reads every public TAG_* attribute it can see.
 * 3. Every readable value is written back through [ExifInterface.setAttribute].
 * 4. [ExifInterface.saveAttributes] saves the file.
 * 5. Completely new [ExifInterface] instances read the saved file through both
 *    the File and InputStream constructors.
 * 6. Every originally readable tag is compared with the value read after saving.
 *
 * Put test images in:
 *
 * app/src/androidTest/assets/metadata_round_trip/
 *
 * Recommended files:
 * rich.jpg, rich.png, rich.webp, rich.avif, rich.heic, rich.heif,
 * rich.jxl, rich.tif, rich.tiff, rich.jp2 and rich.j2k.
 *
 * The images should already contain as many different metadata tags as possible.
 */
@RunWith(AndroidJUnit4::class)
class ExifMetadataRoundTripInstrumentedTest {

    private lateinit var context: Context
    private lateinit var workingDirectory: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workingDirectory = File(
            context.cacheDir,
            "exif_metadata_round_trip_${System.nanoTime()}"
        ).apply {
            check(mkdirs() || isDirectory) {
                "Could not create test directory: $absolutePath"
            }
        }
    }

    @After
    fun tearDown() {
        workingDirectory.deleteRecursively()
    }

    @Test
    fun everyReadableMetadataTagSurvivesSaveAndFreshExifInterfaceRead() {
        val assetNames = context.assets
            .list(ASSET_DIRECTORY)
            .orEmpty()
            .filter(::isSupportedTestImage)
            .sorted()

        if (assetNames.isEmpty()) {
            fail(
                "No test images found. Add metadata-rich images to " +
                        "app/src/androidTest/assets/$ASSET_DIRECTORY/"
            )
        }

        val failures = mutableListOf<String>()
        val testedFormats = linkedSetOf<String>()
        var totalComparedTags = 0

        assetNames.forEach { assetName ->
            val extension = assetName.substringAfterLast('.', missingDelimiterValue = "")
                .lowercase()
            testedFormats += extension

            runCatching {
                val file = copyAssetToWritableFile(assetName)
                val comparedTags = verifyRoundTrip(assetName, file)
                totalComparedTags += comparedTags

                Log.i(
                    TAG,
                    "PASS: $assetName — compared $comparedTags metadata tags"
                )
            }.onFailure { throwable ->
                failures += buildString {
                    append(assetName)
                    append(": ")
                    append(throwable.message ?: throwable::class.java.name)
                }
                Log.e(TAG, "FAIL: $assetName", throwable)
            }
        }

        if (failures.isNotEmpty()) {
            fail(
                buildString {
                    appendLine("EXIF metadata round-trip failed:")
                    failures.forEach { appendLine("• $it") }
                    appendLine()
                    append("Tested formats: ${testedFormats.joinToString()}")
                }
            )
        }

        Log.i(
            TAG,
            "SUCCESS: ${assetNames.size} files, " +
                    "${testedFormats.size} formats, $totalComparedTags tag comparisons"
        )
    }

    private fun verifyRoundTrip(
        assetName: String,
        file: File
    ): Int {
        val writer = ExifInterface(file).toMetadata()
        val before = writer.toMap()

        check(before.isNotEmpty()) {
            "$assetName contains no metadata readable by our ExifInterface"
        }

        val writeErrors = mutableListOf<String>()



        before.forEach { (tag, value) ->
            runCatching {
                // Deliberately write the value back through the public API.
                // This exercises dirty tracking and container serialization.
                writer.setAttribute(tag, value)
            }.onFailure { throwable ->
                writeErrors += "$tag=${value.quoted()} -> ${throwable.message}"
            }
        }

        check(writeErrors.isEmpty()) {
            buildString {
                appendLine("setAttribute() rejected readable tags:")
                writeErrors.forEach { appendLine("  $it") }
            }
        }

        writer.saveAttributes()

        check(file.exists() && file.length() > 0L) {
            "$assetName became empty or disappeared after saveAttributes()"
        }

        // The important check: use completely new instances after saving.
        val afterFromFile = ExifInterface(file).toMetadata().toMap()
        val afterFromStream = file.inputStream().buffered().use { input ->
            ExifInterface(input).toMetadata().toMap()
        }

        val failures = mutableListOf<String>()

        before.forEach { (tag, expected) ->
            compareTag(
                source = "File constructor",
                tag = tag.key,
                expected = expected,
                actual = afterFromFile[tag],
                failures = failures
            )
            compareTag(
                source = "InputStream constructor",
                tag = tag.key,
                expected = expected,
                actual = afterFromStream[tag],
                failures = failures
            )
        }

        check(failures.isEmpty()) {
            buildString {
                appendLine("Metadata changed after save and fresh ExifInterface read:")
                failures.forEach { appendLine("  $it") }
                appendLine("Before tag count: ${before.size}")
                appendLine("After File tag count: ${afterFromFile.size}")
                append("After InputStream tag count: ${afterFromStream.size}")
            }
        }

        return before.size * 2
    }

    private fun compareTag(
        source: String,
        tag: String,
        expected: String,
        actual: String?,
        failures: MutableList<String>
    ) {
        if (tag in VOLATILE_LAYOUT_OFFSET_TAGS) {
            // These values are physical locations inside the rewritten container.
            // Their numeric value may legitimately change, but the tag must survive.
            if (actual == null) {
                failures += "$source: $tag disappeared; before=${expected.quoted()}"
            }
            return
        }

        if (actual != expected) {
            failures += buildString {
                append(source)
                append(": ")
                append(tag)
                append(" expected=")
                append(expected.quoted())
                append(", actual=")
                append(actual.quoted())
            }
        }
    }

    private fun copyAssetToWritableFile(assetName: String): File {
        val destination = File(workingDirectory, assetName)
        destination.parentFile?.mkdirs()

        context.assets.open("$ASSET_DIRECTORY/$assetName").use { input ->
            destination.outputStream().buffered().use(input::copyTo)
        }

        check(destination.length() > 0L) {
            "Copied test asset is empty: $assetName"
        }

        return destination
    }

    private fun isSupportedTestImage(name: String): Boolean =
        name.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase() in SUPPORTED_EXTENSIONS

    private fun String?.quoted(): String = when (this) {
        null -> "<null>"
        else -> "\"${replace("\n", "\\n").replace("\r", "\\r")}\""
    }

    private companion object {
        const val TAG = "ExifRoundTripTest"
        const val ASSET_DIRECTORY = "metadata_round_trip"

        val SUPPORTED_EXTENSIONS = setOf(
            "jpg",
            "jpeg",
            "png",
            "webp",
            "avif",
            "heic",
            "heif",
            "jxl",
            "tif",
            "tiff",
            "jp2",
            "j2k"
        )

        /**
         * Physical offsets are allowed to change when a container is rewritten.
         * We still verify that these tags remain readable after saving.
         */
        val VOLATILE_LAYOUT_OFFSET_TAGS = setOf(
            "StripOffsets",
            "StripByteCounts",
            "TileOffsets",
            "TileByteCounts",
            "JPEGInterchangeFormat",
            "JPEGInterchangeFormatLength",
            "SubIFDPointer",
            "ExifIFDPointer",
            "GPSInfoIFDPointer",
            "InteroperabilityIFDPointer"
        )
    }
}

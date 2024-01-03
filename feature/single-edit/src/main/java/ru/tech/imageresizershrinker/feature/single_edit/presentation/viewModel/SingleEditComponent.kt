package ru.tech.imageresizershrinker.feature.single_edit.presentation.viewModel

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.exifinterface.media.ExifInterface
import com.arkivanov.decompose.ComponentContext
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.coredomain.image.ImageManager
import ru.tech.imageresizershrinker.coredomain.image.Metadata
import ru.tech.imageresizershrinker.coredomain.model.DomainAspectRatio
import ru.tech.imageresizershrinker.coredomain.model.ImageData
import ru.tech.imageresizershrinker.coredomain.model.ImageFormat
import ru.tech.imageresizershrinker.coredomain.model.ImageInfo
import ru.tech.imageresizershrinker.coredomain.model.IntegerSize
import ru.tech.imageresizershrinker.coredomain.model.Preset
import ru.tech.imageresizershrinker.coredomain.model.ResizeType
import ru.tech.imageresizershrinker.coredomain.saving.FileController
import ru.tech.imageresizershrinker.coredomain.saving.SaveResult
import ru.tech.imageresizershrinker.coredomain.saving.model.ImageSaveTarget
import ru.tech.imageresizershrinker.coreui.extension.coroutineScope
import ru.tech.imageresizershrinker.coreui.model.UiPathPaint
import ru.tech.imageresizershrinker.coreui.utils.state.update

class SingleEditComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    private val fileController: FileController,
    private val imageManager: ImageManager<Bitmap, ExifInterface>
) : ComponentContext by componentContext {

    private val _originalSize: MutableState<IntegerSize?> = mutableStateOf(null)
    val originalSize by _originalSize

    private val _erasePaths = mutableStateOf(listOf<UiPathPaint>())
    val erasePaths: List<UiPathPaint> by _erasePaths

    private val _eraseLastPaths = mutableStateOf(listOf<UiPathPaint>())
    val eraseLastPaths: List<UiPathPaint> by _eraseLastPaths

    private val _eraseUndonePaths = mutableStateOf(listOf<UiPathPaint>())
    val eraseUndonePaths: List<UiPathPaint> by _eraseUndonePaths

    private val _drawPaths = mutableStateOf(listOf<UiPathPaint>())
    val drawPaths: List<UiPathPaint> by _drawPaths

    private val _drawLastPaths = mutableStateOf(listOf<UiPathPaint>())
    val drawLastPaths: List<UiPathPaint> by _drawLastPaths

    private val _drawUndonePaths = mutableStateOf(listOf<UiPathPaint>())
    val drawUndonePaths: List<UiPathPaint> by _drawUndonePaths

    private val _filterList = mutableStateOf(listOf<ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter<*>>())
    val filterList by _filterList

    private val _selectedAspectRatio: MutableState<DomainAspectRatio> =
        mutableStateOf(DomainAspectRatio.Free)
    val selectedAspectRatio by _selectedAspectRatio

    private val _cropProperties = mutableStateOf(
        CropDefaults.properties(
            cropOutlineProperty = CropOutlineProperty(
                OutlineType.Rect,
                RectCropShape(
                    id = 0,
                    title = OutlineType.Rect.name
                )
            ),
            fling = true
        )
    )
    val cropProperties by _cropProperties

    private val _exif: MutableState<ExifInterface?> = mutableStateOf(null)
    val exif by _exif

    private val _uri: MutableState<Uri> = mutableStateOf(Uri.EMPTY)
    val uri: Uri by _uri

    private val _internalBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val initialBitmap by _internalBitmap

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    private val _previewBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val previewBitmap: Bitmap? by _previewBitmap

    private val _imageInfo: MutableState<ImageInfo> = mutableStateOf(ImageInfo())
    val imageInfo: ImageInfo by _imageInfo

    private val _isImageLoading: MutableState<Boolean> = mutableStateOf(false)
    val isImageLoading: Boolean by _isImageLoading

    private val _showWarning: MutableState<Boolean> = mutableStateOf(false)
    val showWarning: Boolean by _showWarning

    private val _shouldShowPreview: MutableState<Boolean> = mutableStateOf(true)
    val shouldShowPreview by _shouldShowPreview

    private val _presetSelected: MutableState<Preset> = mutableStateOf(Preset.None)
    val presetSelected by _presetSelected

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving by _isSaving

    private var job: Job? = null

    private suspend fun checkBitmapAndUpdate(resetPreset: Boolean = false) {
        if (resetPreset) {
            _presetSelected.value = Preset.None

        }
        _bitmap.value?.let { bmp ->
            val preview = updatePreview(bmp)
            _previewBitmap.value = null
            _shouldShowPreview.value = imageManager.canShow(preview)
            if (shouldShowPreview) _previewBitmap.value = preview
        }
    }

    private fun debouncedImageCalculation(
        onFinish: suspend () -> Unit = {},
        delay: Long = 600L,
        action: suspend () -> Unit
    ) {
        job?.cancel()
        _isImageLoading.value = false
        job = coroutineScope.launch {
            _isImageLoading.value = true
            delay(delay)
            action()
            _isImageLoading.value = false
            onFinish()
        }
    }

    private var savingJob: Job? = null
    fun saveBitmap(
        onComplete: (result: SaveResult) -> Unit,
    ) = coroutineScope.launch {
        withContext(Dispatchers.IO) {
            _isSaving.value = true
            bitmap?.let { bitmap ->
                onComplete(
                    fileController.save(
                        saveTarget = ImageSaveTarget(
                            imageInfo = imageInfo,
                            metadata = exif,
                            originalUri = uri.toString(),
                            sequenceNumber = null,
                            data = imageManager.compress(
                                ImageData(
                                    image = bitmap,
                                    imageInfo = imageInfo,
                                    metadata = exif,
                                )
                            )
                        ),
                        keepMetadata = true
                    )
                )
            }
            _isSaving.value = false
        }
    }.also {
        _isSaving.value = false
        savingJob?.cancel()
        savingJob = it
    }

    private suspend fun updatePreview(
        bitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.IO) {
        return@withContext imageInfo.run {
            _showWarning.value = width * height * 4L >= 10_000 * 10_000 * 3L
            imageManager.createFilteredPreview(
                image = bitmap,
                imageInfo = this,
                onGetByteCount = {
                    _imageInfo.value = _imageInfo.value.copy(sizeInBytes = it)
                }
            )
        }
    }

    private fun setBitmapInfo(newInfo: ImageInfo) {
        if (_imageInfo.value != newInfo) {
            _imageInfo.value = newInfo
            debouncedImageCalculation {
                checkBitmapAndUpdate()
            }
        }
    }

    fun resetValues(newBitmapComes: Boolean = false) {
        _imageInfo.value = ImageInfo(
            width = _internalBitmap.value?.width ?: 0,
            height = _internalBitmap.value?.height ?: 0,
            imageFormat = imageInfo.imageFormat
        )
        if (newBitmapComes) {
            _bitmap.value = _internalBitmap.value
        }
        debouncedImageCalculation {
            checkBitmapAndUpdate(
                resetPreset = true
            )
        }
    }

    fun updateBitmapAfterEditing(bitmap: Bitmap?) {
        coroutineScope.launch {
            val size = bitmap?.let { it.width to it.height }
            _originalSize.value = size?.run { IntegerSize(width = first, height = second) }
            _bitmap.value = imageManager.scaleUntilCanShow(bitmap)
            resetValues()
            _imageInfo.value = _imageInfo.value.copy(
                width = size?.first ?: 0,
                height = size?.second ?: 0
            )
        }
    }

    fun rotateBitmapLeft() {
        _imageInfo.value = _imageInfo.value.run {
            copy(
                rotationDegrees = _imageInfo.value.rotationDegrees - 90f,
                height = width,
                width = height
            )
        }
        debouncedImageCalculation {
            checkBitmapAndUpdate()
        }
    }

    fun rotateBitmapRight() {
        _imageInfo.value = _imageInfo.value.run {
            copy(
                rotationDegrees = _imageInfo.value.rotationDegrees + 90f,
                height = width,
                width = height
            )
        }
        debouncedImageCalculation {
            checkBitmapAndUpdate()
        }
    }

    fun flipImage() {
        _imageInfo.value = _imageInfo.value.copy(isFlipped = !_imageInfo.value.isFlipped)
        debouncedImageCalculation {
            checkBitmapAndUpdate()
        }
    }

    fun updateWidth(width: Int) {
        if (_imageInfo.value.width != width) {
            _imageInfo.value = _imageInfo.value.copy(width = width)
            debouncedImageCalculation {
                checkBitmapAndUpdate(
                    resetPreset = true
                )
            }
        }
    }

    fun updateHeight(height: Int) {
        if (_imageInfo.value.height != height) {
            _imageInfo.value = _imageInfo.value.copy(height = height)
            debouncedImageCalculation {
                checkBitmapAndUpdate(
                    resetPreset = true
                )
            }
        }
    }

    fun setQuality(quality: Float) {
        if (_imageInfo.value.quality != quality) {
            _imageInfo.value = _imageInfo.value.copy(quality = quality.coerceIn(0f, 100f))
            debouncedImageCalculation {
                checkBitmapAndUpdate()
            }
        }
    }

    fun setImageFormat(imageFormat: ImageFormat) {
        if (_imageInfo.value.imageFormat != imageFormat) {
            _imageInfo.value = _imageInfo.value.copy(imageFormat = imageFormat)
            debouncedImageCalculation {
                checkBitmapAndUpdate(
                    resetPreset = _presetSelected.value == Preset.Telegram && imageFormat != ImageFormat.Png
                )
            }
        }
    }

    fun setResizeType(type: ResizeType) {
        if (_imageInfo.value.resizeType != type) {
            _imageInfo.value = _imageInfo.value.copy(resizeType = type)
            debouncedImageCalculation {
                checkBitmapAndUpdate(
                    resetPreset = false
                )
            }
        }
    }

    fun setUri(uri: Uri) {
        _uri.value = uri
    }

    fun decodeBitmapByUri(
        uri: Uri,
        onError: (Throwable) -> Unit
    ) {
        _isImageLoading.value = true
        imageManager.getImageAsync(
            uri = uri.toString(),
            originalSize = true,
            onGetImage = ::setImageData,
            onError = {
                _isImageLoading.value = false
                onError(it)
            }
        )
    }

    private fun setImageData(imageData: ImageData<Bitmap, ExifInterface>) {
        job?.cancel()
        _isImageLoading.value = false
        job = coroutineScope.launch {
            _isImageLoading.value = true
            updateExif(imageData.metadata)
            val bitmap = imageData.image
            val size = bitmap.width to bitmap.height
            _originalSize.value = size.run { IntegerSize(width = first, height = second) }
            _bitmap.value =
                imageManager.scaleUntilCanShow(bitmap).also { _internalBitmap.value = it }
            resetValues(true)
            _imageInfo.value = imageData.imageInfo.copy(
                width = size.first,
                height = size.second
            )
            checkBitmapAndUpdate(
                resetPreset = _presetSelected.value == Preset.Telegram && imageData.imageInfo.imageFormat != ImageFormat.Png
            )
            _isImageLoading.value = false
        }
    }

    fun shareBitmap(onComplete: () -> Unit) {
        _isSaving.value = false
        savingJob?.cancel()
        savingJob = coroutineScope.launch {
            _isSaving.value = true
            imageManager.shareImages(
                uris = listOf(_uri.value.toString()),
                imageLoader = {
                    imageManager.getImage(uri = uri.toString())?.image?.let {
                        ImageData(
                            image = it,
                            imageInfo = imageInfo
                        )
                    }
                },
                onProgressChange = { onComplete() }
            )
            _isSaving.value = false
        }
    }

    fun canShow(): Boolean = bitmap?.let { imageManager.canShow(it) } ?: false

    fun setPreset(preset: Preset) {
        setBitmapInfo(
            imageManager.applyPresetBy(
                image = bitmap,
                preset = preset,
                currentInfo = imageInfo
            )
        )
        _presetSelected.value = preset
    }

    fun clearExif() {
        val t = _exif.value
        Metadata.metaTags.forEach {
            t?.setAttribute(it, null)
        }
        _exif.value = t
    }

    private fun updateExif(exifInterface: ExifInterface?) {
        _exif.value = exifInterface
    }

    fun removeExifTag(tag: String) {
        val exifInterface = _exif.value
        exifInterface?.setAttribute(tag, null)
        updateExif(exifInterface)
    }

    fun updateExifByTag(tag: String, value: String) {
        val exifInterface = _exif.value
        exifInterface?.setAttribute(tag, value)
        updateExif(exifInterface)
    }

    fun setCropAspectRatio(
        domainAspectRatio: DomainAspectRatio,
        aspectRatio: AspectRatio
    ) {
        _cropProperties.value = _cropProperties.value.copy(
            aspectRatio = aspectRatio.takeIf {
                it != AspectRatio.Original
            } ?: _bitmap.value?.let {
                AspectRatio(it.width.toFloat() / it.height)
            } ?: aspectRatio,
            fixedAspectRatio = domainAspectRatio != DomainAspectRatio.Free
        )
        _selectedAspectRatio.update { domainAspectRatio }
    }

    fun setCropMask(cropOutlineProperty: CropOutlineProperty) {
        _cropProperties.value =
            _cropProperties.value.copy(cropOutlineProperty = cropOutlineProperty)
    }

    suspend fun loadImage(uri: Uri): Bitmap? = imageManager.getImage(data = uri)

    fun getImageManager(): ImageManager<Bitmap, ExifInterface> = imageManager

    fun <T : Any> updateFilter(
        value: T,
        index: Int,
        showError: (Throwable) -> Unit
    ) {
        val list = _filterList.value.toMutableList()
        kotlin.runCatching {
            list[index] = list[index].copy(value)
            _filterList.value = list
        }.exceptionOrNull()?.let {
            showError(it)
            list[index] = list[index].newInstance()
            _filterList.value = list
        }
    }

    fun updateOrder(value: List<ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter<*>>) {
        _filterList.value = value
    }

    fun addFilter(filter: ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter<*>) {
        _filterList.value = _filterList.value + filter
    }

    fun removeFilterAtIndex(index: Int) {
        _filterList.value = _filterList.value.toMutableList().apply {
            removeAt(index)
        }
    }

    fun clearFilterList() {
        _filterList.value = listOf()
    }

    fun calculateScreenOrientationBasedOnBitmap(bitmap: Bitmap?): Int {
        if (bitmap == null) return ActivityInfo.SCREEN_ORIENTATION_USER
        val imageRatio = bitmap.width / bitmap.height.toFloat()
        return if (imageRatio <= 1.05f) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    fun clearDrawing(canUndo: Boolean = false) {
        coroutineScope.launch {
            delay(500L)
            _drawLastPaths.value = if (canUndo) drawPaths else listOf()
            _drawPaths.value = listOf()
            _drawUndonePaths.value = listOf()
        }
    }

    fun undoDraw() {
        if (drawPaths.isEmpty() && drawLastPaths.isNotEmpty()) {
            _drawPaths.value = drawLastPaths
            _drawLastPaths.value = listOf()
            return
        }
        if (drawPaths.isEmpty()) return

        val lastPath = drawPaths.last()

        _drawPaths.update { it - lastPath }
        _drawUndonePaths.update { it + lastPath }
    }

    fun redoDraw() {
        if (drawUndonePaths.isEmpty()) return

        val lastPath = drawUndonePaths.last()
        _drawPaths.update { it + lastPath }
        _drawUndonePaths.update { it - lastPath }
    }

    fun addPathToDrawList(pathPaint: UiPathPaint) {
        _drawPaths.update { it + pathPaint }
        _drawUndonePaths.value = listOf()
    }

    fun clearErasing(canUndo: Boolean = false) {
        coroutineScope.launch {
            delay(250L)
            _eraseLastPaths.value = if (canUndo) erasePaths else listOf()
            _erasePaths.value = listOf()
            _eraseUndonePaths.value = listOf()
        }
    }

    fun undoErase() {
        if (erasePaths.isEmpty() && eraseLastPaths.isNotEmpty()) {
            _erasePaths.value = eraseLastPaths
            _eraseLastPaths.value = listOf()
            return
        }
        if (erasePaths.isEmpty()) return

        val lastPath = erasePaths.last()

        _erasePaths.update { it - lastPath }
        _eraseUndonePaths.update { it + lastPath }
    }

    fun redoErase() {
        if (eraseUndonePaths.isEmpty()) return

        val lastPath = eraseUndonePaths.last()
        _erasePaths.update { it + lastPath }
        _eraseUndonePaths.update { it - lastPath }
    }

    fun addPathToEraseList(pathPaint: UiPathPaint) {
        _erasePaths.update { it + pathPaint }
        _eraseUndonePaths.value = listOf()
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): SingleEditComponent
    }

}

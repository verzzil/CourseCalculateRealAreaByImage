package com.xannanov.course

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.common.util.concurrent.ListenableFuture
import com.xannanov.course.databinding.ActivityCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Нахождение расстояния до объекта
 * f = (w * d) / W;
 * d = (f * W) / w;
 * w = (f * W) / d;
 *  d - расстояние до предмеда; w - длина объекта в пикселях; W - реальная длина объекта в сантиметрах; f - фокальное расстояние
 */

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private var currentDistance: Int = 170
    private var currentCameraId: String = ""
    private var currentFocalLength: Float = 0f
    private var currentResolution: Size = Size(9216, 6912)
    private var bitmapResolution: Size = Size(9216, 6912)
    private var sensorPhysicalSize: SizeF = SizeF(7.42f, 5.56f)
    private var sensorPxSize: Size? = null
    private var howManyPxInMm = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phMode.takePicture.onActionUpListener = {
            capturePicture()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding.selectMode.btnRephoto.setOnClickListener {
            showPhMode()
        }

//        binding.selectMode.selectionCanvas.touchUpListener = {
//            binding.selectMode.btnRephoto.isVisible = false
//            binding.selectMode.gAreaSelection.isVisible = true
//        }

        binding.selectMode.btnGoSelect.setOnClickListener {
            showSelection()
//            binding.selectMode.gCardSelection.isVisible = false
//            binding.selectMode.gAreaSelection.isVisible = true
//            binding.selectMode.btnRephoto.isVisible = true
        }

        binding.selectMode.btnReset.setOnClickListener {
            binding.selectMode.selectionCanvas.reset()
        }

        binding.selectMode.btnCalculateArea.setOnClickListener {
            val mmImPx = binding.selectMode.cardSelectoinCanvas.getMmInOnePx()

            val areaInPx = binding.selectMode.selectionCanvas.calculateAreaInPixels()

            val result = areaInPx * mmImPx * mmImPx

            AlertDialog.Builder(this).apply {
                setTitle("Результат")
                setMessage("Выделенная область составляет $result мм²")
                setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }

        /*binding.imageCaptureButton.setOnClickListener { capturePicture() }
        binding.btnCalculateResolution.setOnClickListener {

            val temp = (binding.iv.drawable as BitmapDrawable).bitmap
            Log.i("asdfasdf", "-------- ${temp.width} ${temp.height}")

            val wOnSensor = sensorPhysicalSize.width * binding.cardSelectoinCanvas.selectionWidth() / binding.cardSelectoinCanvas.width

            Log.i("asdfasdf", "focal length ${currentFocalLength}")
            Log.i("asdfasdf", "imageView width ${binding.iv.width}   canvas width ${binding.cardSelectoinCanvas.canvasWidth()}   selected width ${binding.cardSelectoinCanvas.selectionWidth()}")
            Log.i("asdfasdf", "sensor width ${sensorPhysicalSize.width} width card on sensor $wOnSensor")
            Log.i("asdfasdf", "rrrrrr ${currentFocalLength * DEFAULT_CARD_WIDTH / wOnSensor / 10}")

        }*/
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun showCardSelection() {
        with(binding) {
            phMode.root.isVisible = false
            selectMode.root.isVisible = true
            selectMode.gCardSelection.isVisible = true
            selectMode.gAreaSelection.isVisible = false
        }
    }

    private fun showPhMode() {
        with(binding) {
            phMode.root.isVisible = true
            selectMode.root.isVisible = false
            selectMode.selectionCanvas.reset()
            selectMode.cardSelectoinCanvas.reset()
        }
    }

    private fun showSelection() {
        with(binding) {
            phMode.root.isVisible = false
            selectMode.root.isVisible = true
            selectMode.gCardSelection.isVisible = false
            selectMode.gAreaSelection.isVisible = true
        }
    }

    private fun calculateDistance(
        focalLength: Float,
        realSize: Float,
        sizeOfOutputImage: Int,
        sizeOfSensor: Float,
        sizeOfObjectOnImage: Float
    ) {
        val result =
            (focalLength * realSize * sizeOfOutputImage.toFloat()) / (sizeOfSensor * sizeOfObjectOnImage)
        Log.i("asdfasdf", "${result / 10f}")
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProvideFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)

        cameraProvideFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProvideFuture.get()

            val preview = Preview.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build()
                .also {
                    it.setSurfaceProvider(binding.phMode.preview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .setBufferFormat(ImageFormat.JPEG)
                .build()

            val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /*private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }*/

    @ExperimentalGetImage
    private fun capturePicture() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val img = image.image ?: return

                    val buffer = img.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }

                    val m: Matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    image.close()
                    val bit = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val bitmap = Bitmap.createBitmap(bit, 0, 0, bit.width, bit.height, m, true)
                    binding.selectMode.iv.setImageBitmap(bitmap)

                    showCardSelection()

                    setCameraId(imageCapture.camera?.cameraInfoInternal?.cameraId)
                    defineResolutionInfo(bitmap)
                    defineFocalLength()

                    cameraExecutor.shutdown()
                }
            }
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setCameraId(newId: String?) = newId?.let { currentCameraId = it }

    private fun defineFocalLength() {
        (getSystemService(CAMERA_SERVICE) as CameraManager).let { cameraManager ->
            currentFocalLength = cameraManager
                .getCameraCharacteristics(currentCameraId)
                .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.get(0) ?: 0f

            sensorPhysicalSize = cameraManager
                .getCameraCharacteristics(currentCameraId)
                .get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: SizeF(1f, 1f)

            sensorPxSize = cameraManager
                .getCameraCharacteristics(currentCameraId)
                .get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        }
    }

    private fun defineResolutionInfo(bitmap: Bitmap) {
        imageCapture?.resolutionInfo
        currentResolution = imageCapture?.resolutionInfo?.resolution ?: currentResolution
        bitmapResolution = Size(bitmap.width, bitmap.height)
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        private const val DEFAULT_CARD_WIDTH = 86f
        private const val DEFAULT_CARD_HEIGHT = 54f
    }
}

package com.xannanov.course

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.view.Surface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.internal.compat.CameraCharacteristicsCompat
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.pow

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
    private var currentResolution: Size = Size(0, 0)
    private var sensorSize: SizeF? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        binding.imageCaptureButton.setOnClickListener { capturePicture() }
        binding.btnCalculateResolution.setOnClickListener {
            val wOfCardPx = binding.cardSelectoinCanvas.selectionWidth()
            var wOnSensor = 0f

            sensorSize?.width?.let {
                wOnSensor = wOfCardPx * it / currentResolution.width
            }

            Log.i(
                "asdfasdf",
                "width in pixels $wOfCardPx \t focal length $currentFocalLength \t width in mm $STANDART_CARD_WIDTH"
            )

            Log.i(
                "asdfasdf",
                "result ${(STANDART_CARD_WIDTH * currentFocalLength / wOnSensor) / 10}"
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
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
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
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

    private fun takePhoto() {
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
    }

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

                    setCameraId(imageCapture.camera?.cameraInfoInternal?.cameraId)
                    defineResolutionInfo()
                    defineFocalLength()
                    val img = image.image ?: return

                    val buffer = img.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }

                    val m: Matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    image.close()
                    val bit = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val bitmap = Bitmap.createBitmap(bit, 0, 0, bit.width, bit.height, m, true)
                    binding.iv.setImageBitmap(bitmap)

                    with(binding) {
                        iv.isVisible = true
                        viewFinder.isVisible = false
                        cardSelectoinCanvas.isVisible = true
                        btnCalculateResolution.isVisible = true
                        imageCaptureButton.isVisible = false
                    }
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
        currentFocalLength = (getSystemService(CAMERA_SERVICE) as CameraManager)
            .getCameraCharacteristics(currentCameraId)
            .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.get(0) ?: 0f

        sensorSize = (getSystemService(CAMERA_SERVICE) as CameraManager)
            .getCameraCharacteristics(currentCameraId)
            .get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
    }

    private fun defineResolutionInfo() {
        imageCapture?.resolutionInfo
        currentResolution = imageCapture?.resolutionInfo?.resolution ?: currentResolution
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
        private const val STANDART_CARD_WIDTH = 86
    }
}

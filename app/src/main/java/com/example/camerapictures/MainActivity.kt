package com.example.camerapictures

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.camerapictures.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 카메라 원본 크기 저장
 * https://developer.android.com/training/camera/photobasics?hl=ko
 */
class MainActivity : AppCompatActivity() {
    companion object {
        val TAG1 = "CAMERA_RESULT:"
    }

    private lateinit var binding: ActivityMainBinding
    private var imgUri: Uri? = null
    private lateinit var currentPhotoPath: String

    private val permissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        if(result) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.e(TAG1, "$imgUri")
        Log.e(TAG1, "$result")
        result.data?.apply {
            Log.e(TAG1, "${result.data}")
            Log.e(TAG1, "${result.data!!.data}")
            binding.ivIMG.setImageURI(imgUri)
        }
    }

    private val albumResult = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
        result?.also {
            binding.ivIMG.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnPhoto.setOnClickListener {
            AlertDialog.Builder(this@MainActivity).apply {
                setTitle("선택")
                setItems(arrayOf("카메라", "앨범")) { _, which ->
                    when(which) {
                        0 -> cameraChoose()
                        1 -> albumChoose()
                    }
                }
                show()
            }
        }
    }

    private fun cameraChoose() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val photoFIle = createImageFile()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFIle?.also {
                val photoUri = FileProvider.getUriForFile(
                    this, "com.example.camerapictures.fileprovider", it
                )
                imgUri = photoUri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                cameraResult.launch(intent)
            }
        } else {
            permissionResult.launch(Manifest.permission.CAMERA)
        }
    }
    private fun albumChoose() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            albumResult.launch("image/*")
        } else {
            permissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}
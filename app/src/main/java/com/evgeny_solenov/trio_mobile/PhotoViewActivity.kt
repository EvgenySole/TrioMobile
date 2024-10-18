package com.example.testandro6

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.example.testandro6.databinding.ActivityMainBinding
import com.example.testandro6.databinding.ActivityPhotoViewBinding


class PhotoViewActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPhotoViewBinding
    private var photoView: Bitmap? = null
    var thre: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        val imageName = extras!!.getString("picture")
        Log.e("PhotoActivity", "ImageName - $imageName, PhotoView - $photoView")
        loadImage(imageName)
        thre = Thread{
        Thread.sleep(2000)
            runOnUiThread {
            setContent {
                Log.e("PhotoActivity", "SetContent, PhotoView - $photoView")
                var ch = PhotoView()
                ch.PhotoDraw(photoView)
                photoView = null
            }
            }
        }
        thre?.start()
    }
    private fun loadImage(eventImage: String?){
        var url = "http://${SendData.IPSERVER}:${SendData.PORTDB}/images/db/$eventImage"
        val queue = Volley.newRequestQueue(this)
        var request = ImageRequest(url, {
            photoView = it
            Log.e("PhotoActivity", "Image Loaded")
        }, 0, 0, null,
            {
                it.printStackTrace()
            })
        Log.e("PhotoActivity", "Request Add, PhotoView - $photoView")
        queue.add(request)
    }

    override fun onDestroy() {
        Log.e("PhotoActivity", "OnDestroy, PhotoView - $photoView")
        photoView = null
        thre?.interrupt()
        super.onDestroy()
        finish()
    }
}
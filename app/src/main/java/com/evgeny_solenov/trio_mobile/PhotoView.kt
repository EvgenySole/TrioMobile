package com.example.testandro6

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class PhotoView {

    @SuppressLint("SuspiciousIndentation")
    @Composable
    fun PhotoDraw(bmp: Bitmap?) {
        // set up all transformation states
        var scale by remember { mutableStateOf(1f) }
       // var rotation by remember { mutableStateOf(0f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale *= zoomChange
           // rotation += rotationChange
            offset += offsetChange*scale
        }

        Surface (modifier = with (Modifier){
            background(Color.Black)
        }, color = Color.Black){
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
//                    bitmap = ImageBitmap.imageResource(R.drawable.subaru_impreza_wrx_1),
                    contentDescription = "233",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        // apply other transformations like rotation and zoom
                        // on the pizza slice emoji
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            //rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        // add transformable to listen to multitouch transformation events
                        // after offset
                        .transformable(state = state)
                        .fillMaxSize()
                )
            }
        }
    }
}
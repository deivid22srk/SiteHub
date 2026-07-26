package com.deivid22srk.sitehub.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun ImageCropper(
    imageSource: Any,
    onCropComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(imageSource) {
        isLoading = true
        hasError = false
        try {
            val bmp = withContext(Dispatchers.IO) {
                when (imageSource) {
                    is Uri -> {
                        context.contentResolver.openInputStream(imageSource)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                    is String -> {
                        val conn = URL(imageSource).openConnection() as HttpURLConnection
                        conn.connectTimeout = 10000
                        conn.readTimeout = 10000
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                        conn.inputStream.use { BitmapFactory.decodeStream(it) }
                    }
                    else -> null
                }
            }
            bitmap = bmp
            if (bmp == null) hasError = true
        } catch (_: Exception) {
            hasError = true
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Carregando imagem...", style = MaterialTheme.typography.bodySmall)
            }
        } else if (hasError || bitmap == null) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Erro ao carregar imagem", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Voltar")
            }
        } else {
            val bmp = bitmap!!
            val imageBitmap = remember(bmp) { bmp.asImageBitmap() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val bmpW = bmp.width.toFloat()
                    val bmpH = bmp.height.toFloat()
                    val baseScale = size.minDimension / maxOf(bmpW, bmpH)
                    val drawScale = baseScale * scale
                    val drawW = bmpW * drawScale
                    val drawH = bmpH * drawScale
                    val cx = (size.width - drawW) / 2f + offset.x
                    val cy = (size.height - drawH) / 2f + offset.y

                    clipRect(0f, 0f, size.width, size.height) {
                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(cx.toInt(), cy.toInt()),
                            dstSize = IntSize(drawW.toInt(), drawH.toInt())
                        )
                    }

                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Zoom", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.5f..5f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val cropped = cropCenterSquare(bmp, scale, offset)
                                val file = saveBitmap(context, cropped, "icon_${System.currentTimeMillis()}.png")
                                withContext(Dispatchers.Main) {
                                    onCropComplete(file.absolutePath)
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recortar e aplicar")
                }
            }
        }
    }
}

private fun cropCenterSquare(source: Bitmap, scale: Float, offset: Offset): Bitmap {
    val scaledW = (source.width * scale).toInt().coerceAtLeast(1)
    val scaledH = (source.height * scale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(source, scaledW, scaledH, true)

    val cropSize = minOf(scaledW, scaledH)
    var x = ((scaledW - cropSize) / 2f - offset.x / scale).toInt()
    var y = ((scaledH - cropSize) / 2f - offset.y / scale).toInt()
    x = x.coerceIn(0, maxOf(0, scaledW - cropSize))
    y = y.coerceIn(0, maxOf(0, scaledH - cropSize))

    val cropped = Bitmap.createBitmap(scaled, x, y, cropSize, cropSize)
    return Bitmap.createScaledBitmap(cropped, 256, 256, true)
}

private fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String): File {
    val dir = File(context.filesDir, "icons")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}

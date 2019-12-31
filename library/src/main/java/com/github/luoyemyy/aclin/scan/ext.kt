package com.github.luoyemyy.aclin.scan

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import java.lang.Boolean.TRUE

typealias QrCodeCallback = (String) -> Unit

internal fun parseQrCode(image: ImageProxy?): String? {
    if (image != null && image.planes != null && image.planes.isNotEmpty()) {
        val y = image.planes[0].buffer
        val ya = ByteArray(y.limit() - y.position())
        y.get(ya)
        return parse(ya, image.width, image.height)
    }
    return null
}

internal fun parse(data: ByteArray, w: Int, h: Int): String? {
    val hints: Map<DecodeHintType, *> =
        mapOf(DecodeHintType.CHARACTER_SET to "utf-8",
              DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.QR_CODE,
              DecodeHintType.TRY_HARDER to TRUE)
    val scanSize = ((if (w > h) h else w) / 2 * QrCodeBuilder.SCAN_PERCENT).toInt()
    val rect = Rect(w / 2 - scanSize, h / 2 - scanSize, w / 2 + scanSize, h / 2 + scanSize)
    val source = PlanarYUVLuminanceSource(data, w, h, rect.left, rect.top, rect.width(), rect.height(), false)
    val binarizer = HybridBinarizer(source)
    val bitmap = BinaryBitmap(binarizer)
    return try {
        QRCodeReader().decode(bitmap, hints).text
    } catch (thr: Throwable) {
        Log.e("QrCode", "parse:  not found")
        null
    }
}


internal fun format(content: String, w: Int, h: Int): Bitmap {
    val hints = mapOf(EncodeHintType.CHARACTER_SET to "utf-8")
    val black = 0xFF000000.toInt()
    val white = 0xFFFFFFFF.toInt()
    val result = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, w, h, hints)
    val width = result.width
    val height = result.height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        val offset = y * width
        for (x in 0 until width) {
            pixels[offset + x] = if (result.get(x, y)) black else white
        }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return bitmap
}
package com.github.luoyemyy.aclin.file

import android.app.Application
import android.util.Log
import java.io.*

object IoManager {
    /**
     * 如果文件已存在，直接返回
     * @return
     */
    fun copyFromAsset(app: Application, type: FileManager.Type, source: String): File? {
        val file = FileManager(app).inner().file(type, source).also {
            when {
                it == null -> return null
                it.exists() -> return it
            }
        }
        return try {
            app.assets.open(source).use { input -> FileOutputStream(file).use { fos -> copy(input, fos) } }
            file
        } catch (e: Exception) {
            Log.e("FileManager", "copyFromAsset:  $e")
            null
        }
    }

    @Throws(IOException::class)
    fun copy(input: InputStream, os: OutputStream) {
        val bytes = ByteArray(1024)
        var len: Int
        do {
            len = input.read(bytes)
            if (len > 0) {
                os.write(bytes, 0, len)
            }
        } while (len != -1)
    }

}
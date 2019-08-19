@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.luoyemyy.aclin.file

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * test code

sb.append("\n                             log:${manager.log()}")
sb.append("\n                           image:${manager.image()}")
sb.append("\n                           voice:${manager.voice()}")
sb.append("\n                           video:${manager.video()}")
sb.append("\n                             apk:${manager.apk()}")
sb.append("\n                              db:${manager.db()}")

sb.append("\n inner                 customDir:${manager.inner().customDir(FileManager.LOG).absolutePath}")
sb.append("\n inner                customFile:${manager.inner().customFile(FileManager.LOG, filename).absolutePath}")

sb.append("\n inner                       dir:${manager.inner().dir(FileManager.LOG)?.absolutePath}")
sb.append("\n inner                      file:${manager.inner().file(FileManager.LOG, filename)?.absolutePath}")

sb.append("\n inner                  cacheDir:${manager.inner().cacheDir(FileManager.LOG)?.absolutePath}")
sb.append("\n inner                 cacheFile:${manager.inner().cacheFile(FileManager.LOG, filename)?.absolutePath}")

sb.append("\n outer                privateDir:${manager.outer().privateDir(FileManager.LOG)?.absolutePath}")
sb.append("\n outer               privateFile:${manager.outer().privateFile(FileManager.LOG, filename)?.absolutePath}")

sb.append("\n outer           privateCacheDir:${manager.outer().privateCacheDir(FileManager.LOG)?.absolutePath}")
sb.append("\n outer          privateCacheFile:${manager.outer().privateCacheFile(FileManager.LOG, filename)?.absolutePath}")

sb.append("\n outer                 publicDir:${manager.outer().publicDir(FileManager.LOG)?.absolutePath}")
sb.append("\n outer                publicFile:${manager.outer().publicFile(FileManager.LOG, filename)?.absolutePath}")

sb.append("\n outer           publicCustomDir:${manager.outer().publicCustomDir(FileManager.LOG)?.absolutePath}")
sb.append("\n outer          publicCustomFile:${manager.outer().publicCustomFile(FileManager.LOG, filename)?.absolutePath}")

sb.append("\n outer         publicStandardDir:${manager.outer().publicStandardDir(FileManager.PUBLIC_DIRECTORY_PICTURES)?.absolutePath}")
sb.append("\n outer        publicStandardFile:${manager.outer().publicStandardFile(FileManager.PUBLIC_DIRECTORY_PICTURES, filename, FileManager.SUFFIX_IMAGE)?.absolutePath}")

 */
class FileManager(val app: Application) {

    private val inner: Inner by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Inner(app) }
    private val outer: Outer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Outer(app) }

    fun inner(): Inner = inner
    fun outer(): Outer = outer

    /**
     * 生成一个当前时间的名称
     */
    fun getName(): String = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())

    /**
     * /data/user/0/${packageName}/files/log/${name}.txt
     */
    fun log(name: String = getName()): File? = inner.file(LOG, name)

    /**
     * /data/user/0/${packageName}/files/apk/${name}.apk
     */
    fun apk(name: String = getName()): File? = inner.file(APK, name)

    /**
     * /data/user/0/${packageName}/files/Pictures/${name}.jpg
     */
    fun image(name: String = getName()): File? = inner.file(IMAGE, name)

    /**
     * /data/user/0/${packageName}/files/voice/${name}.aac
     */
    fun voice(name: String = getName()): File? = inner.file(VOICE, name)

    /**
     * /data/user/0/${packageName}/files/Movies/${name}.mp4
     */
    fun video(name: String = getName()): File? = inner.file(VIDEO, name)

    /**
     * /data/user/0/${packageName}/files/database/${name}.db
     */
    fun db(name: String = getName()): File? = inner.file(DB, name)

    /**
     * uri 转换为 地址
     */
    fun getPathFromUri(uri: Uri?): String? {
        if (null == uri) return null
        val scheme = uri.scheme
        return when (scheme) {
            null -> uri.path
            ContentResolver.SCHEME_FILE -> uri.path
            ContentResolver.SCHEME_CONTENT -> {
                val cursor = app.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)
                var path: String? = null
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                        if (index > -1) {
                            path = cursor.getString(index)
                        }
                    }
                    cursor.close()
                }
                path
            }
            else -> null
        }
    }

    /**
     * 内部存储，（私有）
     */
    class Inner(val app: Application) {

        /**
         * 私有目录（包名下面自定义文件夹）
         * /data/user/0/${packageName}/app_${type.dir}
         */
        fun customDir(type: Type): File = app.getDir(type.dir, Context.MODE_PRIVATE)

        /**
         * 私有目录（包名下面自定义文件夹）的文件
         * /data/user/0/${packageName}/app_${type.dir}/${name}${type.suffix}
         */
        fun customFile(type: Type, name: String): File = File(customDir(type), "$name${type.suffix}")

        /**
         * 私有目录
         * /data/user/0/${packageName}/files/${type.dir}
         */
        fun dir(type: Type): File? = File(app.filesDir, type.dir).takeIf { it.exists() || it.mkdirs() }

        /**
         * 私有目录的文件
         * /data/user/0/${packageName}/files/${type.dir}/${name}${type.suffix}
         */
        fun file(type: Type, name: String): File? = dir(type)?.let { File(it, "$name${type.suffix}") }

        /**
         * 私有缓存目录
         * /data/user/0/${packageName}/cache/${type.dir}
         */
        fun cacheDir(type: Type): File? = File(app.cacheDir, type.dir).takeIf { it.exists() || it.mkdirs() }

        /**
         * 私有缓存目录的文件
         * /data/user/0/${packageName}/cache/${type.dir}/${name}${type.suffix}
         */
        fun cacheFile(type: Type, name: String): File? = cacheDir(type)?.let { File(it, "$name${type.suffix}") }

    }

    /**
     * 外部存储，（私有/公共）
     */
    class Outer(val app: Application) {

        /**
         * 私有目录
         * /storage/emulated/0/Android/data/${packageName}/files/${type.dir}
         */
        fun privateDir(type: Type): File? =
            if (isMounted()) app.getExternalFilesDir(type.dir).takeIf { it != null && (it.exists() || it.mkdirs()) }
            else null

        /**
         * 私有目录的文件
         * /storage/emulated/0/Android/data/${packageName}/files/${type.dir}/${name}${type.suffix}
         */
        fun privateFile(type: Type, name: String): File? = privateDir(type)?.let { File(it, "$name${type.suffix}") }

        /**
         * 私有缓存目录
         * /storage/emulated/0/Android/data/${packageName}/cache/${type.dir}
         */
        fun privateCacheDir(type: Type): File? = if (isMounted()) app.externalCacheDir.let { baseDir ->
            File(baseDir, type.dir).takeIf { it.exists() || it.mkdirs() }
        }
        else null

        /**
         * 私有缓存目录的文件
         * /storage/emulated/0/Android/data/${packageName}/cache/${type.dir}/${name}${type.suffix}
         */
        fun privateCacheFile(type: Type, name: String): File? =
            privateCacheDir(type)?.let { File(it, "$name${type.suffix}") }

        /**
         * 公共目录，包名（packageName）下的目录
         * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
         * /storage/emulated/0/${packageName}/${type.dir}
         */
        fun publicDir(type: Type): File? =
            if (isMounted() && hasPermission()) Environment.getExternalStorageDirectory().let { baseDir ->
                File(baseDir, "${app.packageName}${File.separator}${type.dir}").takeIf { it.exists() || it.mkdirs() }
            }
            else null

        /**
         * 公共目录，包名（packageName）下的目录的文件
         * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
         * /storage/emulated/0/${packageName}/${type.dir}/${name}${type.suffix}
         */
        fun publicFile(type: Type, name: String): File? = publicDir(type)?.let { File(it, "$name${type.suffix}") }

        /**
         * 公共目录，自定义的目录
         * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
         * /storage/emulated/0/${type.dir}
         */
        fun publicCustomDir(type: Type): File? =
            if (isMounted() && hasPermission()) Environment.getExternalStorageDirectory().let { baseDir ->
                File(baseDir, type.dir).takeIf { it.exists() || it.mkdirs() }
            }
            else null

        /**
         * 公共目录，自定义的目录的文件
         * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
         * /storage/emulated/0/${type.dir}/${name}${type.suffix}
         */
        fun publicCustomFile(type: Type, name: String): File? =
            publicCustomDir(type)?.let { File(it, "$name${type.suffix}") }

        /**
         * 公共目录，android定义的标准目录
         * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
         * @param dir      Environment.PUBLIC_DIRECTORY_*
         * /storage/emulated/0/${type}
         */
        fun publicStandardDir(dir: String): File? = if (isMounted() && hasPermission()) {
            Environment.getExternalStoragePublicDirectory(dir).takeIf { it.exists() || it.mkdirs() }
        } else null

        /**
         * 公共目录，android定义的标准目录下的文件
         * 需要权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
         * @param dir       Environment.PUBLIC_DIRECTORY_*
         * @param name      文件名 没有文件类型
         * @param suffix    文件类型
         * /storage/emulated/0/${type}/${name}${suffix}
         */
        fun publicStandardFile(dir: String, name: String, suffix: String): File? =
            publicStandardDir(dir)?.let { File(it, "$name$suffix") }

        private fun isMounted(): Boolean = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

        private fun hasPermission(): Boolean =
            app.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, android.os.Process.myPid(),
                    android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        /**
         * 文件后缀
         */
        const val SUFFIX_LOG = ".txt"
        const val SUFFIX_VOICE = ".aac"
        const val SUFFIX_VIDEO = ".mp4"
        const val SUFFIX_IMAGE = ".jpg"
        const val SUFFIX_DB = ".db"
        const val SUFFIX_APK = ".apk"
        const val SUFFIX_EMPTY = ""

        /**
         * 自定义的目录名称
         */
        const val DIRECTORY_FIle = "file"
        const val DIRECTORY_LOG = "log"
        const val DIRECTORY_VOICE = "voice"
        const val DIRECTORY_DATABASE = "database"
        const val DIRECTORY_APK = "apk"
        const val DIRECTORY_IMAGE = "image"
        const val DIRECTORY_VIDEO = "video"

        /**
         * 定义的一些常用文件保存的目录和文件类型关系
         */
        val FILE = Type(DIRECTORY_FIle, SUFFIX_EMPTY)
        val LOG = Type(DIRECTORY_LOG, SUFFIX_LOG)
        val VIDEO = Type(DIRECTORY_VIDEO, SUFFIX_VIDEO)
        val IMAGE = Type(DIRECTORY_IMAGE, SUFFIX_IMAGE)
        val VOICE = Type(DIRECTORY_VOICE, SUFFIX_VOICE)
        val DB = Type(DIRECTORY_DATABASE, SUFFIX_DB)
        val APK = Type(DIRECTORY_APK, SUFFIX_APK)

        private lateinit var instance: FileManager

        @JvmStatic
        fun init(app: Application) {
            instance = FileManager(app)
        }

        /**
        //class App : Application() {
        //    override fun onCreate() {
        //        super.onCreate()
        //        AppInfo.init(this) // 或者 FileManager.init(this)
        //    }
        //}
         */
        @JvmStatic
        fun getInstance(): FileManager = instance
    }

    data class Type(val dir: String, val suffix: String)

}
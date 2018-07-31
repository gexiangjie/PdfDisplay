package com.gxj1228.pdfdisplay.util

import okhttp3.ResponseBody
import java.io.*

/**
 * Created by gxj on 2018/2/8.
 */
object FileUtil {
    /**
     * 把pdf文件写到sd卡
     *
     * @param body
     * @return
     */
    fun writeFileToDisk(fileName: String, body: ResponseBody): String {
        try {
            val file = File(fileName)
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
                return fileName
            } catch (e: FileNotFoundException) {
                return ""
            } catch (e: IOException) {
                return ""
            } finally {
                if (outputStream != null) {
                    outputStream.close()
                }
                if (inputStream != null) {
                    inputStream.close()
                }
            }
        } catch (e: IOException) {
            return ""
        }

    }
}

package com.atoolkit.astorage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipOutputStream

/**
 * Description: 根据父目录和文件名创建文件
 * Author: summer
 *
 * @param dirPath 要创建文件所属的目录
 * @param fileName 要创建的文件名称
 *
 * @return 目标文件，为null表示未创建成功
 */
fun createFile(dirPath: String, fileName: String): File? {
    if (dirPath.isEmpty() || fileName.isEmpty()) {
        return null
    }
    try {
        val dirFile = File(dirPath)
        if (!dirFile.exists()) {
            // 如果路径有安全（无访问权限）问题，mkdirs会抛出SecurityException异常
            val isMkSuccess = dirFile.mkdirs()
            if (!isMkSuccess) {
                aLog?.v(TAG, "create dir failed, just return null")
                return null
            }
        }
        val targetFile = File(dirFile, fileName)
        if (!targetFile.exists()) {
            // 文件创建时可能会抛出IOException或SecurityException异常
            val isFileCreated = targetFile.createNewFile()
            if (!isFileCreated) {
                return null
            }
        }
        return targetFile
    } catch (e: Exception) {
        aLog?.w(
            TAG,
            "create file exception（dirPath=$dirPath, fileName=$fileName）, please check your path is correct and has permission to create",
            e
        )
    }
    return null
}

/**
 * Description: 删除指定路径的文件，如果路径是文件夹，则会递归删除其内部的子文件
 * Author: summer
 *
 * @param filePath 待删除的文件路径
 *
 * @return Boolean，true：删除成功，false：删除失败
 */
fun deleteFile(filePath: String): Boolean {
    if (filePath.isEmpty()) {
        // 空路径，直接返回失败
        aLog?.v(TAG, "filePath is empty, just return false")
        return false
    }
    try {
        val targetFile = File(filePath)
        if (targetFile.exists()) {
            return if (targetFile.isDirectory) {
                // 删除文件夹及其内部的文件
                targetFile.deleteRecursively()
            } else {
                targetFile.delete()
            }
        }
    } catch (e: Exception) {
        aLog?.w(
            TAG,
            "delete file failed(filePath=$filePath), please check the filePath is correct and has permission to delete",
            e
        )
    }
    return false
}

/**
 * Description: 重命名文件
 * Author: summer
 *
 * @param srcFilePath 待重命名的文件路径
 * @param destFilePath 重命名后的文件路径
 *
 * @return Boolean， ture：重命名成功，false：重命名失败
 */
fun renameFile(srcFilePath: String, destFilePath: String): Boolean {
    if (srcFilePath.isEmpty() || destFilePath.isEmpty()) {
        return false
    }
    val originFile = File(srcFilePath)
    if (!originFile.exists()) {
        aLog?.v(TAG, "The file(srcFilePath = $srcFilePath) is not exists, rename failed")
        return false
    }
    val targetFile = File(destFilePath)
    if (targetFile.exists()) {
        aLog?.v(TAG, "The file(destFilePath = $destFilePath) is exists, can't rename")
        return false
    }
    try {
        return originFile.renameTo(targetFile)
    } catch (e: Exception) {
        aLog?.w(TAG, "rename file(srcFilePath=$srcFilePath, destFilePath=$destFilePath) failed with exception", e)
    }
    return false
}

suspend fun zipFile(
    srcFilePath: String,
    zipFilePath: String? = null,
    password: String? = null,
    callback: ((Boolean, String) -> Unit)?
) {
    if (srcFilePath.isEmpty()) {
        callback?.invoke(false, "Zip failed, srcFilePath is empty")
        return
    }
    val srcFile = File(srcFilePath)
    val targetZipPath: String = zipFilePath ?: run {
        val srcParentPath = srcFile.parent
        val zipName = "${System.currentTimeMillis()}.zip"
        (srcParentPath?.plus(File.separator) ?: "./") + zipName
    }
    withContext(Dispatchers.IO) {
        if (srcFile.isDirectory){

        } else {

        }
    }
}

suspend fun unzipFile() {

}
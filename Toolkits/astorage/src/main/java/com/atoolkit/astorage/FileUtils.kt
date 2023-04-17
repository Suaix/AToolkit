package com.atoolkit.astorage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val BUFF_SIZE: Int = 8192
private const val MAC_IGNORE: String = "__MACOSX/"

/**
 * 非法文件，避免解压时被偷换目录，造成文件被覆盖，系统被攻击
 */
private const val ILLEGAL_NAME: String = "../"

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
fun deleteFileByPath(filePath: String): Boolean {
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

/**
 * Description: 压缩文件
 * Author: summer
 * @param srcFilePath 待压缩的文件路径
 * @param zipFilePath 压缩后的文件路径，默认为null，会压缩到跟源文件相同的路径下，以时间戳为名的zip文件；
 * @param callback 压缩结果回调，注意callback可能在子线程中被调用
 */
suspend fun zipFile(
    srcFilePath: String,
    zipFilePath: String? = null,
    callback: ((Boolean, String, File?) -> Unit)? = null
) {
    if (srcFilePath.isEmpty()) {
        callback?.invoke(false, "Zip failed, srcFilePath is empty", null)
        return
    }
    val srcFile = File(srcFilePath)
    val targetZipPath: String = if (zipFilePath.isNullOrEmpty()) {
        // 没有传压缩文件路径，使用默认路径：srcFilePath.parentPath+"/+System.currentTimeMillis()+".zip"
        val srcParentPath = srcFile.parent
        val zipName = "${System.currentTimeMillis()}.zip"
        (srcParentPath?.plus(File.separator) ?: "./") + zipName
    } else {
        zipFilePath
    }
    withContext(Dispatchers.IO) {
        var zos: ZipOutputStream? = null
        try {
            zos = ZipOutputStream(FileOutputStream(targetZipPath))
            aLog?.v(TAG, "targetZip is created = ${File(targetZipPath).exists()}")
            zipFileOrDirectory("", srcFile, zos)
            callback?.invoke(true, "zip file success", File(targetZipPath))
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.invoke(false, "zip file failed with exception: ${e.message}", null)
        } finally {
            zos?.close()
        }
    }
}

/**
 * Description: 对文件或文件夹进行压缩，如果是文件夹则进行循环压缩
 * Author: summer
 */
private fun zipFileOrDirectory(rootPath: String, srcFile: File, zos: ZipOutputStream) {
    val zipRootPath = rootPath + (if (rootPath.isEmpty()) "" else File.separator) + srcFile.name
    if (srcFile.isDirectory) {
        val fileList = srcFile.listFiles()
        if (fileList.isNullOrEmpty()) {
            val entry = ZipEntry("$zipRootPath/")
            zos.putNextEntry(entry)
            zos.closeEntry()
        } else {
            for (file in fileList) {
                aLog?.v(TAG, "file in list, path=${file.path}")
                zipFileOrDirectory(zipRootPath, file, zos)
            }
        }
    } else {
        var inputStream: BufferedInputStream? = null
        try {
            inputStream = BufferedInputStream(FileInputStream(srcFile))
            val entry = ZipEntry(zipRootPath)
            zos.putNextEntry(entry)
            val buffer = ByteArray(BUFF_SIZE)
            var len: Int
            while (inputStream.read(buffer, 0, BUFF_SIZE).also { len = it } != -1) {
                zos.write(buffer, 0, len)
            }
            zos.closeEntry()
        } catch (e: IOException) {
            throw e
        } finally {
            inputStream?.close()
        }
    }
}

/**
 * Description: 解压缩文件
 * Author: summer
 * @param zipFile 待加压的压缩文件
 * @param targetFolderPath 带解压到的文件夹路径
 * @param isUnzipWithParentDir 解压缩时是否将压缩文件本身作为父目录
 * @param callback 解压结果回调
 */
suspend fun unzipFile(
    zipFile: File,
    targetFolderPath: String,
    isUnzipWithParentDir: Boolean = true,
    callback: ((Boolean, String) -> Unit)? = null
) {
    if (targetFolderPath.isEmpty()) {
        callback?.invoke(false, "The target folder path is empty")
        return
    }
    withContext(Dispatchers.IO) {
        val targetFile = if (isUnzipWithParentDir) {
            val targetFileName = zipFile.name.let {
                val index = it.lastIndexOf(".")
                if (index <= 0) {
                    it
                } else {
                    it.substring(0, index)
                }
            }
            File(targetFolderPath, targetFileName)
        } else {
            File(targetFolderPath)
        }
        if (!targetFile.exists()) {
            targetFile.mkdirs()
        }
        // 创建ZipFile实例
        val zf = ZipFile(zipFile)
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))

        var zipEntry = zipInputStream.nextEntry
        var count = 1
        while (zipEntry != null) {
            val entryName = zipEntry.name
            if (entryName?.contains(MAC_IGNORE) == true || entryName?.contains(ILLEGAL_NAME) == true) {
                zipEntry = zipInputStream.nextEntry
                continue
            }
            val temp = File(targetFile, entryName)
            if (zipEntry.isDirectory) {
                // 是文件夹，创建文件夹
                temp.mkdirs()
                zipEntry = zipInputStream.nextEntry
                continue
            }
            // 如果文件有父级目录，且父级目录不存在，则创建父级目录
            val parentFile = temp.parentFile
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs()
            }
            // 开始读取文件流
            val buffer = ByteArray(BUFF_SIZE)
            val outputStream = FileOutputStream(temp)
            val inputStream = zf.getInputStream(zipEntry)
            var len = inputStream.read(buffer)
            while (len != -1) {
                outputStream.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }
            outputStream.close()
            inputStream.close()
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        callback?.invoke(true, "unzip file success!")
    }
}
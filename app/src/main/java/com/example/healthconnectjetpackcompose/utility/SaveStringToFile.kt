package com.example.healthconnectjetpackcompose.utility

import java.io.File

/**
 * Save new json file to cache directory.
 * @param json is the json string to be save
 * @param fileName is the name of the file. If there is a file of the same name, it will be overwritten
 * @param dir is the directory of the file in the device
 * @author Firdaus Abdullah
 * @since 1.0.0
 */
fun saveStringAsFile(json: String, fileName: String, dir: File) {
    val file = File(dir, fileName)
    try {
        // Overwrite the file with new data
        file.writeText(json)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
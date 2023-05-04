package io.github.takusan23.arobjviewer.screen

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val objFolder = context.getExternalFilesDir(null)?.resolve(OBJECTS_FOLDER)!!
    private val _objFileListFlow = MutableStateFlow<List<ObjFile>>(emptyList())

    val objFileListFlow = _objFileListFlow.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            objFolder.mkdir()
            _objFileListFlow.value = getObjectFolderContentList()
        }
    }

    fun addAllObjFile(uriList: List<@JvmSuppressWildcards Uri>) {
        val contentResolver = context.contentResolver
        viewModelScope.launch(Dispatchers.IO) {
            uriList.forEach { uri ->
                val fileName = getFileNameFromUri(uri)
                if (fileName.endsWith(".obj")) {
                    val newFile = objFolder.resolve(fileName)
                    newFile.outputStream().use { outputStream ->
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
            _objFileListFlow.value = getObjectFolderContentList()
        }
    }

    fun deleteObjFile(objFile: ObjFile) {
        viewModelScope.launch(Dispatchers.IO) {
            File(objFile.path).delete()
            _objFileListFlow.value = getObjectFolderContentList()
        }
    }

    private suspend fun getObjectFolderContentList(): List<ObjFile> = withContext(Dispatchers.IO) {
        return@withContext objFolder.listFiles()?.map { ObjFile(it.name, it.path) } ?: emptyList()
    }

    private suspend fun getFileNameFromUri(uri: Uri) = withContext(Dispatchers.IO) {
        val fileName = context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(0)
        }
        return@withContext fileName ?: System.currentTimeMillis().toString()
    }

    data class ObjFile(
        val name: String,
        val path: String
    )

    companion object {
        private const val OBJECTS_FOLDER = "objects"
    }
}
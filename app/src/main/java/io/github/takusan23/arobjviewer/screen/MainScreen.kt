package io.github.takusan23.arobjviewer.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.takusan23.arobjviewer.ArActivity
import io.github.takusan23.arobjviewer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val objFileList = mainScreenViewModel.objFileListFlow.collectAsState()

    val filePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments(), onResult = {
        it ?: return@rememberLauncherForActivityResult
        mainScreenViewModel.addAllObjFile(it)
    })

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                filePicker.launch(arrayOf("*/*"))
            }) { Icon(painter = painterResource(id = R.drawable.outline_note_add_24), contentDescription = null) }
        }
    ) { values ->
        Box(modifier = Modifier.padding(values)) {
            LazyColumn {
                items(objFileList.value) { objItem ->
                    Surface(
                        onClick = {
                            ArActivity.createIntent(context, objItem.path).also { intent ->
                                context.startActivity(intent)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .weight(1f),
                                fontSize = 20.sp,
                                text = objItem.name
                            )
                            IconButton(
                                modifier = Modifier
                                    .padding(5.dp),
                                onClick = {
                                    mainScreenViewModel.deleteObjFile(objItem)
                                }
                            ) { Icon(painter = painterResource(id = R.drawable.outline_delete_24), contentDescription = null) }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
package io.github.takusan23.arobjviewer.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.takusan23.arobjviewer.R

@Composable
fun ArActivityController(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onRotateLock: () -> Unit,
    onRotateX: () -> Unit,
    onRotateY: () -> Unit,
    onRotateZ: () -> Unit
) {
    Row(modifier = modifier) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(25)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) { Icon(painter = painterResource(id = R.drawable.outline_delete_24), contentDescription = null) }
                IconButton(onClick = onRotateLock) { Icon(painter = painterResource(id = R.drawable.outline_lock_reset_24), contentDescription = null) }
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(25)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onRotateX) { Icon(painter = painterResource(id = R.drawable.outline_rotate_90_degrees_ccw_24), contentDescription = null) }
                    Text(text = "X")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onRotateY) { Icon(painter = painterResource(id = R.drawable.outline_rotate_90_degrees_ccw_24), contentDescription = null) }
                    Text(text = "Y")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onRotateZ) { Icon(painter = painterResource(id = R.drawable.outline_rotate_90_degrees_ccw_24), contentDescription = null) }
                    Text(text = "Z")
                }
            }
        }
    }

}
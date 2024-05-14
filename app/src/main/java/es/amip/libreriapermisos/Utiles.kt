package es.amip.libreriapermisos

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PermissionDialog(
    showDialog:Boolean,
    setShow: (Boolean)->Unit,
    action:()->Unit,
    title: MutableState<String>,
    body: MutableState<String>,
    buttonText: MutableState<String>
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                setShow(false)
            },
            title = {
                Text(text = title.value)
            },
            text = {
                Text(text = body.value)
            },
            confirmButton = {
                Button(
                    onClick = {
                        action()
                    }
                ) {
                    Text(buttonText.value)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        setShow(false)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun showSnackbar(message:String, scope: CoroutineScope, snackbarHostState: SnackbarHostState){
    scope.launch {
        snackbarHostState.showSnackbar(message)
    }
}
package es.amip.libreriapermisos

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import es.amip.libreriapermisos.Permisos.checkPermissions
import es.amip.libreriapermisos.Permisos.isActive
import es.amip.libreriapermisos.Permisos.requestPermission
import es.amip.libreriapermisos.ui.theme.LibreriaPermisosTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibreriaPermisosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val listaPermisos = remember {
                        mutableStateListOf("")
                    }
                    val context = LocalContext.current
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState)
                        },
                        content = {
                            PanelPermissions(scope, snackbarHostState,listaPermisos)
                            LaunchedEffect(key1 = listaPermisos) {
                                checkPermissions(context, listaPermisos)
                            }
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PanelPermissions(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    listaPermisos: SnapshotStateList<String>,

    ) {
    val context = LocalContext.current

    Box() {
        Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(10.dp))
            for(p in permissions) {
                //PERMISOS
                PermissionComposable(
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    p,
                    listaPermisos
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            Spacer(modifier = Modifier.height(40.dp))
            //LISTA DE PERMISOS
            checkPermissions(context, listaPermisos)
            PermissionList(listaPermisos)
        }

    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionComposable(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    permiso: String,
    listaPermisos: MutableList<String>
) {
    val permissionState = rememberPermissionState(permiso)
    val (showDialog, setShowDialog) = remember { mutableStateOf(false) }
    val title = remember { mutableStateOf("TITULO") }
    val body = remember { mutableStateOf("BODY") }
    val buttonText = remember { mutableStateOf("buttonText") }
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //Acción permiso concedido
            showSnackbar("PERMISO ${permiso} CONCEDIDO", scope, snackbarHostState)

        } else {
            //acción permiso denegado
            showSnackbar("PERMISO ${permiso} DENEGADO", scope, snackbarHostState)
        }
        checkPermissions(context, listaPermisos)
    }
    Button(
        onClick = {
            scope.launch() {
                //EXPLICAR POR QUÉ ES NECESARIO SI NO SE CONCEDE
                if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
                    title.value = "PERMISO NECESARIO"
                    body.value = "NO HAS CONCEDIDO PERMISO: ${permiso}"
                    buttonText.value = "REINTENTAR"
                    setShowDialog(true)
                } else {
                    requestPermissionLauncher.launch(permiso)
                }
            }
        },
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = isActive(listaPermisos,permiso),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text = permiso)
    }
    PermissionDialog(
        showDialog,
        setShowDialog,
        { requestPermission(requestPermissionLauncher, permiso); setShowDialog(false) },
        title,
        body,
        buttonText
    )
}

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)

fun PermissionList(listaPermisos: MutableList<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Permisos Concedidos",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (listaPermisos.isNotEmpty()) {
            listaPermisos.forEach { permission ->
                Text(
                    text = permission,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        } else {
            Text(
                text = "No hay permisos concedidos",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}








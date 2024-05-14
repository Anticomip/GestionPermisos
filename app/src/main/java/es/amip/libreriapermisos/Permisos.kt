package es.amip.libreriapermisos

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

object Permisos {

    fun isActive(listaPermisos: List<String>, permiso: String): Color {
        return if(listaPermisos.contains(permiso)) Color.Green else Color.Transparent
    }
    fun requestPermission(
        requestPermissionLauncher: ActivityResultLauncher<String>,
        permission: String
    ) {
        requestPermissionLauncher.launch(permission)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermissions(context: Context, permisos:MutableList<String>){
        permisos.clear()
        for(e in permissions) {
            if(checkOnePermission(context, e))permisos.add(e)
        }
    }
    fun checkOnePermission(context: Context, e:String): Boolean {
        return ContextCompat.checkSelfPermission(context, e) == PackageManager.PERMISSION_GRANTED
    }
}
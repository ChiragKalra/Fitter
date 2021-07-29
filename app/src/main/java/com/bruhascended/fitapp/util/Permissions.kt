package com.bruhascended.fitapp.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.bruhascended.fitapp.ui.main.permissions
import com.bruhascended.fitapp.ui.main.runningQOrLater
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.FitnessOptions

lateinit var requestAndroidPermissionLauncher: ActivityResultLauncher<Array<String>>
lateinit var requestOauthPermissionsLauncher: ActivityResultLauncher<Intent>

fun getAndroidRunTimePermissionGivenMap(context: Context, list: List<permissions>): Map<String,Boolean> {
    val permissionMap = mutableMapOf<String,Boolean>()
    if(runningQOrLater){
        for(permission in list){
            permissionMap.put(permission.str, checkSelf(context,permission.str))
        }
    }else{
        for(permission in list){
            if(permission != permissions.ACTIVITY_RECOGNITION && permission != permissions.BACKGROUND_LOCATION){
                permissionMap.put(permission.str, checkSelf(context,permission.str))
            }
        }
    }
    return permissionMap
}

private fun checkSelf(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun isOauthPermissionsApproved(context: Context, fitnessOptions: FitnessOptions): Boolean {
    return GoogleSignIn.hasPermissions(getGoogleAccount(context, fitnessOptions), fitnessOptions)
}

fun getGoogleAccount(context: Context, fitnessOptions: FitnessOptions): GoogleSignInAccount {
    return GoogleSignIn.getAccountForExtension(context, fitnessOptions)
}

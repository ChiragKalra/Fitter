package com.bruhascended.fitapp.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.FitnessOptions

lateinit var requestAndroidPermissionLauncher: ActivityResultLauncher<Array<String>>
lateinit var requestOauthPermissionsLauncher: ActivityResultLauncher<Intent>

/* in case Api <=28 ACTIVITY_RECOGNITION permission is automatically granted, so always true*/
fun isActivityRecognitionPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun isAndroidRunTimePermissionGiven(context: Context, permission: String): Boolean {
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

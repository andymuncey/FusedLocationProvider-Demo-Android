package com.tinyappco.fusedlocationdemo

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NeedsLocationPermission(modifier: Modifier = Modifier, onPermissionResult: (Boolean) -> Unit ){
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val isGranted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        onPermissionResult(isGranted)
    }

    Spacer(modifier = modifier)
    Text(
        text = "Location permission is required to display location attributes.",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        },
    ) {
        Text(text = "Grant Location Permission")
    }
    Spacer(modifier = modifier)
}
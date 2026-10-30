package com.tinyappco.fusedlocationdemo

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LocationScreen(
    viewModel: LocationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val isGranted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        viewModel.onPermissionResult(isGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!uiState.hasLocationPermission) {
            Spacer(modifier = Modifier.weight(1f))
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
            Spacer(modifier = Modifier.weight(1f))
        } else {
            // Section 1: Start/Stop updates
            Text(
                text = "Start/Stop updates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    if (uiState.isUpdatingLocation) {
                        viewModel.stop()
                    } else {
                        viewModel.start()
                    }
                },
            ) {
                Text(if (uiState.isUpdatingLocation) "Stop" else "Start")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Section 2: CLLocation attributes
            Text(
                text = "CLLocation attributes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LabelledValue(label = "Latitude", value = uiState.latitude)
            LabelledValue(label = "Longitude", value = uiState.longitude)
            LabelledValue(label = "Accuracy", value = uiState.accuracy)
            LabelledValue(label = "Bearing", value = uiState.bearing)
            LabelledValue(label = "Speed", value = uiState.speed)
            LabelledValue(label = "Altitude", value = uiState.altitude)

            Spacer(modifier = Modifier.weight(1f))

            // Section 4: Address
            Text(
                text = "Address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = { viewModel.updateAddress() }) {
                Text("Lookup address")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.address,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${uiState.distanceFromChester} from Chester",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/**
 * Simple Composable for presenting a value with a label to the left of it
 */
@Composable
fun LabelledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

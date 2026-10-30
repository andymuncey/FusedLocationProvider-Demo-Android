package com.tinyappco.fusedlocationdemo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    //observe changes in the uiState StateFlow
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!uiState.hasLocationPermission) {
            NeedsLocationPermission { viewModel.onPermissionResult(it) }
        } else {
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
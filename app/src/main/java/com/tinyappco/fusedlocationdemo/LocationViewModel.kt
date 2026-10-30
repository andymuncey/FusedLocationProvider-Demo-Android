package com.tinyappco.fusedlocationdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

data class LocationUiState(
    val hasLocationPermission: Boolean = false,
    val isUpdatingLocation: Boolean = false,
    val latitude: String = "N/A",
    val longitude: String = "N/A",
    val accuracy: String = "N/A",
    val bearing: String = "N/A",
    val speed: String = "N/A",
    val altitude: String = "N/A",
    val address: String = "",
    val distanceFromChester: String = "-",
    val errorMessage: String? = null,
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {



    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null

    private var location: Location? = null

    init {
        checkPermission()
    }

    fun checkPermission() {
        val hasPermission = isLocationPermissionGranted()
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = isGranted,
                errorMessage = if (!isGranted) "Location permission denied." else null,
            )
        }
        if (isGranted) {
            start()
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (!isLocationPermissionGranted()) {
            _uiState.update { it.copy(hasLocationPermission = false) }
            return
        }

        if (_uiState.value.isUpdatingLocation) return



        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation ?: return
                updateLocationData(lastLocation)
                location = lastLocation
            }
        }
        locationCallback = callback

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L) //don't update more often than this
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper(),
            )
            _uiState.update { it.copy(isUpdatingLocation = true, errorMessage = null) }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(errorMessage = "Permission missing: ${e.localizedMessage}") }
        }
    }

    fun stop() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        location = null
        _uiState.update { it.copy(
            isUpdatingLocation = false,
            latitude = "N/A",
            longitude = "N/A",
            accuracy = "N/A",
            bearing = "N/A",
            speed = "N/A",
            altitude = "N/A"
        ) }
    }

    fun updateAddress() {
        val loc = location ?: run {
            _uiState.update { it.copy(address = "No location available yet.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                //Android 33 and later
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()?.getAddressLine(0) ?: "Address not found"
                        _uiState.update { it.copy(address = address) }
                    }
                } else {
                    //Android 32 and earlier
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
                    _uiState.update { it.copy(address = address) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(address = "Error looking up address: ${e.localizedMessage}") }
            }
        }
    }

    private fun distanceFromChester(location: Location) : String {
        val chesterLat = 53.2
        val chesterLon = -2.9
        val results = FloatArray(1)
        Location.distanceBetween(location.latitude, location.longitude, chesterLat, chesterLon, results)
        return format(results[0], 0, "m")
    }

    private fun altitude(location: Location) : String {
        if (!location.hasAltitude()) return "N/A"
        if (!location.hasVerticalAccuracy()) return "N/A"
        return String.format(Locale.UK,"%.0fm ± %.0fm", location.altitude, abs(location.accuracy))
    }

    private fun updateLocationData(location: Location) {
        //this.location = location
        val lat = location.latitude
        val lon = location.longitude

        _uiState.update {
            it.copy(
                latitude = format(lat, 4),
                longitude = format(lon, 4),
                accuracy = if (location.hasAccuracy()) format(location.accuracy, 0, "m") else "N/A",
                bearing = if (location.hasBearing()) format(location.bearing, 0 , "°") else "N/A",
                speed = if (location.hasSpeed()) format(location.speed*2.237, 0, " mph") else "N/A",
                altitude = altitude(location),
                distanceFromChester = distanceFromChester(location),
            )
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        val context = getApplication<Application>()
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    //uses Number because lat/long are double, but accuracy is float
    private fun format(value: Number?, places: Int, unit: String = "") : String {
        if (value != null) {
            val formatString = "%.${places}f$unit)"
            return String.format(formatString, value, unit)
        }
        return "N/A"
    }

    override fun onCleared() {
        stop()
    }
}

package com.udacity.project4.locationreminders.savereminder.selectreminderlocation



import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var marker: Marker
    private lateinit var pointOfInterest: PointOfInterest
    lateinit var fusedLocationClient: FusedLocationProviderClient
    val REQUEST_TURN_DEVICE_LOCATION_ON = 101


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.let {
            mapFragment.getMapAsync(this)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }
        return binding.root
    }


    private fun onLocationSelected() {
        if (::pointOfInterest.isInitialized) {
            _viewModel.setPOILocation(pointOfInterest)
            findNavController().popBackStack()
        } else {
            Snackbar.make(
                this.view!!,
                "No Location Selected",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(gMap: GoogleMap) {
        map = gMap
        setPointClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()
    }

    private fun setPointClick(map: GoogleMap) {
        map.setOnMapClickListener {
            if (::marker.isInitialized)
                marker.remove()
            val geocoder = Geocoder(this.context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (addresses.isNullOrEmpty())
                return@setOnMapClickListener
            val address = addresses[0].getAddressLine(0)
            marker = map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(address)
            )
            pointOfInterest = PointOfInterest(it, null, address)
            marker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("setMapStyle", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("setMapStyle", "Can't find style. Error: ", e)
        }
    }

    fun enableMyLocation() {
        if (osVersionAboveQ) {
            requestPermission(
                fineAndBackgroundLocationPermissions,
                fineAndBackgroundLocationRequestCode,
                fineAndBackgroundLocationPermissionsMessages,
                ::getUserLocation,
                ::showSnackBarWhenDenied
            )
        } else {
            requestPermission(
                fineLocationPermission,
                fineLocationRequestCode,
                fineLocationPermissionMessages,
                ::getUserLocation,
                ::showSnackBarWhenDenied
            )
        }
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            if (::marker.isInitialized)
                marker.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker.showInfoWindow()
            pointOfInterest = poi
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                checkGPSIsOn(false)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == fineAndBackgroundLocationRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else showSnackBarWhenDenied()
        } else if (requestCode == fineLocationRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else showSnackBarWhenDenied()
        }
    }

    fun showSnackBarWhenDenied() {
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            R.string.location_required_error,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        map.isMyLocationEnabled = true
        checkGPSIsOn(false)
    }

    @SuppressLint("MissingPermission")
    fun checkGPSIsOn(enableBox: Boolean) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && enableBox) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                    /*exception.startResolutionForResult(
                        this.activity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )*/
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    this.view!!,
                    R.string.location_required_error,
                    Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    checkGPSIsOn(true)
                }.show()
            }

        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    }
                }
            }
        }
    }
}




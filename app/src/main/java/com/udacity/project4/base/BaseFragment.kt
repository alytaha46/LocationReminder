package com.udacity.project4.base

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel
    var dialog: AlertDialog? = null
    val fineLocationRequestCode = 1000
    val fineAndBackgroundLocationRequestCode = 1001
    val osVersionAboveQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    val fineAndBackgroundLocationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    val fineAndBackgroundLocationPermissionsMessages = arrayOf(
        "current location and Background Location",
        "Background location"
    )
    val fineLocationPermission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val fineLocationPermissionMessages = arrayOf(
        "current location and Background Location"
    )
    val REQUEST_TURN_DEVICE_LOCATION_ON = 101


    fun showDialog(
        title: String? = null,
        message: String? = null,
        posActionName: String? = null,
        posAction: DialogInterface.OnClickListener? = null,
        negActionName: String? = null,
        negAction: DialogInterface.OnClickListener? = null,
        cancelable: Boolean = true
    ) {
        dialog = AlertDialog.Builder(
            this.context
        )
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(posActionName, posAction)
            .setNegativeButton(negActionName, negAction)
            .setCancelable(cancelable)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun hideDialog() {
        dialog?.dismiss()
    }

    fun isPermissionGranted(permissions: Array<String>): Boolean {
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    it
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    /*
        fun requestLocationPermission(doAction:()->Unit) {
            if (osVersionAboveQ) {
                if (!isLocationGranted(fineAndBackgroundLocationPermissions)) {
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        showDialog(
                            "Important Permission",
                            "Please Accept this Permission to access current location and Background Location",
                            "OK",
                            DialogInterface.OnClickListener { dialog, which ->
                                hideDialog()
                                requestPermissions(fineAndBackgroundLocationPermissions, fineAndBackgroundLocationRequestCode)
                            },
                            "Cancel",
                            DialogInterface.OnClickListener { dialog, which ->
                                hideDialog()
                            }
                        )
                    } else if (shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    ) {
                        showDialog(
                            "Important Permission",
                            "Please Accept this Permission to access Background location",
                            "OK",
                            DialogInterface.OnClickListener { dialog, which ->
                                hideDialog()
                                requestPermissions(fineAndBackgroundLocationPermissions, fineAndBackgroundLocationRequestCode)
                            },
                            "Cancel",
                            DialogInterface.OnClickListener { dialog, which ->
                                hideDialog()
                            }
                        )
                    } else {
                        requestPermissions(fineAndBackgroundLocationPermissions, fineAndBackgroundLocationRequestCode)
                    }
                } else {
                    doAction()
                }

            } else {
                if (!isLocationGranted(fineLocationPermission)) {
                    if (shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        showDialog(
                            "Important Permission",
                            "Please Accept this Permission to your location",
                            "OK",
                            DialogInterface.OnClickListener { dialog, which ->
                                hideDialog()
                                requestPermissions(fineLocationPermission, fineLocationRequestCode)
                            },
                            "Cancel",
                            DialogInterface.OnClickListener { dialog, which ->
                                hideDialog()
                            }
                        )
                    } else {
                        requestPermissions(fineLocationPermission, fineLocationRequestCode)
                    }
                } else {
                    doAction()
                }
            }

        }*/
    fun requestPermission(
        permissions: Array<String>,
        requestCode: Int,
        messages: Array<String>,
        doAction: () -> Unit,
        showSnackBarWhenDenied:() -> Unit
    ) {
        if (!isPermissionGranted(permissions)) {
            var shouldShowPermissionRequest = true
            permissions.forEachIndexed { index, it ->
                if (shouldShowRequestPermissionRationale(it)
                ) {
                    showDialog(
                        "Important Permission",
                        "Please Accept this Permission to access ${messages[index]}",
                        "OK",
                        DialogInterface.OnClickListener { dialog, which ->
                            hideDialog()
                            requestPermissions(
                                permissions,
                                requestCode
                            )
                        },
                        "Cancel",
                        DialogInterface.OnClickListener { dialog, which ->
                            hideDialog()
                            showSnackBarWhenDenied()
                        }
                    )
                    shouldShowPermissionRequest = false
                    return
                }
            }
            if (shouldShowPermissionRequest) {
                requestPermissions(
                    permissions,
                    requestCode
                )
            }
        } else {
            doAction()
        }

    }

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, Observer {
            Snackbar.make(this.view!!, it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, Observer {
            Snackbar.make(this.view!!, getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.navigationCommand.observe(this, Observer { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })
    }
}
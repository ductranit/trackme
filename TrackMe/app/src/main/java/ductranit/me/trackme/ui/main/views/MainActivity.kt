/*
 * Copyright (C) 2018 ductranit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ductranit.me.trackme.ui.main.views

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityMainBinding
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.main.viewmodels.MainViewModel
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.ui.tracking.views.TrackingActivity
import ductranit.me.trackme.ui.widgets.VerticalSpaceItemDecoration
import ductranit.me.trackme.utils.AppExecutors
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.LocationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var adapter: SessionAdapter
    private var sessionId: Long = INVALID_ID
    private var permissionListener: PermissionListener? = null

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var sessionDataManager: SessionDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // is running
        if (sessionDataManager.isAddNew && sessionDataManager.sessionId != INVALID_ID) {
            goToTracking(sessionDataManager.sessionId)
        }

        layoutAppBar.toolbar?.apply {
            setSupportActionBar(layoutAppBar.toolbar)
            supportActionBar?.title = getString(R.string.app_name)
        }

        layoutMain.fabRecord.setOnClickListener {
            sessionDataManager.sessionId = INVALID_ID
            sessionDataManager.isAddNew = true
            sessionDataManager.state = State.PLAYING
            goToTracking(INVALID_ID)
        }

        adapter = SessionAdapter(
                appExecutors = appExecutors
        ) { session ->
            sessionDataManager.sessionId = session.id
            sessionDataManager.isAddNew = false
            sessionDataManager.state = State.DEFAULT
            goToTracking(session.id)
        }

        rvSession.adapter = adapter
        val itemSpace = resources.getDimension(R.dimen.item_pad).toInt()
        val lastSpace = resources.getDimension(R.dimen.last_item_space).toInt()
        rvSession.addItemDecoration(VerticalSpaceItemDecoration(itemSpace, lastSpace))

        lifecycle.addObserver(adapter)

        rvSession.setRecyclerListener { holder ->
            if (holder is SessionAdapter.SessionViewHolder) {
                holder.googleMap?.clear()
                holder.googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
            }
        }

        viewModel.isLoaded.observe(this, Observer { loaded ->
            if (loaded == true) {
                viewModel.getSessions().observe(this, Observer { sessions ->
                    progressLoading.visibility = View.GONE
                    adapter.submitList(sessions)
                    binding.executePendingBindings()
                })
            } else {
                progressLoading.visibility = View.VISIBLE
            }
        })


        rvSession.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fabRecord.isShown) {
                    fabRecord.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fabRecord.show()
                }
            }
        })

    }

    override fun layoutId(): Int {
        return R.layout.activity_main
    }

    override fun onDestroy() {
        permissionListener = null
        super.onDestroy()
    }

    private fun onPermissionGranted() {
        if (LocationUtils.isGPSEnable(this)) {
            val intent = Intent(getActivity(), TrackingActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.msg_gps_disable, Toast.LENGTH_LONG).show()
        }
    }

    private fun goToAppSetting() {
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", getActivity().packageName, null)
            intent.data = uri
            getActivity().startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Timber.e(ex)
        }
    }

    private fun goToTracking(sessionId: Long) {
        this.sessionId = sessionId
        permissionListener = PermissionListener(this)

        Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(permissionListener)
                .check()

    }

    /**
     * Have to use WeakReference because of memory leak from Dexter lib
     * https://github.com/Karumi/Dexter/issues/197
     */
    private class PermissionListener(activity: MainActivity) : MultiplePermissionsListener {
        private val weakReference: WeakReference<MainActivity> = WeakReference(activity)

        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            if (report != null && weakReference.get() != null) {
                if (report.areAllPermissionsGranted()) {
                    weakReference.get()?.onPermissionGranted()
                } else {
                    for (item in report.deniedPermissionResponses) {
                        if (item.isPermanentlyDenied) {
                            Toast.makeText(weakReference.get()?.getActivity(), R.string.msg_location_permission_disable,
                                    Toast.LENGTH_LONG).show()
                            weakReference.get()?.goToAppSetting()
                            break
                        }
                    }
                }
            }
        }

        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
            if (weakReference.get() != null) {
                Toast.makeText(weakReference.get()?.getActivity(), R.string.msg_location_permission, Toast.LENGTH_LONG).show()
                token?.continuePermissionRequest()
            }
        }
    }
}

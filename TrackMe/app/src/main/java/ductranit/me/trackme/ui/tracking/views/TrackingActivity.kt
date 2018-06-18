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

package ductranit.me.trackme.ui.tracking.views

import android.arch.lifecycle.Observer
import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityTrackingBinding
import ductranit.me.trackme.models.HistoryLocation
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.services.LocationService
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.ui.tracking.viewmodels.TrackingViewModel
import ductranit.me.trackme.utils.Constants.Companion.ACTION_BROADCAST
import ductranit.me.trackme.utils.Constants.Companion.EXTRA_LOCATION
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_LATITUDE
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_LONGITUDE
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_SPEED
import ductranit.me.trackme.utils.Constants.Companion.MAP_ZOOM_LEVEL
import ductranit.me.trackme.utils.Constants.Companion.MARKER_CIRCLE_RADIUS
import ductranit.me.trackme.utils.Constants.Companion.TIMER_TICK
import ductranit.me.trackme.utils.converters.setDate
import ductranit.me.trackme.utils.converters.setDateRange
import ductranit.me.trackme.utils.converters.setSpeed
import ductranit.me.trackme.utils.getDouble
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.content_tracking.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TrackingActivity : BaseActivity<ActivityTrackingBinding, TrackingViewModel>(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var polyLines: Polyline? = null
    private var options: PolylineOptions? = null
    private var firstPositionMarker: Marker? = null
    private var currentPositionCircle: Circle? = null
    private var locationReceiver: LocationReceiver? = null
    private var handler: Handler = Handler()
    private var timerRunnable: TimerRunnable = TimerRunnable()
    private var startTime: Date? = null

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var sessionDataManager: SessionDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.sessionId = sessionDataManager.sessionId
        viewModel.isRecording = sessionDataManager.isAddNew
        viewModel.state.observe(this, Observer { updateState() })
        viewModel.state.value = sessionDataManager.state

        layoutAppBar.toolbar?.apply {
            setSupportActionBar(layoutAppBar.toolbar)
            if (!viewModel.isRecording) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }

            supportActionBar?.title = getString(R.string.record)
        }

        viewModel.getSession().observe(this, Observer { session ->
            if (viewModel.isRecording) {
                startTime = session?.startTime
                handler.post(timerRunnable)
            } else {
                tvTime.setDateRange(session?.startTime, session?.endTime)
            }

            session?.locations?.let {
                updateLocations(it)
            }

            binding.layoutTracking?.session = session
            binding.executePendingBindings()
        })

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        btnPause.setOnClickListener {
            viewModel.state.value = State.PAUSE
            sessionDataManager.state = State.PAUSE
        }

        btnStop.setOnClickListener {
            viewModel.state.value = State.STOP
            sessionDataManager.state = State.STOP
        }

        btnReplay.setOnClickListener {
            viewModel.state.value = State.PLAYING
            sessionDataManager.state = State.PLAYING
        }
    }

    override fun onBackPressed() {
        // can go back if user view history session
        if (!viewModel.isRecording) {
            super.onBackPressed()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        MapsInitializer.initialize(this)
        googleMap = map
        googleMap?.uiSettings?.isCompassEnabled = true
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isScrollGesturesEnabled = true
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.setAllGesturesEnabled(true)

        options = PolylineOptions()
        options?.width(5f)
        options?.visible(true)
        options?.color(ContextCompat.getColor(this, R.color.colorMapPath))

        polyLines = googleMap?.addPolyline(options)
        if (preferences.getDouble(KEY_LOCATION_LATITUDE, 0.0) != 0.0
                && preferences.getDouble(KEY_LOCATION_LONGITUDE, 0.0) != 0.0) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(preferences.getDouble(KEY_LOCATION_LATITUDE,
                    0.0), preferences.getDouble(KEY_LOCATION_LONGITUDE, 0.0)),
                    MAP_ZOOM_LEVEL))
        }

        updateSpeed(preferences.getFloat(KEY_LOCATION_SPEED, 0.0f))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (viewModel.isRecording) {
            handler.post(timerRunnable)
        }
    }

    override fun onPause() {
        mapView.onPause()
        handler.removeCallbacks(timerRunnable)
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        unregisterReceiver()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        registerReceiver()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        handler.removeCallbacks(timerRunnable)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun layoutId(): Int {
        return R.layout.activity_tracking
    }

    private fun updateState() {
        when (viewModel.state.value) {
            State.PLAYING -> {
                btnPause.visibility = View.VISIBLE
                btnReplay.visibility = View.GONE
                btnStop.visibility = View.GONE
                LocationService.start(this)

                if (viewModel.isRecording) {
                    handler.post(timerRunnable)
                }
            }

            State.PAUSE -> {
                btnPause.visibility = View.GONE
                btnReplay.visibility = View.VISIBLE
                btnStop.visibility = View.VISIBLE
                LocationService.stop(this)
                handler.removeCallbacks(timerRunnable)
            }

            State.STOP -> {
                viewModel.stop()
                handler.removeCallbacks(timerRunnable)
                LocationService.stop(this)
                sessionDataManager.clear()
                finish()
            }

            State.DEFAULT -> {
                layoutBottom.visibility = View.GONE
            }
        }
    }

    private fun updateLocations(locations: MutableList<HistoryLocation>) {
        if (googleMap == null || options == null) {
            return
        }

        polyLines?.remove()


        if (!locations.isEmpty()) {
            for (location in locations) {
                Timber.d("location $location")
                options?.add(LatLng(location.lat, location.lng))
            }

            // draw first position as marker
            firstPositionMarker?.remove()
            firstPositionMarker = googleMap?.addMarker(MarkerOptions()
                    .position(LatLng(locations[0].lat, locations[0].lng)))

            // draw current location as circle
            if (locations.size >= 2) {
                currentPositionCircle?.remove()
                val lastLocation = locations[locations.size - 1]
                currentPositionCircle = googleMap?.addCircle(CircleOptions()
                        .center(LatLng(lastLocation.lat, lastLocation.lng))
                        .radius(MARKER_CIRCLE_RADIUS)
                        .strokeColor(ContextCompat.getColor(this, R.color.colorCircleRadius))
                        .fillColor(ContextCompat.getColor(this, R.color.colorMapPath)))
            }

            val lastLocation = locations[locations.size - 1]
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.lat, lastLocation.lng),
                    MAP_ZOOM_LEVEL))
        }

        polyLines = googleMap?.addPolyline(options)
    }

    fun updateSpeed(speed: Float?) {
        tvSpeed.setSpeed(speed)
    }

    private fun registerReceiver() {
        if (locationReceiver == null) {
            locationReceiver = LocationReceiver()
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_BROADCAST)
        registerReceiver(locationReceiver, intentFilter)
    }

    private fun unregisterReceiver() {
        unregisterReceiver(locationReceiver)
    }

    inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location = intent?.getSerializableExtra(EXTRA_LOCATION) as Location?
            updateSpeed(location?.speed)
        }
    }

    inner class TimerRunnable : Runnable {
        override fun run() {
            tvTime.setDate(startTime)
            handler.postDelayed(this, TIMER_TICK)
        }
    }
}
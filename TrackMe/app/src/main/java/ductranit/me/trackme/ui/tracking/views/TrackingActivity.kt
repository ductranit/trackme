package ductranit.me.trackme.ui.tracking.views

import android.arch.lifecycle.Observer
import android.content.*
import android.location.Location
import android.os.Bundle
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
import ductranit.me.trackme.services.LocationService
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.ui.tracking.viewmodels.TrackingViewModel
import ductranit.me.trackme.utils.Constants
import ductranit.me.trackme.utils.Constants.Companion.ACTION_BROADCAST
import ductranit.me.trackme.utils.Constants.Companion.EXTRA_LOCATION
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_LATITUDE
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_LONGITUDE
import ductranit.me.trackme.utils.Constants.Companion.KEY_LOCATION_SPEED
import ductranit.me.trackme.utils.Constants.Companion.MARKER_CIRCLE_RADIUS
import ductranit.me.trackme.utils.Constants.Companion.SESSION_ID
import ductranit.me.trackme.utils.converters.setSpeed
import ductranit.me.trackme.utils.getDouble
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.content_tracking.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import timber.log.Timber
import javax.inject.Inject

class TrackingActivity : BaseActivity<ActivityTrackingBinding, TrackingViewModel>(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var polyLines: Polyline? = null
    private var options: PolylineOptions? = null
    private var firstPositionMarker: Marker? = null
    private var currentPositionCircle: Circle? = null
    private var locationReceiver: LocationReceiver? = null
    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutAppBar.toolbar?.apply {
            setSupportActionBar(layoutAppBar.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.record)
        }

        viewModel.sessionId = intent.getLongExtra(SESSION_ID, INVALID_ID)
        viewModel.state.observe(this, Observer { updateState() })
        if (viewModel.sessionId != INVALID_ID) {
            viewModel.state.value = State.STOP
        } else {
            viewModel.state.value = State.PLAYING
            LocationService.start(this)
        }

        viewModel.getSession().observe(this, Observer { session ->
            session?.locations?.let {
                updateLocations(it)
            }

            binding.layoutTracking?.session = session
            binding.executePendingBindings()
        })

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
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
                    Constants.MAP_ZOOM_LEVEL))
        }

        updateSpeed(preferences.getFloat(KEY_LOCATION_SPEED, 0.0f))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        unregisterReceiver()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        registerReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
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
            }

            State.PAUSE -> {
                btnPause.visibility = View.GONE
                btnReplay.visibility = View.VISIBLE
                btnStop.visibility = View.VISIBLE
            }

            State.STOP -> {
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
                    Constants.MAP_ZOOM_LEVEL))
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
}
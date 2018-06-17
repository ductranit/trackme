package ductranit.me.trackme.ui.tracking.views

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityTrackingBinding
import ductranit.me.trackme.models.HistoryLocation
import ductranit.me.trackme.services.LocationService
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.tracking.State
import ductranit.me.trackme.ui.tracking.viewmodels.TrackingViewModel
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.MAP_ZOOM_LEVEL
import ductranit.me.trackme.utils.Constants.Companion.SESSION_ID
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.content_tracking.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import timber.log.Timber

class TrackingActivity : BaseActivity<ActivityTrackingBinding, TrackingViewModel>(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    private var polyLines: Polyline? = null
    private var options: PolylineOptions? = null
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
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(10.8269675, 106.7057734), MAP_ZOOM_LEVEL))
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
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
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
        for (location in locations) {
            Timber.d("location $location")
            options?.add(LatLng(location.lat, location.lng))
        }

        polyLines = googleMap?.addPolyline(options)
    }
}
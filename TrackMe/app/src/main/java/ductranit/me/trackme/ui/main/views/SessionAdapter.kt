package ductranit.me.trackme.ui.main.views

import android.arch.lifecycle.LifecycleObserver
import android.databinding.DataBindingUtil
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.SessionItemBinding
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.widgets.DataBoundListAdapter
import ductranit.me.trackme.ui.widgets.DataBoundViewHolder
import ductranit.me.trackme.utils.AppExecutors

class SessionAdapter(appExecutors: AppExecutors,
                     private val itemClickCallback: ((Session) -> Unit)?) :
        DataBoundListAdapter<Session, SessionItemBinding>(
                appExecutors = appExecutors,
                diffCallback = object : DiffUtil.ItemCallback<Session>() {
                    override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
                        return oldItem.distance == newItem.distance
                                && oldItem.averageSpeed == newItem.averageSpeed
                                && oldItem.startTime == newItem.startTime
                                && oldItem.endTime == newItem.endTime
                    }
                }
        ), LifecycleObserver {

    override fun createBinding(parent: ViewGroup): SessionItemBinding {
        val binding = DataBindingUtil.inflate<SessionItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.session_item,
                parent,
                false
        )

        binding.root.setOnClickListener {
            binding.session?.let {
                itemClickCallback?.invoke(it)
            }
        }

        binding.mapView.isClickable = false

        return binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<SessionItemBinding> {
        val binding = createBinding(parent)
        val viewHolder = SessionViewHolder(binding)
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(viewHolder)
        return viewHolder
    }

    override fun bind(holder: DataBoundViewHolder<SessionItemBinding>, item: Session) {
        val sessionViewHolder = holder as SessionViewHolder
        sessionViewHolder.binding.session = item
        sessionViewHolder.setMapLocation(item)
    }

    class SessionViewHolder constructor(binding: SessionItemBinding) :
            DataBoundViewHolder<SessionItemBinding>(binding), OnMapReadyCallback {
        var googleMap: GoogleMap? = null
        override fun onMapReady(map: GoogleMap?) {
            MapsInitializer.initialize(binding.root.context)
            googleMap = map
            googleMap?.uiSettings?.isCompassEnabled = false
            googleMap?.uiSettings?.isMapToolbarEnabled = false
            googleMap?.uiSettings?.isScrollGesturesEnabled = false
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            googleMap?.uiSettings?.setAllGesturesEnabled(false)

            setMapLocation(null)
        }

        fun setMapLocation(session: Session?) {
            if (googleMap == null) {
                return
            }

            val options = PolylineOptions()

            options.width(5f)
            options.visible(true)
            options.color(ContextCompat.getColor(binding.root.context, R.color.colorMapPath))
            val locations = ArrayList<LatLng>()
            locations.add(LatLng(10.8269675, 106.7057734))
            locations.add(LatLng(10.8269675, 106.7057734))
            locations.add(LatLng(10.8258295, 106.6920834))
            locations.add(LatLng(10.8299814, 106.6844766))

            for (item in locations) {
                options.add(LatLng(item.latitude,
                        item.longitude))
            }

            googleMap?.addPolyline(options)

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(10.8269675, 106.7057734), 14f))

            // Set the map type back to normal.
            googleMap?.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        }
    }
}
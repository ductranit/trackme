package ductranit.me.trackme.ui.main.views

import android.arch.lifecycle.LifecycleObserver
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ItemSessionBinding
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.widgets.DataBoundListAdapter
import ductranit.me.trackme.ui.widgets.DataBoundViewHolder
import ductranit.me.trackme.utils.AppExecutors

class SessionAdapter(private val context: Context, appExecutors: AppExecutors,
                     private val itemClickCallback: ((Session) -> Unit)?) :
        DataBoundListAdapter<Session, ItemSessionBinding>(
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

    override fun createBinding(parent: ViewGroup): ItemSessionBinding {
        val binding = DataBindingUtil.inflate<ItemSessionBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_session,
                parent,
                false
        )

        binding.root.setOnClickListener {
            binding.session?.let {
                itemClickCallback?.invoke(it)
            }
        }

        return binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<ItemSessionBinding> {
        val binding = createBinding(parent)
        val viewHolder = SessionViewHolder(binding)
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(viewHolder)
        return viewHolder;
    }

    override fun bind(binding: ItemSessionBinding, item: Session) {
        binding.session = item
    }

    class SessionViewHolder constructor(binding: ItemSessionBinding) :
            DataBoundViewHolder<ItemSessionBinding>(binding), OnMapReadyCallback {
        var googleMap: GoogleMap? = null
        override fun onMapReady(map: GoogleMap?) {
            MapsInitializer.initialize(binding.root.context)
            googleMap = map
            googleMap?.uiSettings?.isCompassEnabled = false
            googleMap?.uiSettings?.isMapToolbarEnabled = false
            googleMap?.uiSettings?.isScrollGesturesEnabled = false
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            googleMap?.uiSettings?.setAllGesturesEnabled(false)

            val camera = CameraUpdateFactory.newLatLngZoom(LatLng(52.3905217, 9.6996769), 14.0f)
            googleMap?.moveCamera(camera)
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL;
        }
    }
}
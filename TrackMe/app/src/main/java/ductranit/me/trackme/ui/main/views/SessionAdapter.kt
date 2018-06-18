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
import com.google.android.gms.maps.model.*
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.SessionItemBinding
import ductranit.me.trackme.models.Session
import ductranit.me.trackme.ui.widgets.DataBoundListAdapter
import ductranit.me.trackme.ui.widgets.DataBoundViewHolder
import ductranit.me.trackme.utils.AppExecutors
import ductranit.me.trackme.utils.Constants.Companion.MARKER_CIRCLE_RADIUS
import ductranit.me.trackme.utils.Constants.Companion.MAP_ZOOM_LEVEL

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
        private var options: PolylineOptions = PolylineOptions().visible(true).
                width(5f).color(ContextCompat.getColor(binding.root.context,
                R.color.colorMapPath))
        private var polyLines: Polyline? = null
        private var firstPositionMarker: Marker? = null
        private var currentPositionCircle: Circle? = null

        override fun onMapReady(map: GoogleMap?) {
            MapsInitializer.initialize(binding.root.context)
            googleMap = map
            googleMap?.uiSettings?.isCompassEnabled = false
            googleMap?.uiSettings?.isMapToolbarEnabled = false
            googleMap?.uiSettings?.isScrollGesturesEnabled = false
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            googleMap?.uiSettings?.setAllGesturesEnabled(false)

            setMapLocation(binding.session)
        }

        fun setMapLocation(session: Session?) {
            if (googleMap == null) {
                return
            }

            if (session != null && !session.locations.isEmpty()) {
                for (item in session.locations) {
                    options.add(LatLng(item.lat,
                            item.lng))
                }

                polyLines?.remove()
                polyLines = googleMap?.addPolyline(options)

                // draw first position as marker
                firstPositionMarker?.remove()
                firstPositionMarker = googleMap?.addMarker(MarkerOptions()
                        .position(LatLng(session.locations[0].lat, session.locations[0].lng)))

                // draw current location as circle
                if(session.locations.size >= 2) {
                    currentPositionCircle?.remove()
                    val lastLocation = session.locations[session.locations.size - 1]
                    currentPositionCircle = googleMap?.addCircle(CircleOptions()
                            .center(LatLng(lastLocation.lat, lastLocation.lng))
                            .radius(MARKER_CIRCLE_RADIUS)
                            .strokeColor(ContextCompat.getColor(binding.root.context, R.color.colorCircleRadius))
                            .fillColor(ContextCompat.getColor(binding.root.context, R.color.colorMapPath)))
                }

                val lastItem = session.locations[session.locations.size - 1]
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastItem.lat, lastItem.lng), MAP_ZOOM_LEVEL))
            }

            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }
}
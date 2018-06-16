package ductranit.me.trackme.ui.main.views

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityMainBinding
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.main.viewmodels.MainViewModel
import ductranit.me.trackme.ui.tracking.views.TrackingActivity
import ductranit.me.trackme.ui.widgets.VerticalSpaceItemDecoration
import ductranit.me.trackme.utils.AppExecutors
import ductranit.me.trackme.utils.Constants.Companion.INVALID_ID
import ductranit.me.trackme.utils.Constants.Companion.SESSION_ID
import ductranit.me.trackme.utils.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import javax.inject.Inject
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import timber.log.Timber


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private val PERMISSIONS_REQUEST = 1
    private lateinit var adapter: SessionAdapter
    private var sessionId: Long = INVALID_ID

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var permissionUtil: PermissionUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutAppBar.toolbar?.apply {
            setSupportActionBar(layoutAppBar.toolbar)
            supportActionBar?.title = getString(R.string.app_name)
        }

        layoutMain.fabRecord.setOnClickListener {
            goToTracking(INVALID_ID)
        }

        adapter = SessionAdapter(
                appExecutors = appExecutors
        ) { session ->
            goToTracking(session.id)
        }

        rvSession.adapter = adapter
        rvSession.addItemDecoration(VerticalSpaceItemDecoration(resources.getDimension(R.dimen.item_pad).toInt()))

        lifecycle.addObserver(adapter)

        rvSession.setRecyclerListener { holder ->
            if (holder is SessionAdapter.SessionViewHolder) {
                holder.googleMap?.clear()
                holder.googleMap?.mapType = GoogleMap.MAP_TYPE_NONE
            }
        }

        viewModel.getSessions().observe(this, Observer { sessions ->
            adapter.submitList(sessions)
            binding.executePendingBindings()
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

    private fun goToTracking(sessionId: Long) {
        this.sessionId = sessionId
        permissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION,
                object : PermissionUtil.PermissionAskListener {
                    override fun onNeedPermission() {
                        ActivityCompat.requestPermissions(getActivity(),
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                PERMISSIONS_REQUEST)
                    }

                    override fun onPermissionPreviouslyDenied() {
                        Toast.makeText(getActivity(), R.string.msg_location_permission, Toast.LENGTH_LONG).show()
                        onNeedPermission()
                    }

                    override fun onPermissionDisabled() {
                        Toast.makeText(getActivity(), R.string.msg_location_permission_disable, Toast.LENGTH_LONG).show()

                        try {
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        } catch (ex: ActivityNotFoundException) {
                            Timber.e(ex)
                        }
                    }

                    override fun onPermissionGranted() {
                        val intent = Intent(getActivity(), TrackingActivity::class.java)
                        intent.putExtra(SESSION_ID, sessionId)
                        startActivity(intent)
                    }

                })
    }
}

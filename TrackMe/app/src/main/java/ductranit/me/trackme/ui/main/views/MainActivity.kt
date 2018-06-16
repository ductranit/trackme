package ductranit.me.trackme.ui.main.views

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityMainBinding
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.main.viewmodels.MainViewModel
import ductranit.me.trackme.ui.record.views.TrackingActivity
import ductranit.me.trackme.utils.AppExecutors
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import javax.inject.Inject

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    lateinit var adapter: SessionAdapter

    @Inject
    lateinit var appExecutors: AppExecutors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutAppBar.toolbar?.apply {
            setSupportActionBar(layoutAppBar.toolbar)
            supportActionBar?.title = getString(R.string.app_name)
        }

        layoutMain.fabRecord.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
        }

        adapter = SessionAdapter(this,
                appExecutors = appExecutors
        ) { _ ->
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
        }

        rvSession.adapter = adapter
        lifecycle.addObserver(adapter)

        rvSession.setRecyclerListener { holder ->
            if (holder is SessionAdapter.SessionViewHolder) {
                holder.googleMap?.clear();
                holder.googleMap?.mapType = GoogleMap.MAP_TYPE_NONE;
            }
        }

        viewModel.getSessions().observe(this, Observer { sessions ->
            adapter.submitList(sessions)
            binding.executePendingBindings()
        })

    }

    override fun layoutId(): Int {
        return R.layout.activity_main
    }
}

package ductranit.me.trackme.ui.main.views

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityMainBinding
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.main.viewmodels.MainViewModel
import ductranit.me.trackme.ui.tracking.views.TrackingActivity
import ductranit.me.trackme.utils.AppExecutors
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*
import javax.inject.Inject
import android.support.v7.widget.RecyclerView
import ductranit.me.trackme.ui.widgets.VerticalSpaceItemDecoration

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var adapter: SessionAdapter

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

        adapter = SessionAdapter(
                appExecutors = appExecutors
        ) { _ ->
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
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
}

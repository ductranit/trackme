package ductranit.me.trackme.ui.record.views

import android.os.Bundle
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityTrackingBinding
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.record.viewmodels.TrackingViewModel
import kotlinx.android.synthetic.main.activity_tracking.*
import kotlinx.android.synthetic.main.partial_app_bar.view.*

class TrackingActivity: BaseActivity<ActivityTrackingBinding, TrackingViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutAppBar.toolbar?.apply {
            setSupportActionBar(layoutAppBar.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.app_name)
        }
    }

    override fun layoutId(): Int {
        return R.layout.activity_tracking
    }

}
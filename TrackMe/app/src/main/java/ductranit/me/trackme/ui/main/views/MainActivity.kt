package ductranit.me.trackme.ui.main.views

import android.os.Bundle
import ductranit.me.trackme.R
import ductranit.me.trackme.databinding.ActivityMainBinding
import ductranit.me.trackme.ui.base.views.BaseActivity
import ductranit.me.trackme.ui.main.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.title = getString(R.string.app_name)
        }
    }

    override fun layoutId(): Int {
        return R.layout.activity_main
    }
}

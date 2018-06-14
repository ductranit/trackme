package ductranit.me.trackme

import android.app.Activity
import android.app.Application
import com.squareup.leakcanary.LeakCanary
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import ductranit.me.trackme.di.AppInjector
import javax.inject.Inject

class GlobalApp : Application(), HasActivityInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        initLeakCanary()
    }

    override fun activityInjector() = dispatchingAndroidInjector

    private fun initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this)
    }
}
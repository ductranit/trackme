package ductranit.me.trackme

import android.app.Activity
import android.app.Application
import android.app.Service
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import ductranit.me.trackme.di.AppInjector
import javax.inject.Inject
import timber.log.Timber


class GlobalApp : Application(), HasActivityInjector, HasServiceInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        initLeakCanary()
        initLog()
    }

    override fun activityInjector() = dispatchingAndroidInjector
    override fun serviceInjector() = dispatchingServiceInjector

    private fun initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this)
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
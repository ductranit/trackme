package ductranit.me.trackme

import android.app.Activity
import android.app.Application
import android.app.Service
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.arch.lifecycle.ProcessLifecycleOwner
import com.squareup.leakcanary.LeakCanary
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import ductranit.me.trackme.di.AppInjector
import ductranit.me.trackme.models.SessionDataManager
import ductranit.me.trackme.services.LocationService
import ductranit.me.trackme.ui.tracking.State
import timber.log.Timber
import javax.inject.Inject

class GlobalApp : Application(), HasActivityInjector, HasServiceInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var sessionManager: SessionDataManager

    private var appLifecycleObserver: AppLifecycleObserver = AppLifecycleObserver()

    var wasInBackground: Boolean = false

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
        initLeakCanary()
        initLog()

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun activityInjector() = dispatchingAndroidInjector
    override fun serviceInjector() = dispatchingServiceInjector

    private fun initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        LeakCanary.install(this)
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    inner class AppLifecycleObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onEnterForeground() {
            wasInBackground = false
            Timber.d("onEnterForeground")
            // restart service and hide notification when app goes to foreground
            if (sessionManager.state == State.PLAYING) {
                LocationService.startAndClearNotification(this@GlobalApp)
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onEnterBackground() {
            wasInBackground = true
            Timber.d("onEnterBackground")
            // restart service and show notification when app goes to background
            if (sessionManager.state == State.PLAYING) {
                LocationService.start(this@GlobalApp)
            }
        }
    }
}
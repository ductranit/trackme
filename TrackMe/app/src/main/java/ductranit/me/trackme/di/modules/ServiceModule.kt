package ductranit.me.trackme.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ductranit.me.trackme.services.LocationService

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    internal abstract fun contributeLocationService(): LocationService
}
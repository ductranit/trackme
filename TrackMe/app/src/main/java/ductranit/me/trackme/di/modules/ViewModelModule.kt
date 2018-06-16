/*
 * Copyright (C) 2018 The Android Open Source Project
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

package ductranit.me.trackme.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider


import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ductranit.me.trackme.di.ViewModelKey
import ductranit.me.trackme.utils.ViewModelFactory
import ductranit.me.trackme.ui.main.viewmodels.MainViewModel
import ductranit.me.trackme.ui.record.viewmodels.TrackingViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrackingViewModel::class)
    abstract fun bindRecordViewModel(viewModel: TrackingViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

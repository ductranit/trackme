/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import ductranit.me.trackme.models.MyObjectBox
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    @Singleton
    fun provideAppContext(app: Application): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideResources(app: Application): Resources {
        return app.resources
    }

    @Provides
    @Singleton
    fun provideObjectBoxStore(app: Application): BoxStore {
        return MyObjectBox.builder().debugRelations()
                .androidContext(app)
                .build()
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }
}

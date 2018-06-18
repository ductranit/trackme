/*
 * Copyright (C) 2018 ductranit
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

package ductranit.me.trackme.ui.base.views

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import ductranit.me.trackme.di.Injectable
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

@SuppressLint("Registered")
abstract class BaseActivity<V : ViewDataBinding, M : ViewModel> : AppCompatActivity(), Injectable, HasSupportFragmentInjector {
    protected lateinit var binding: V
    protected lateinit var viewModel: M
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingView(layoutId())
        viewModel = ViewModelProviders.of(this, factory).get(viewModelClass())
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    protected abstract fun layoutId(): Int

    protected fun getActivity(): Activity {
        return this
    }

    private fun bindingView(layoutId: Int): V {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    private fun viewModelClass(): Class<M> {
        // As there is no official way to get generic type, so we need to use dirty hack
        // https://stackoverflow.com/a/1901275/719212
        @Suppress("UNCHECKED_CAST")
        return (javaClass.genericSuperclass as ParameterizedType)
                .actualTypeArguments[1] as Class<M>
    }
}
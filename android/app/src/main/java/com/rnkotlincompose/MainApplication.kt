package com.rnkotlincompose

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.PackageList
import com.rnkotlincompose.perse.bridge.PersePackage

class MainApplication : Application(), ReactApplication {
  private val rnHost: DefaultReactNativeHost = object : DefaultReactNativeHost(this) {
    override fun getPackages() = PackageList(this).packages + listOf(PersePackage())
  override fun getUseDeveloperSupport() = BuildConfig.DEBUG
  override fun getJSMainModuleName() = "index"
  }

  override val reactHost: ReactHost by lazy { getDefaultReactHost(applicationContext, rnHost) }

  override fun onCreate() {
    super.onCreate()
    loadReactNative(this)
  }
}

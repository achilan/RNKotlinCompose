package com.rnkotlincompose.perse.bridge

import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.rnkotlincompose.perse.ui.UsersScreen
import com.rnkotlincompose.perse.data.provideUsersRepository
import com.rnkotlincompose.perse.data.BuildConfig as DataBuildConfig

class PerseModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "PerseModule"

    @ReactMethod
    fun getVersion(callback: Callback) { callback.invoke("1.0") }
}

class PerseViewManager : SimpleViewManager<View>() {
    override fun getName() = "PerseView"
    override fun createViewInstance(reactContext: ThemedReactContext): View = ComposeView(reactContext).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                setContent {
                    MaterialTheme {
                        UsersScreen(
                            repositoryFactory = {
                                val tokenProvider = { DataBuildConfig.GOREST_TOKEN }
                                provideUsersRepository(DataBuildConfig.API_BASE_URL, tokenProvider)
                            }
                        )
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                // Drop composition on detach
                setContent { }
            }
        })
    }

    @ReactProp(name = "page")
    fun setPage(view: View, page: Int) {  }
}

class PersePackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> = listOf(PerseModule(reactContext))
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> = listOf(PerseViewManager())
}

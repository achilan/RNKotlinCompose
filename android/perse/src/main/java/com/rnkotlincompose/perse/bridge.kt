package com.rnkotlincompose.perse.bridge

import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.rnkotlincompose.perse.security.TokenRotator
import android.widget.Toast
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.rnkotlincompose.core.UsersRepository
import com.rnkotlincompose.perse.ui.UsersScreen
import com.rnkotlincompose.perse.data.ApiClientFactory
import com.rnkotlincompose.perse.data.UsersRepositoryImpl
import com.rnkotlincompose.perse.security.TokenStore

class PerseModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName() = "PerseModule"

    @ReactMethod
    fun getVersion(callback: Callback) {
        callback.invoke("1.0")
    }

    @ReactMethod
    fun setToken(token: String, callback: Callback) {
        try {
            TokenStore.saveToken(reactApplicationContext, token)
            callback.invoke(null, "ok")
        } catch (t: Throwable) {
            callback.invoke(t.message ?: "error")
        }
    }

    @ReactMethod
    fun rotateToken(callback: Callback) {
        val rotateUrl = BuildConfig.TOKEN_ROTATE_URL
        if (rotateUrl.isBlank()) {
            callback.invoke("rotate_url_empty")
            return
        }
        val current = TokenStore.getToken(reactApplicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            val res = TokenRotator.rotate(reactApplicationContext, rotateUrl, current)
            withContext(Dispatchers.Main) {
                res.fold(onSuccess = { newToken ->
                    callback.invoke(null, newToken)
                }, onFailure = { err ->
                    callback.invoke(err.message ?: "rotate_error")
                })
            }
        }
    }
}
class PerseViewManager : SimpleViewManager<View>() {
    override fun getName() = "PerseView"

    override fun createViewInstance(reactContext: ThemedReactContext): View {
        return ComposeView(reactContext).apply {
            setContent {
                MaterialTheme {
                    UsersScreen(
                        repositoryFactory = {
                            val initialToken = BuildConfig.GOREST_TOKEN
                            if (initialToken.isNotBlank()) {
                                TokenStore.saveToken(this.context, initialToken)
                            }

                            val tokenProvider = {
                                TokenStore.getToken(this.context)
                            }
                            val tokenRefresher = {
                                try {
                                    TokenRotator.rotateBlocking(this.context, BuildConfig.TOKEN_ROTATE_URL, TokenStore.getToken(this.context))
                                } catch (t: Throwable) { null }
                            }
                            val api = ApiClientFactory.create(BuildConfig.API_BASE_URL, tokenProvider, tokenRefresher)
                            UsersRepositoryImpl(api)
                        }
                    )
                }
            }
        }
    }

    @ReactProp(name = "page")
    fun setPage(view: View, page: Int) {
        
    }
}

class PersePackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> = listOf(PerseModule(reactContext))
    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> = listOf(PerseViewManager())
}

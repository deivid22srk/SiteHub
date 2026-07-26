package com.deivid22srk.sitehub.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.webkit.WebViewCompat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.deivid22srk.sitehub.SiteHubApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    siteId: Long,
    url: String,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as SiteHubApp
    val prefs = context.getSharedPreferences("sitehub_prefs", Context.MODE_PRIVATE)
    val fullscreenMode = prefs.getBoolean("fullscreen_mode", false)

    var canGoBack by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var containerRef by remember { mutableStateOf<FrameLayout?>(null) }
    var isCustomViewActive by remember { mutableStateOf(false) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    DisposableEffect(fullscreenMode) {
        if (fullscreenMode && activity != null) {
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        onDispose {
            if (activity != null) {
                val window = activity.window
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    BackHandler(enabled = isCustomViewActive) {
        customViewCallback?.onCustomViewHidden()
    }

    BackHandler(enabled = canGoBack && !isCustomViewActive) {
        webViewRef?.goBack()
    }

    BackHandler(enabled = !canGoBack && !isCustomViewActive) {
        onBack()
    }

    fun hideSystemBars() {
        activity?.let { act ->
            val window = act.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val ctrl = WindowInsetsControllerCompat(window, window.decorView)
            ctrl.hide(WindowInsetsCompat.Type.systemBars())
            ctrl.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun showSystemBars() {
        if (!fullscreenMode) {
            activity?.let { act ->
                val window = act.window
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val ctrl = WindowInsetsControllerCompat(window, window.decorView)
                ctrl.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun setupDocumentStartScripts(webView: WebView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scripts = app.userscriptRepository.getEnabledBySiteId(siteId)
                if (scripts.isNotEmpty()) {
                    val combinedJs = scripts.joinToString("\n") { it.scriptContent }
                    val wrappedJs = """
                        (function() {
                            try {
                                $combinedJs
                            } catch(e) {
                                console.error('[SiteHub Userscript Error]', e);
                            }
                        })();
                    """.trimIndent()
                    CoroutineScope(Dispatchers.Main).launch {
                        WebViewCompat.addDocumentStartJavaScript(webView, wrappedJs, setOf("*"))
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun injectSharedSession(webView: WebView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val site = app.repository.getById(siteId) ?: return@launch
                if (site.sharedGroupId <= 0) return@launch

                val syncJs = """
                    (function() {
                        var GROUP_KEY = '__sitehub_shared_${site.sharedGroupId}';
                        try {
                            var stored = localStorage.getItem(GROUP_KEY);
                            if (stored) {
                                var data = JSON.parse(stored);
                                Object.keys(data).forEach(function(k) {
                                    if (localStorage.getItem(k) === null) {
                                        localStorage.setItem(k, data[k]);
                                    }
                                });
                            }
                            var snapshot = {};
                            for (var i = 0; i < localStorage.length; i++) {
                                var key = localStorage.key(i);
                                if (key !== GROUP_KEY && key.indexOf('__sitehub') !== 0) {
                                    snapshot[key] = localStorage.getItem(key);
                                }
                            }
                            localStorage.setItem(GROUP_KEY, JSON.stringify(snapshot));

                            var origSetItem = Storage.prototype.setItem;
                            Storage.prototype.setItem = function(k, v) {
                                origSetItem.call(this, k, v);
                                if (this === localStorage && k !== GROUP_KEY && k.indexOf('__sitehub') !== 0) {
                                    try {
                                        var s = JSON.parse(localStorage.getItem(GROUP_KEY) || '{}');
                                        s[k] = v;
                                        localStorage.setItem(GROUP_KEY, JSON.stringify(s));
                                    } catch(e) {}
                                }
                            };
                            var origRemoveItem = Storage.prototype.removeItem;
                            Storage.prototype.removeItem = function(k) {
                                origRemoveItem.call(this, k);
                                if (this === localStorage && k !== GROUP_KEY) {
                                    try {
                                        var s = JSON.parse(localStorage.getItem(GROUP_KEY) || '{}');
                                        delete s[k];
                                        localStorage.setItem(GROUP_KEY, JSON.stringify(s));
                                    } catch(e) {}
                                }
                            };
                        } catch(e) {}
                    })();
                """.trimIndent()

                CoroutineScope(Dispatchers.Main).launch {
                    webView.evaluateJavascript(syncJs, null)
                }
            } catch (_: Exception) {}
        }
    }

    fun createWebView(ctx: Context): WebView {
        return WebView(ctx).apply {
            webViewRef = this

            setBackgroundColor(0xFF000000.toInt())
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                javaScriptCanOpenWindowsAutomatically = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                setSupportMultipleWindows(false)
                loadsImagesAutomatically = true
                blockNetworkImage = false
                blockNetworkLoads = false
                setGeolocationEnabled(true)
                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            }

            setupDocumentStartScripts(this)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    canGoBack = view?.canGoBack() ?: false
                    view?.let {
                        injectSharedSession(it)
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = newProgress / 100f
                }

                override fun onPermissionRequest(request: PermissionRequest?) {
                    request?.let { it.grant(it.resources) }
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view == null) return
                    customViewCallback = callback
                    isCustomViewActive = true

                    containerRef?.let { container ->
                        webViewRef?.visibility = View.GONE
                        view.layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        container.addView(view)
                    }

                    hideSystemBars()
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }

                override fun onHideCustomView() {
                    containerRef?.let { container ->
                        val childCount = container.childCount
                        for (i in childCount - 1 downTo 1) {
                            container.removeViewAt(i)
                        }
                    }
                    webViewRef?.visibility = View.VISIBLE
                    isCustomViewActive = false
                    customViewCallback = null

                    showSystemBars()
                    if (!fullscreenMode) {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    return false
                }
            }

            setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK &&
                    event.action == android.view.KeyEvent.ACTION_UP && canGoBack
                ) {
                    goBack()
                    true
                } else false
            }

            loadUrl(url)
        }
    }

    if (fullscreenMode) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    containerRef = this
                    addView(createWebView(ctx), FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (canGoBack) webViewRef?.goBack() else onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            if (progress < 1f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.padding(padding).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        containerRef = this
                        addView(createWebView(ctx), FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ))
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

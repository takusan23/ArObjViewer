package io.github.takusan23.arobjviewer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.ar.core.Config
import io.github.takusan23.arobjviewer.common.helpers.CameraPermissionHelper
import io.github.takusan23.arobjviewer.common.helpers.TapHelper
import io.github.takusan23.arobjviewer.common.samplerender.SampleRender
import io.github.takusan23.arobjviewer.component.ArActivityController

class ArActivity : AppCompatActivity() {

    /** ARCoreのセッション管理 */
    private val arCoreSessionLifecycleHelper by lazy { ARCoreSessionLifecycleHelper(this) }

    /** GLSurfaceView */
    private val arViewLifecycle by lazy { ARViewLifecycle(this) }

    /** タッチイベント */
    private val tapHelper by lazy { TapHelper(this).also { arViewLifecycle.viewBinding.activityMainGlSurfaceview.setOnTouchListener(it) } }

    /** OpenGLでARCore描画するやつ */
    private val renderer by lazy {
        val objectFilePath = intent.getStringExtra(KEY_OBJECT_FILE_PATH)!!
        ARCoreOpenGlRenderer(this, arCoreSessionLifecycleHelper, tapHelper, objectFilePath)
    }

    /** 権限コールバック */
    private val permissionRequester = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGrant ->
        if (isGrant) {
            setup()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(factory = {
                    arViewLifecycle.viewBinding.root
                })
                ArActivityController(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    onDelete = { renderer.deleteAllObject() },
                    onRotateLock = { renderer.isEnablePoseRotation = !renderer.isEnablePoseRotation },
                    onRotateX = { renderer.isForceXRotate = !renderer.isForceXRotate },
                    onRotateY = { renderer.isForceYRotate = !renderer.isForceYRotate },
                    onRotateZ = { renderer.isForceZRotate = !renderer.isForceZRotate },
                    onPlane = { renderer.isDrawPlane = !renderer.isDrawPlane }
                )
            }
        }

        // ライフサイクル
        lifecycle.addObserver(arCoreSessionLifecycleHelper)
        lifecycle.addObserver(arViewLifecycle)
        lifecycle.addObserver(renderer)

        // 権限がない場合は取得する
        if (CameraPermissionHelper.hasCameraPermission(this)) {
            setup()
        } else {
            permissionRequester.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun setup() {
        arCoreSessionLifecycleHelper.apply {
            // 失敗コールバック
            exceptionCallback = { exception ->
                exception.printStackTrace()
            }
            // 構成
            beforeSessionResume = { session ->
                session.configure(
                    session.config.apply {
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        // Depth API は使いたい
                        depthMode = if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                            Config.DepthMode.AUTOMATIC
                        } else {
                            Config.DepthMode.DISABLED
                        }
                        // インスタント配置は使わない
                        instantPlacementMode = Config.InstantPlacementMode.DISABLED
                    }
                )
            }
        }

        // システムUIを消す
        supportActionBar?.hide()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }

        // 描画する
        SampleRender(arViewLifecycle.viewBinding.activityMainGlSurfaceview, renderer, assets)
    }

    companion object {
        private const val KEY_OBJECT_FILE_PATH = "object_filepath"

        fun createIntent(context: Context, objectFilePath: String): Intent {
            return Intent(context, ArActivity::class.java).apply {
                putExtra(KEY_OBJECT_FILE_PATH, objectFilePath)
            }
        }

    }
}
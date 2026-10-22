package ru.zona.cinema

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val tv = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        setContent {
            ZonaApp(
                initial = if (tv) Screen.TvHome else Screen.Home,
                requestLandscape = { landscape ->
                    // A TV is always landscape; on a phone the player turns the screen.
                    if (!tv) {
                        requestedOrientation = if (landscape) {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                },
                backHandler = { enabled, onBack -> BackHandler(enabled, onBack) },
            )
        }
    }
}

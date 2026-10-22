package ru.zona.cinema.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import ru.zona.cinema.theme.ZonaColors

/** Result of a network load. */
sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Ready<T>(val value: T) : Load<T>
    data class Failed(val message: String) : Load<Nothing>
}

class LoadHandle<T>(val state: Load<T>, val retry: () -> Unit) {
    val value: T? get() = (state as? Load.Ready<T>)?.value
}

/** Runs [block] when [keys] change (and again on retry), exposing loading/ready/failed state. */
@Composable
fun <T> rememberLoad(vararg keys: Any?, block: suspend () -> T): LoadHandle<T> {
    var attempt by remember(*keys) { mutableIntStateOf(0) }
    val state by produceState<Load<T>>(Load.Loading, *keys, attempt) {
        value = Load.Loading
        value = try {
            Load.Ready(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Load.Failed(e.message ?: "Не удалось загрузить")
        }
    }
    return LoadHandle(state) { attempt++ }
}

@Composable
fun LoadingBlock(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(36.dp).semantics { contentDescription = "Загрузка" }, color = ZonaColors.Blue, strokeWidth = 3.dp)
    }
}

/** Load failure with a retry; [message] is shown under the heading. */
@Composable
fun ErrorBlock(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ZIcon(ZIcons.WifiOff, size = 32.dp, tint = ZonaColors.TextMuted)
        Text("Не удалось загрузить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(message, fontSize = 14.sp, color = ZonaColors.TextSecondary, textAlign = TextAlign.Center)
        PrimaryButton("Повторить", onRetry, Modifier.padding(top = 8.dp).fillMaxWidth(.6f), icon = ZIcons.Retry)
    }
}

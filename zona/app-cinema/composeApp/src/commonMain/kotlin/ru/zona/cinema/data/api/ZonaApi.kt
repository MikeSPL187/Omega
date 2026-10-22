package ru.zona.cinema.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Zona content API (the one Zona 3.x uses). Every method is a GET with one `params`
 * query parameter holding the JSON of its `ru.zona.content.api.*Params` model.
 *
 * Checked 22.10.2026: content methods answer without the `libzona.so` token. The hosts
 * often reset connections, so each call retries across [hosts] (the original app's
 * ApiHostManager does the same).
 */
class ZonaApi(
    private val client: HttpClient = HttpClient {
        // About half the fresh TLS connections to these hosts hang ~12 s and are then reset;
        // fail them fast and retry. Successful ones are reused (HTTP/2), ~0.3 s per call.
        install(HttpTimeout) {
            connectTimeoutMillis = 4_000
            socketTimeoutMillis = 4_000 // also bounds the TLS handshake
            requestTimeoutMillis = 15_000
        }
    },
    private val hosts: List<String> = listOf("https://apiwn1.mzona.net", "https://apirn1.mzona.net"),
) {
    // Nulls are left out: the server reads a present-but-null filter field as a value.
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }

    suspend fun search(query: String): List<ApiEntity> =
        call<EntitiesResponse>("/search", buildJsonObject { put("query", query) }).data

    /** One entity; the API takes a single id. [occupancy]: Stub (lists) or Full (card). */
    suspend fun entity(id: String, occupancy: String = "Stub"): ApiEntity? =
        call<EntitiesResponse>("/getMovieOrSerialById", buildJsonObject { put("id", id); put("occupancy", occupancy) }).data.firstOrNull()

    suspend fun movieIds(offset: Int, limit: Int, filter: Filter = Filter()) = ids("/getMovieIds", offset, limit, filter)
    suspend fun serialIds(offset: Int, limit: Int, filter: Filter = Filter()) = ids("/getSerialIds", offset, limit, filter)

    suspend fun episodes(entityId: String): List<ApiEpisode> =
        call<EpisodesResponse>("/getEpisodes", buildJsonObject { put("entityId", entityId) }).data

    private suspend fun ids(path: String, offset: Int, limit: Int, filter: Filter): IdsResponse =
        call(path, buildJsonObject {
            putJsonObject("page") { put("offset", offset); put("limit", limit) }
            put("filter", json.encodeToJsonElement(Filter.serializer(), filter))
        })

    /** Index of the host that answered last; requests start there. */
    private var preferred = 0
    private var hostPicked = false
    private val pickLock = Mutex()

    /**
     * Before the first request, ask every host for /getTime at once and keep the first to
     * answer (the original app's GoodHostProvider does a similar check). Its connection then
     * carries the following requests.
     */
    private suspend fun pickHost() = pickLock.withLock {
        if (hostPicked) return@withLock
        hostPicked = true
        val winner = channelFlow {
            hosts.forEachIndexed { i, host ->
                launch { if (runCatching { client.get("$host/getTime").status.isSuccess() }.getOrDefault(false)) send(i) }
            }
        }.firstOrNull()
        if (winner != null) preferred = winner
    }

    private suspend inline fun <reified T> call(path: String, params: JsonObject): T {
        pickHost()
        var last: Throwable? = null
        repeat(ATTEMPTS) { attempt ->
            val index = (preferred + attempt) % hosts.size
            val host = hosts[index]
            try {
                val response = client.get(host + path) { parameter("params", params.toString()) }
                val body = response.bodyAsText()
                if (!response.status.isSuccess()) {
                    // 4xx/5xx with a JSON error is a real answer: retrying will not change it.
                    throw ApiException(json.decodeFromString(ApiError.serializer(), body).let { "${it.error}: ${it.message}" })
                }
                preferred = index
                return json.decodeFromString(body)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                throw e
            } catch (e: Exception) {
                log?.invoke("$host$path attempt ${attempt + 1}: ${e::class.simpleName} ${e.message?.take(80)}")
                last = e
                delay(300L * (attempt + 1))
            }
        }
        throw ApiException("Сервер Zona не отвечает", last)
    }

    companion object {
        private const val ATTEMPTS = 6

        /** Receives failed attempts; set by the `checkApi` tool, null in the app. */
        var log: ((String) -> Unit)? = null
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * `ru.zona.content.api.Filter`. [sortBy]: popularity, release_date or rating. [genres] and
 * [countries] are API ids in one string, "|" between them meaning "any of" (the server
 * rejects JSON arrays here, and "8,6" matches nothing).
 */
@Serializable
data class Filter(
    val sortBy: String = "popularity",
    val genres: String? = null,
    val countries: String? = null,
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    val ratingFrom: Double? = null,
)

@Serializable
data class ApiEntity(
    @SerialName("_type") val type: String,
    val id: String,
    val name: Names,
    val year: Int? = null,
    val genreIds: List<String> = emptyList(),
    val countryIds: List<String> = emptyList(),
    val backdropId: Long? = null,
    val ratings: Ratings? = null,
    val description: Names? = null,
    val minAge: Int? = null,
    val runtime: Int? = null,
    val releaseDateInt: Long? = null,
    val directors: List<ApiPerson> = emptyList(),
    val actors: List<ApiPerson> = emptyList(),
    val rels: List<Rel> = emptyList(),
) {
    val isSeries get() = type == "Serial"
}

@Serializable
data class Names(val ru: String? = null, val en: String? = null, val original: String? = null) {
    val best get() = ru?.takeIf { it.isNotBlank() } ?: original?.takeIf { it.isNotBlank() } ?: en.orEmpty()
}

@Serializable
data class Ratings(val zona: Rating? = null, val imdb: Rating? = null, val kinopoisk: Rating? = null)

@Serializable
data class Rating(val value: Double? = null)

@Serializable
data class ApiPerson(val id: String, val name: Names)

/** A related title: [rmi] is its id, [p] the order. */
@Serializable
data class Rel(val rmi: Long, val p: Int = 0)

@Serializable
data class ApiEpisode(val title: Names? = null, val num: EpisodeNum, val releaseDate: Long? = null)

@Serializable
data class EpisodeNum(val season: Int, val episode: Int)

@Serializable
private class EntitiesResponse(val data: List<ApiEntity> = emptyList())

@Serializable
private class EpisodesResponse(val data: List<ApiEpisode> = emptyList())

@Serializable
class IdsResponse(val data: List<String> = emptyList(), val total: Int = 0)

@Serializable
private class ApiError(val error: String = "", val message: String = "")

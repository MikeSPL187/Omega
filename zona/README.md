# Zona 3.0.75 — разбор APK

Результат обратного проектирования `zona_3.0.75.apk` (package `mobi.zona`,
versionCode 1075). Здесь только описание, скрипты и карты имён — сам APK и
декомпилированный код в репозиторий не кладутся (`.gitignore`).

## Быстрый старт

```bash
zona/scripts/decompile.sh path/to/zona_3.0.75.apk   # → zona/work/{apktool,jadx}
# правки делаются в zona/work/apktool (smali + res)
zona/scripts/rebuild.sh                              # → zona/work/zona-mod.apk
```

`decompile.sh` сам скачивает jadx 1.5.1 и apktool 2.10.0. `rebuild.sh` требует
Android SDK build-tools (`zipalign`, `apksigner`). Проверено: `apktool b` на
нетронутом дереве собирается без ошибок.

- `zona/work/jadx` — читаемая Java, **только для анализа** (обратно не собирается).
- `zona/work/apktool` — smali + ресурсы, **это то, что правится и пересобирается**.

## Общие сведения

| | |
|---|---|
| Package | `mobi.zona` |
| minSdk / targetSdk / compileSdk | 21 / 33 / 36 |
| Язык | Kotlin 2.x, R8 (полная обфускация, map-id `eb26a91a…`) |
| DEX | 4 файла, ~19 500 классов |
| Платформы | телефон + Android TV (leanback, `TvSearchProvider`, EPG) |
| Native | `libzona.so` (своя), `libconscrypt_jni`, `librtmp-jni`, `librenderscript-toolkit` |
| Подпись | v1 (`CERT.RSA`) + v2 |

### Стек

- **UI:** Jetpack Compose + Compose Multiplatform resources (`ru.zona.common.generated.resources`),
  отдельные наборы экранов `screens/ui/mobile` и `screens/ui/tv`.
- **Навигация/архитектура:** Decompose (`IRootComponent.Config`, `IProfileRootComponent.Config` …).
- **Сеть:** Ktor client (+ OkHttp engine), kotlinx.serialization.
- **Плеер:** AndroidX Media3 / ExoPlayer, PiP (`ru.zona.app.pip`), Cast.
- **Хранилище:** Room, DataStore, WorkManager (загрузки — `StreamDownloadWorker`).
- **Изображения:** Coil.
- **JS:** Mozilla Rhino; `assets/player_script.js` — инжект в YouTube-плеер (трейлеры).
- **Реклама:** Yandex Mobile Ads, собственный VAST-движок (`ru.zona.ads.vast*`), IMA/PAL.
- **Аналитика/пуши:** AppMetrica, Firebase Messaging (`ZonaMessagingService`).
- **Логин:** Yandex AuthSDK, Telegram OAuth, телефон, QR/кросс-авторизация с ТВ.

## Как читать обфусцированный код

Имена пакетов/классов приложения заменены на `pg`, `Hg`, `rc` и т.п. Но Kotlin
оставил две утечки, из которых построены карты:

- [`mapping/smap_sources.tsv`](mapping/smap_sources.tsv) — ~760 классов → исходный
  `.kt` путь (из `@SourceDebugExtension`/SMAP).
- [`mapping/serial_names.tsv`](mapping/serial_names.tsv) — ~260 `@Serializable`
  классов → полное оригинальное имя (из дескрипторов kotlinx.serialization).

Пример: `pg/C7662a` = `ru/zona/app/services/host/ApiHostManager`.
В jadx-gui можно переименовывать классы по этим таблицам (Rename → сохраняется в проект `.jadx`).

Классы, которые не обфусцированы вовсе: `ru.zona.app.android.{App,MainActivity}`,
`ru.zona.app.services.download.*`, все модели `mobi.zona.data.model.*`,
`mobi.zona.utils.CppUtil`.

## Карта модулей (оригинальные имена → обфусцированные)

| Модуль | Ключевые классы |
|---|---|
| Хосты / failover | `ApiHostManager` `pg/C7662a`, `GoodHostProviderNew` `pg/C7680t`, `HostsConfig` `pg/S`, `HostRecoveryCoordinator` `pg/D` |
| Контент API | `ru.zona.client.api.entity.impl.*` (`rh/`), `SearchService` `uh/C8457m`, `ServerTimeProvider` `kc/C6812d` |
| Потоки | `PlayerStreamService` `Fg/o`, `MovieStreamService` `Hg/k`, `ChannelStreamService` `Gg/a`, `SportStreamService` `Ig/d`, `OfflinePlayerStreamService` `Fg/j` |
| Источники видео | `ru.zona.api.stream.{alloha,filmix,fanserials,link,takedwn}` |
| Загрузки | `ru.zona.app.services.download.StreamDownloadWorker` (не обфусцирован) |
| Реклама | `AdsService` `jg/C5968b`, `PlayerInstreamAdService` `jg/m`, `AdProviderChain` `Rb/b`, `VastAdProvider` `Tb/c` |
| Обновления | `UpdateServiceAndroid` `Eg/n`, `UpdateZonaApi` `Eg/u` |
| Авторизация | `ru.zona.auth.impl.*` (`oh/`), `SyncUserApi` `hi/Q`, `UserApiHttp` `hi/g0` |
| Синхронизация | `SyncManager` `hi/C4775n`, `VariableApiHttp` `hi/h0` |
| Подписка | `PremiumService` `ug/C8444h`, `ru.zona.payment.api.impl.*` (`Vh/`) |

## Сеть

### Хосты (захардкоженный fallback, `HostsConfig` в `pg/S`)

| Роль | Хосты |
|---|---|
| api | `apirn1.mzona.net`, `apiwn1.mzona.net` |
| update | `apir0.mzona.net`, `apiw0.mzona.net` |
| stream | `vsr01.zonasearch.com`, `vsw01.zonasearch.com` |
| image | `imgr1.imgzona.video`, `imgw1.imgzona.video` |
| sync | `sync.zonasearch.com` |
| auth | `az1.zona.pub/v1/auth` |

`ApiHostManager` выбирает конфиг по цепочке `cached → default/remote → hardcoded`,
проверяет хосты HEAD-запросами (`GoodHostProviderNew`) и переключается при сбоях.
Поля конфига: `api, update, stream, image, config, check.urls, checkGoodHostDelay`.
Сервер обновлений может вернуть новый `apiHost` (`Update.apiHost`).

### Эндпоинты (найдены в коде)

- Контент: `/search`, `/getMetadata`, `/getMovieOrSerialById`, `/getMovieIds`,
  `/getSerialIds`, `/getEpisodes`, `/getVideoSources`, `/getTvChannels`,
  `/getEntitiesByPerson`, `/getEntityByLink`, `/getEntityShareUrl`, `/getTime`,
  `/urlStatus`, `/userFeedback`, `/sendLog`, `/api/command/invoke`, `/api/data/collect`
- Обновления: `/getUpdate?app_type=<flavor>&versionCode=<n>`, `/fupdate/check`, `/fupdate/result`
- Пользователь: `/api/user/{login,registration,loadUser,findUser,setPassword,remindPassword,reSendConfirm,setAvaFromDefault}`,
  `/api/user/ava/default/list`, `/api/variable/{getVariables,updateVariables}`,
  `/verify/{loginByYandex,loginByTelegram}`
- Кросс-авторизация: `/auth/cross/{getChallenge,createSession,authorize,getToken}`
- Оплата: `/payment/tariff/getTariffsData`, `/payment/order/{create,createByPromo,current,getPaymentUrl,confirmEmbedded,refuse}`

### Подпись запросов — `libzona.so`

`mobi.zona.utils.CppUtil` объявляет `checkTimeNative(Context, time, userAgent)`,
`checkTimeNative2(...)`, `d()`, `eso()`, `lof(filter)`.

- `App.java` (~стр. 343) и `Id/e.java` берут серверное время (`/getTime`,
  `ServerTimeProvider`) и получают из `checkTimeNative[2]` токен для запросов.
- `Bd/u.java` добавляет заголовок `ld` = `CppUtil.d()`.
- Библиотека защищена обфускатором control-flow (`obf::next_step_functor`,
  `obf::bool_functor`), строки зашифрованы, символы удалены.
- Функция получает `Context` — **вероятно**, в расчёт токена входит подпись/имя
  пакета APK. Не подтверждено, но это главный риск для пересборки (см. ниже).

### Проверка: принимает ли API сторонний клиент (22.10.2026)

Простые GET-запросы с `curl` без `libzona.so`, без заголовка `ld` и без токена `checkTimeNative`:

- Параметры передаются одним query-параметром `params` — JSON модели `*Params`
  (`ru.zona.content.api.*`). На неверный JSON сервер отвечает 500 и перечисляет обязательные поля.
- `/getMetadata` (поля `appType, hasLeanbackFeature, hasCamera, hasTouchscreen, isInTouchMode, hasGps`),
  `/search` (`query`) и `/getVideoSources` (`kinopoiskId, episodeKey`) отвечают **200 с данными**.
  Подпись для каталога не проверяется.
- `/getVideoSources` отдаёт не ссылку на видео, а описатели источника
  (`video_source_type_id`, `download_link_key`). Сам поток клиент получает из сторонних
  источников (`ru.zona.api.stream.*`). Нужна ли там подпись, не проверялось.
- Карточки отдаются по одной: `/getMovieOrSerialById` принимает один `id` (`occupancy`: `Stub`,
  `Lite`, `Full`); списки (`/getMovieIds`, `/getSerialIds`) возвращают только id и `total`.
- Фильтр: `{"page":{"offset","limit"},"filter":{"sortBy":"popularity|release_date|rating","genres":"8|6",
  "countries":"2","yearFrom":2020,"ratingFrom":7}}`. Несколько id — через `|` (это «ИЛИ»);
  массив JSON сервер не принимает, а `"8,6"` даёт пустой ответ.
- Картинки (`Ih/u.java`): `https://imgr1.imgzona.video/images/film_240/{id/1000}/{id}.jpg` (постер),
  `/images/backdrop_1920/{backdropId/1000}/{backdropId}.jpg` (фон), `/images/actor/{id/1000}/{id}.jpg`.
  Других размеров постера сервер не отдаёт (редирект на 404).
- Названия жанров и стран клиент берёт из своих строк (`MessageBundle_ru.properties`, `movie.genre.N`,
  `movie.country.N`), API присылает только id.
- Хосты `apirn1`/`apiwn1` часто сбрасывают TLS-соединение (`connection reset`): примерно каждый
  второй запрос; из контейнера такое соединение висит ~12 с. Сервер поддерживает HTTP/2, и
  переиспользуемое соединение отвечает за ~0,3 с. Поэтому клиенту нужны короткие таймауты,
  повторы, выбор «живого» хоста и одно общее соединение (сделано в `app-cinema`, `ZonaApi`).

## Модели данных (`mobi.zona.data.model`)

- `Movie`: `id, coverUrl, year, description, quality, genres, countries, zonaRating,
  imdbRating, ksRating, directors, actors, originalName, duration, serial, isLiked,
  isWatched, posterTemplate, vast, adsModel, …`
- `StreamInfo`: `url, translation, language, quality, subtitleList, userAgent,
  headers, isTrailer, videoSourceTypeId, codec, …`
- `Settings` (удалённый конфиг): `ads2, mytarget*, yak, noad_threshold,
  loadAdTimeoutMs, sync.server, analytic.url, payment.enabled, check.urls, logLevel, links, …`
- `Update`: `apkUrl, apiHost, vastUrl, whatsNew, versionCode`

## Что учесть при модификации

1. **Подпись APK меняется при любой пересборке.** Если `libzona.so` учитывает
   подпись, сервер начнёт отклонять запросы контента. Проверять первым делом:
   собрать нетронутое дерево `rebuild.sh`, установить, открыть любой фильм.
2. **Установка рядом с оригиналом** — сменить `package` в `AndroidManifest.xml`
   (`apktool.yml → renameManifestPackage`), а также `authorities` у провайдеров
   и кастомные permission `mobi.zona.*`. Native-методы привязаны к имени класса
   `mobi.zona.utils.CppUtil`, а не к package — их не трогать.
3. **Автообновление** (`UpdateServiceAndroid`, `REQUEST_INSTALL_PACKAGES`)
   поставит поверх мода официальную версию — его стоит отключить первым.
4. **Безопасные точки для своих изменений:** ресурсы/тема (`res/`, `composeResources`),
   строки, хосты в `pg/S` (если поднимаете свой прокси/зеркало API),
   `player_script.js`, параметры плеера Media3, логика загрузок
   (`StreamDownloadWorker` — читаемый, не обфусцирован).
5. Для серьёзных изменений проще писать **свой клиент** на тех же моделях
   (`mobi.zona.data.model` — готовые схемы JSON), чем патчить smali.

## Чего здесь нет

Обход подписки/премиума и вырезание рекламы как монетизации я не описывал —
это коммерческие функции владельца приложения. Также учтите, что источники
видео (`alloha`, `filmix`, `fanserials`) — сторонние сайты, и права на
контент у них, как правило, отсутствуют.

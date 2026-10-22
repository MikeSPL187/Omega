# Zona Cinema — реализация редизайна

Compose Multiplatform-модуль с новым UI Zona по макетам `zona/design/cinema/`
(канвас: https://claude.ai/artifact/4sH6sSDGkzXmtM4GzSiuWV). Весь UI в `commonMain`,
поэтому один и тот же код работает на Android и на десктопе (для быстрого просмотра).

## Что есть

| Файл | Содержимое |
|---|---|
| `theme/Theme.kt` | Палитра Zona Cinema, шрифты (Roboto, Roboto Condensed, Oswald, Unbounded, Playfair Display, Russo One), типографика, формы |
| `ui/ZIcons.kt` | Иконки из макетов (SVG-пути → `ImageVector`), логотип Zona |
| `ui/Components.kt` | Стекло, кнопки-пилюли, чипы, заголовки рядов, постер с «леттерингом», кадр с прогрессом, плавающее меню |
| `screens/HomeScreen.kt` | Главная (API): баннер популярного, «Продолжить просмотр» (история сессии), «Топ-10 фильмов», популярные сериалы, новинки, популярные фильмы |
| `screens/CardScreen.kt` | Карточка (API): фон, рейтинги, «Смотреть», сезоны и серии, «О фильме», актёры с фото, «Похожие» |
| `screens/PlayerScreen.kt` | Плеер: пауза, ±10 с, шкала с перемоткой, «Пропустить заставку», «Следующая серия», субтитры, скорость, панель «Озвучка и качество», индикатор буферизации, экран ошибки с «Повторить» |
| `player/VideoPlayer.kt` | `VideoPlayer` (expect/actual): Android — Media3 ExoPlayer + `PlayerView` (видео и субтитры), десктоп — заглушка-таймер |
| `screens/CatalogScreen.kt` | Каталог (API): фильмы/сериалы, сортировка, жанры, страна, годы, рейтинг КП, подгрузка страниц; шторка фильтров с числом результатов |
| `screens/SearchScreen.kt` | Поиск (API) по мере ввода, недавние запросы, жанры |
| `screens/MyScreen.kt` | «Моё»: смотрю сейчас, избранное с «+1 серия», загрузки; фильтр-чипы |
| `screens/ProfileScreen.kt` | Профиль: аккаунт, премиум, вход на ТВ по QR, настройки |
| `screens/DownloadsScreen.kt` | Загрузки: память, прогресс, очередь, ошибка с «Повторить», офлайн-просмотр |
| `screens/LoginScreen.kt` | Вход: Яндекс ID, Telegram, почта, «Не сейчас» |
| `screens/OfflineScreen.kt` | «Нет соединения» с переходом к загруженному |
| `screens/TrailerFeedScreen.kt` | Вертикальная лента трейлеров (свайп вверх) |
| `App.kt` | Навигация: вкладки нижнего меню + стек экранов, хуки поворота и «Назад» |
| `data/api/ZonaApi.kt` | Клиент API Zona (Ktor): `search`, `getMovieIds`/`getSerialIds`, `getMovieOrSerialById`, `getEpisodes`; выбор хоста, повторы |
| `data/api/Dictionaries.kt` | Названия жанров и стран по id (из строк оригинала 3.0.75) |
| `data/Catalog.kt` | Репозиторий: API → модели UI, кэш по id, адреса постеров/фонов/фото актёров |
| `data/History.kt` | «Продолжить просмотр»: что смотрели в этой сессии (в памяти) |
| `ui/Load.kt` | `rememberLoad`: загрузка / ошибка с «Повторить» / готово |
| `data/Demo.kt`, `data/Library.kt` | Демо-данные (выдуманные названия и цифры) для экранов без API |

Видео: на Android играет Media3 ExoPlayer 1.11.1. Пока все серии воспроизводят публичный тестовый
HLS Apple (`Demo.TEST_STREAM`: 1080p…270p, субтитры WebVTT). Позиция, пауза, скорость, субтитры
(`TRACK_TYPE_TEXT`) и качество (ограничение высоты видео; «Авто» — адаптивно) идут в плеер. Озвучка
переключается только в UI — у тестового потока нет дорожек-«озвучек», их даст API Zona. При уходе
приложения в фон плеер ставится на паузу; экран не гаснет, пока идёт видео.

Данные: Главная, Поиск, Каталог и Карточка берут каталог из API Zona (без подписи `libzona.so`,
см. `zona/README.md`). Картинки грузит Coil 3. Пока на демо-данных: «Моё», загрузки, лента трейлеров,
сцены, разделы ТВ и Спорт, профиль и вход — им нужен аккаунт или видеопотоки. Видео во всех случаях
тестовое. Android TV не делаем. Экран «Нет сети» в превью открывается из профиля.

## Сборка

Нужен доступ к Google Maven (`dl.google.com`): Compose тянет оттуда пакеты `androidx`,
даже для десктопа.

```bash
cd zona/app-cinema
./gradlew :composeApp:run                 # окно 390×844, плеер поворачивает его в 844×390
./gradlew :composeApp:renderScreens       # PNG ключевых экранов в composeApp/build/screens (нужна сеть: API)
./gradlew :composeApp:checkApi            # запросы к API через Catalog с замером времени
./gradlew -Pzona.android=true :composeApp:assembleDebug   # APK (нужен Android SDK)
```

Android: `androidMain/MainActivity.kt` (edge-to-edge, поворот в горизонталь для плеера,
системная «Назад»). Нужен Android SDK с `platforms;android-36` (Media3 1.11 требует compileSdk 36)
и `build-tools;35.0.0`, `local.properties` → `sdk.dir`; minSdk 24, targetSdk 35.

## Статус

Проверено: desktop-сборка, `renderScreens` (экраны сверены с макетами), debug-APK
(`./gradlew -Pzona.android=true :composeApp:assembleDebug`). На реальном устройстве не запускалось.

Шрифты — Google Fonts (OFL / Apache 2.0). Обложки — процедурные (`zona/design/v3/scenes.py`).

# Zona Cinema — реализация редизайна (этап 1)

Compose Multiplatform-модуль с новым UI Zona по макетам `zona/design/cinema/`
(канвас: https://claude.ai/artifact/4sH6sSDGkzXmtM4GzSiuWV). Весь UI в `commonMain`,
поэтому один и тот же код работает на Android и на десктопе (для быстрого просмотра).

## Что есть

| Файл | Содержимое |
|---|---|
| `theme/Theme.kt` | Палитра Zona Cinema, шрифты (Roboto, Roboto Condensed, Oswald, Unbounded, Playfair Display, Russo One), типографика, формы |
| `ui/ZIcons.kt` | Иконки из макетов (SVG-пути → `ImageVector`), логотип Zona |
| `ui/Components.kt` | Стекло, кнопки-пилюли, чипы, заголовки рядов, постер с «леттерингом», кадр с прогрессом, плавающее меню |
| `screens/HomeScreen.kt` | Главная: баннер, «Продолжить просмотр», «Трейлеры и сцены», «Топ-10», новинки |
| `screens/CardScreen.kt` | Карточка: обложка, «Смотреть · сезон/серия», действия, вкладки Серии/Сцены/О сериале/Похожие |
| `screens/PlayerScreen.kt` | Плеер (хром без видео): пауза, ±10 с, шкала с перемоткой, «Пропустить заставку», «Следующая серия», субтитры, скорость, панель «Озвучка и качество» |
| `App.kt` | Навигация (стек экранов), хуки для поворота и системной кнопки «Назад» |
| `data/Demo.kt` | Демо-каталог (выдуманные названия и цифры) |

Не входит в этап 1: каталог, поиск, «Моё», профиль, фильтры, загрузки, вход, «Нет сети»,
вертикальная лента трейлеров, Android TV; реальные данные (API Zona) и видео (Media3).

## Сборка

Нужен доступ к Google Maven (`dl.google.com`): Compose тянет оттуда пакеты `androidx`,
даже для десктопа.

```bash
cd zona/app-cinema
./gradlew :composeApp:run                 # окно 390×844, плеер поворачивает его в 844×390
./gradlew :composeApp:renderScreens       # PNG ключевых экранов в composeApp/build/screens
./gradlew -Pzona.android=true :composeApp:assembleDebug   # APK (нужен Android SDK)
```

Для Android в `androidMain` нужна `MainActivity` (ещё не добавлена):

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(); super.onCreate(savedInstanceState)
        setContent {
            ZonaApp(
                requestLandscape = { requestedOrientation = if (it) SCREEN_ORIENTATION_SENSOR_LANDSCAPE else SCREEN_ORIENTATION_UNSPECIFIED },
                backHandler = { enabled, onBack -> BackHandler(enabled, onBack) },
            )
        }
    }
}
```

## Статус

Код не компилировался: в облачном окружении, где он написан, сетевая политика
закрывает `dl.google.com`. Первая сборка с доступом к Google Maven может потребовать мелких правок.

Шрифты — Google Fonts (OFL / Apache 2.0). Обложки — процедурные (`zona/design/v3/scenes.py`).

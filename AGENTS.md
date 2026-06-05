# AGENTS.md for pdf-ai-mentor

## Project Overview
Android app for AI-powered PDF reading/mentoring. Built as native Kotlin with Jetpack Compose + Material3. Target: academic papers with AI chat (Gemini) for translation, explanations, follow-ups.

Agents MUST:
- Follow Android best practices.
- Use Kotlin coroutines/Flow for async.
- Never commit secrets (API keys in local.properties or .env).
- Run lint/tests after changes.
- Mimic existing code style.

## Directory Structure
```
.
├── app/
│   ├── src/main/
│   │   ├── java/ly/schil/pdfaimentor/   # Kotlin sources
│   │   │   ├── ui/theme/                 # Compose theme
│   │   │   └── MainActivity.kt
│   │   ├── res/                          # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts                  # App-level Gradle
├── gradle/
│   ├── libs.versions.toml                # Version catalog
│   └── wrapper/
├── build.gradle.kts                      # Project-level
├── settings.gradle.kts
├── gradle.properties
└── .github/workflows/ci.yml              # CI pipeline
```

## Essential Commands

### Build
```
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
./gradlew bundleRelease    # AAB
```

### Lint
```
./gradlew lint             # Android Lint
```

### Tests
```
./gradlew test                          # Unit tests
./gradlew connectedAndroidTest          # Instrumentation (device/emulator)
```

### Verification (run after changes)
```
./gradlew lint test assembleDebug
```

## Code Style

### Language & Framework
- **Primary**: Kotlin + Jetpack Compose + Material3
- **State**: ViewModel + StateFlow
- **Navigation**: Navigation Compose
- **AI**: com.google.ai.client.generativeai (Gemini)
- **HTTP**: Retrofit + OkHttp (when needed)

### Naming
- **Files**: PascalCase.kt (PdfViewerScreen.kt)
- **Composables/Classes**: PascalCase
- **Functions/Variables**: camelCase
- **Constants**: UPPER_SNAKE_CASE
- **Tests**: *Test.kt

### Build Config
- **Package**: `ly.schil.pdfaimentor`
- **compileSdk**: 35
- **minSdk**: 24
- **targetSdk**: 35
- **JVM target**: 17
- **Compose compiler**: via `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.0+)
- **Dependencies**: managed via `gradle/libs.versions.toml` version catalog + Compose BOM

### Compose Example
```kotlin
@Composable
fun PdfPageViewer(
    page: PdfPage,
    onProcess: (PdfPage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(page.text)
    }
}
```

### Error Handling
- Result/Flow for async
- Snackbar for user-facing errors
```kotlin
viewModelScope.launch {
    try {
        val result = withContext(Dispatchers.IO) { processPdf(file) }
    } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message) }
    }
}
```

## Commit Messages
```
feat: add pdf ai processor
fix: handle large pdf crash
refactor: extract hooks
docs: update README
test: add unit tests for ai service
```

## When Editing Files
1. Read file first.
2. Mimic existing style/imports.
3. Run lint/tests after edits.
4. Test changes.

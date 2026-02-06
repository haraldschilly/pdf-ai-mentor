# AGENTS.md for android-pdf-ai

## Project Overview
This is an Android application for AI-powered PDF processing. Built as native Kotlin Android app with Jetpack Compose. Uses libraries like react-native-pdf, react-native-vision-camera for OCR/AI, TensorFlow Lite or ML Kit for AI features.

Agents MUST:
- Follow Android best practices.
- Use Kotlin coroutines/Flow for async.
- Never commit secrets (API keys in .env).
- Run lint/typecheck/tests after changes.
- Mimic existing code style (check when files exist).

## Directory Structure (Standard Android Studio Project)
```
.
├── app/                  # Main app module
│   ├── src/main/
│   │   ├── java/com/example/pdfai/  # Kotlin sources
│   │   │   ├── ui/       # Compose screens/composables
│   │   │   ├── services/ # PDF/AI services
│   │   │   └── data/     # Repos/models
│   │   ├── res/          # Resources (layouts if XML)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts  # App-level Gradle
├── gradle/               # Wrapper
├── build.gradle.kts      # Project-level
├── settings.gradle.kts
├── gradle.properties
└── ktlint.yml            # KtLint config
```

## Essential Commands
Run in project root unless specified.

### Setup
```
# From Android Studio or CLI
./gradlew clean
```

### Development
```
./gradlew installDebug  # Build & install to emulator/device
# Or use Android Studio Run button
```

### Build
```
./gradlew assembleDebug   # Debug APK
./gradlew assembleRelease # Release APK
./gradlew bundleRelease   # AAB
```

### Lint & Format
```
./gradlew ktlintCheck     # KtLint
./gradlew ktlintFormat    # Auto-format
./gradlew lint            # Android Lint
./gradlew spotlessCheck   # If Spotless used
```

### Tests (JUnit, Robolectric, Compose UI Test)
```
./gradlew testDebugUnitTest      # Unit tests
./gradlew connectedDebugAndroidTest  # Instrumentation (emulator running)
```
**Single test:**
```
./gradlew testDebugUnitTest --tests=com.example.PdfViewerTest
./gradlew connectedDebugAndroidTest --tests=com.example.UiTest
```

### CI/CD (Add to package.json scripts)
```
npm run build:android  # Full build + test
npm run e2e            # Detox/Appium if setup
```

## Code Style Guidelines

### Language & Framework
- **Primary**: Kotlin + Jetpack Compose + ViewModel + Hilt/Dagger.
- **UI**: Jetpack Compose (Material3).
- **State**: ViewModel + StateFlow.
- **Navigation**: Navigation Compose.
- **PDF**: com.github.barteksc:AndroidPdfViewer or androidx.pdf:pdf-renderer.
- **AI/ML**: com.google.ai.client.generativeai (Gemini), Retrofit for OpenAI.
- **HTTP**: Retrofit + OkHttp.

### Naming Conventions
- **Files**: camelCase.kt (PdfViewerScreen.kt).
- **Composables/Classes**: PascalCase.
- **Functions/Variables**: camelCase.
- **Constants**: UPPER_SNAKE_CASE.
- **Tests**: *Test.kt (PdfViewerScreenTest.kt).

### Imports (Kotlin)
```
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.example.pdfai.services.PdfService
// Group: AndroidX > Material3 > Local > 3rd party
```

### Formatting (KtLint)
```
# ktlint.yml
ktlint_code_style: official
ktlint_disabled_rules:
- import-ordering
```
- `./gradlew ktlintFormat`


**Example Composable:**
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
- Result/Flow for async.
- Snackbar/Toast.
- Hilt for logging.
```kotlin
lifecycleScope.launch {
    try {
        val result = withContext(Dispatchers.IO) { processPdf(file) }
    } catch (e: Exception) {
        Snackbar.make(view, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
    }
}
```

### ViewModels & Repos
- ViewModel with StateFlow.
- Repository pattern with Hilt.

### Android-Specific (app/build.gradle.kts)
```
compileSdk = 34
defaultConfig {
    minSdk = 24
    targetSdk = 34
}
dependencies {
    implementation("androidx.compose.ui:ui:1.6.0")
    // etc.
}
```

### Testing
- Unit: JUnit + Mockito + Robolectric.
- Compose UI: compose-ui-test.
- Instrumentation: Espresso.
- E2E: Maestro or Compose UI tests.

### Security
- EncryptedSharedPreferences for keys.
- HTTPS with OkHttp.
- R8/ProGuard enabled.

### Performance
- LazyColumn for lists.
- Coil for image loading.
- Paging 3 for large data.

## Commit Messages
feat: add pdf ai processor
fix: handle large pdf crash
refactor: extract hooks
docs: update README
test: add unit tests for ai service

## When Editing Files
1. Read file first.
2. Mimic existing style/imports.
3. Run lint/typecheck after edits.
4. Test changes.

## Verification After Changes
```
./gradlew ktlintCheck lint testDebugUnitTest assembleDebug
```
If failures, fix before proceeding.

---
*Generated for agentic coding. Update as project evolves.* (148 lines)
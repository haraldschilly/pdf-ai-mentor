# TODO.md

## Phase 1: Project Setup (High Priority)
- [ ] Install Android Studio on Linux
- [ ] Create new native Android project with Kotlin and Jetpack Compose
- [ ] Set minSdk 24, targetSdk 34
- [ ] Add dependencies: PDF viewer (e.g., pdfium-android, barteksc/AndroidPdfViewer), Retrofit/OkHttp for API, Google Gemini SDK or REST API
- [ ] Configure permissions: READ_EXTERNAL_STORAGE, INTERNET
- [ ] Set up emulator: Tablet AVD (e.g., 10-inch Pixel Tablet)
- [ ] Basic lint/test setup (KtLint, JUnit)

## Phase 2: PDF Viewer (Left Side) (High Priority)
- [ ] Implement PDF file picker (Intent ACTION_OPEN_DOCUMENT)
- [ ] PDF rendering component: zoom, scroll, page navigation
- [ ] Text selection support on PDF pages
- [ ] Extract selected text
- [ ] Handle large PDFs efficiently (paging)

## Phase 3: AI Chat Column (Right Side) (High Priority)
- [ ] Split-screen layout (ConstraintLayout or Compose Row, landscape/portrait adaptive)
- [ ] Chat UI: input field, send button, message bubbles
- [ ] Pre-defined buttons: "Translate selection", "Explain selection", "Explain page"
- [ ] Chat history per PDF/session

## Phase 4: AI Integration (Medium Priority)
- [ ] Settings screen for API key (Gemini/OpenAI)
- [ ] Detect user locale for translation target language
- [ ] "Translate": Send selected text + locale to LLM
- [ ] "Explain": Send selected/page text with prompt "Explain this simply:"
- [ ] Chat follow-ups: Context-aware (include previous messages)
- [ ] Switch providers (Gemini first, OpenAI later)
- [ ] Loading states, error handling (no key, network fail)

## Phase 5: Authentication (Medium Priority)
- [ ] OAuth support for Gemini (Google Sign-In integration)
- [ ] OpenAI account link (if API supports)
- [ ] Fallback to manual API key entry
- [ ] Secure storage (EncryptedSharedPreferences)

## Phase 6: Polish & Features (Low Priority)
- [ ] Dark mode support
- [ ] Search in PDF
- [ ] Bookmarks/annotations
- [ ] Export chat responses

## Stretch Goals (Later)
- [ ] Multimodal: OCR/images from PDF pages to LLM (ML Kit Vision or Gemini vision)
- [ ] Offline mode (local LLM? Tiny models)
- [ ] Share/export processed content
- [ ] Multiple PDF tabs

## Verification
- Run `./gradlew lint ktlintCheck test build` after changes
- Test on emulator: PDF load, select text, AI responses, chat flow
- Edge cases: Large PDF, no internet, invalid key, RTL languages
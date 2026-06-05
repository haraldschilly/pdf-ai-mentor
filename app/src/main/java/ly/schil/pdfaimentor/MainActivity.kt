package ly.schil.pdfaimentor

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.barteksc.pdfviewer.PDFView
import ly.schil.pdfaimentor.ui.theme.PDFAIMentorTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PDFAIMentorTheme {
                MentorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorScreen(chatViewModel: ChatViewModel = viewModel()) {
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectMode by remember { mutableStateOf(false) }
    // Cropped PDF region waiting to be sent with the next chat message.
    var pendingCrop by remember { mutableStateOf<Bitmap?>(null) }
    // Reference to the underlying PDFView so we can screenshot it.
    var pdfViewRef by remember { mutableStateOf<PDFView?>(null) }

    // System file picker for PDFs. The persistable permission keeps the URI
    // readable across app restarts (needed later for "recent documents").
    val context = LocalContext.current
    val pickPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            pdfUri = uri
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("PDF AI Mentor") },
                actions = {
                    if (pdfUri != null) {
                        TextButton(onClick = { selectMode = !selectMode }) {
                            Text(if (selectMode) "Cancel selection" else "Ask about a part")
                        }
                    }
                    TextButton(onClick = { pickPdf.launch(arrayOf("application/pdf")) }) {
                        Text("Open PDF")
                    }
                },
            )
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Left: PDF viewer (60% of the width)
            Box(modifier = Modifier.weight(0.6f).fillMaxSize()) {
                val uri = pdfUri
                if (uri == null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "No document open",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Button(onClick = { pickPdf.launch(arrayOf("application/pdf")) }) {
                            Text("Open PDF")
                        }
                    }
                } else {
                    PdfViewer(
                        uri = uri,
                        modifier = Modifier.fillMaxSize(),
                        onViewCreated = { pdfViewRef = it },
                    )
                    if (selectMode) {
                        SelectionOverlay(
                            modifier = Modifier.fillMaxSize(),
                            onRegionSelected = { top, bottom ->
                                pdfViewRef?.let { view ->
                                    pendingCrop = cropOfView(view, top, bottom)
                                }
                                selectMode = false
                            },
                        )
                    }
                }
            }

            VerticalDivider()

            // Right: AI chat (40% of the width)
            ChatPanel(
                viewModel = chatViewModel,
                pendingCrop = pendingCrop,
                onClearCrop = { pendingCrop = null },
                modifier = Modifier.weight(0.4f).fillMaxSize(),
            )
        }
    }
}

/**
 * Screenshot the PDFView as currently rendered and crop the vertical band
 * between [top] and [bottom] (full width). Rendering the on-screen view keeps
 * formulas/figures exactly as the reader sees them — the core idea of the app.
 */
private fun cropOfView(view: PDFView, top: Float, bottom: Float): Bitmap? {
    if (view.width == 0 || view.height == 0) return null
    val y0 = max(0, min(top, bottom).toInt())
    val y1 = min(view.height, max(top, bottom).toInt())
    if (y1 - y0 < 24) return null // ignore accidental taps

    val full = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    view.draw(AndroidCanvas(full))
    val crop = Bitmap.createBitmap(full, 0, y0, view.width, y1 - y0)
    full.recycle()
    return crop
}

/** Vertical swipe-select: drag across the unclear part, release to capture. */
@Composable
fun SelectionOverlay(
    modifier: Modifier = Modifier,
    onRegionSelected: (top: Float, bottom: Float) -> Unit,
) {
    var dragStart by remember { mutableStateOf<Float?>(null) }
    var dragCurrent by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    dragStart = offset.y
                    dragCurrent = offset.y
                },
                onDrag = { change, _ ->
                    dragCurrent = change.position.y
                },
                onDragEnd = {
                    dragStart?.let { start ->
                        if (abs(dragCurrent - start) >= 24f) {
                            onRegionSelected(start, dragCurrent)
                        }
                    }
                    dragStart = null
                },
                onDragCancel = { dragStart = null },
            )
        },
    ) {
        val highlight = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxSize()) {
            dragStart?.let { start ->
                val top = min(start, dragCurrent)
                val bottom = max(start, dragCurrent)
                drawRect(
                    color = highlight.copy(alpha = 0.25f),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, bottom - top),
                )
            }
        }
        if (dragStart == null) {
            Card(
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Text(
                    "Drag vertically across the part you want to ask about",
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/** Wraps the classic (View-based) PDFView library for use in Compose. */
@Composable
fun PdfViewer(
    uri: Uri,
    modifier: Modifier = Modifier,
    onViewCreated: (PDFView) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx -> PDFView(ctx, null).also(onViewCreated) },
        update = { view ->
            // `update` runs on every recomposition — only (re)load when the
            // document actually changed, otherwise zoom/scroll would reset.
            if (view.tag != uri) {
                view.tag = uri
                view.fromUri(uri)
                    .enableAnnotationRendering(true)
                    .spacing(8) // px between pages
                    .load()
            }
        },
    )
}

@Composable
fun ChatPanel(
    viewModel: ChatViewModel,
    pendingCrop: Bitmap?,
    onClearCrop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to the newest message.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = modifier.imePadding()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) { message -> MessageBubble(message) }
        }

        // Pending selection: preview of the cropped region to be sent.
        if (pendingCrop != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    bitmap = pendingCrop.asImageBitmap(),
                    contentDescription = "Selected region",
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Fit,
                )
                TextButton(onClick = onClearCrop) { Text("✕") }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (pendingCrop != null) "Question about the selection (optional)…"
                        else "Ask about the document…",
                    )
                },
                enabled = !isLoading,
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            } else {
                Button(
                    onClick = {
                        viewModel.send(input, pendingCrop)
                        input = ""
                        onClearCrop()
                    },
                    enabled = input.isNotBlank() || pendingCrop != null,
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val colors = when (message.author) {
        Author.USER -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
        Author.MODEL -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Author.ERROR -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        )
    }
    Card(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            message.image?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Attached selection",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth,
                )
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

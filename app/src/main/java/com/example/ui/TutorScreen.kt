package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HistoryEntity
import com.example.ui.theme.KeralaGreen
import com.example.ui.theme.KeralaGreenLight
import com.example.ui.theme.KsrtcOrange
import com.example.ui.theme.TraditionalGold
import com.example.ui.theme.TraditionalGoldDark
import com.example.ui.theme.CharcoalNavy
import com.example.ui.viewmodel.TutorUiState
import com.example.ui.viewmodel.TutorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorScreen(
    viewModel: TutorViewModel,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val bookmarkedList by viewModel.bookmarkedList.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Search / Answer Active, 1 = History

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "കൂട്ടുകാരൻ ",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "ചരിത്രം ഒഴിവാക്കുക (Clear History)",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "പഠിക്കുക") },
                    label = { Text("പഠിക്കാം (Learn)", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KeralaGreen,
                        selectedTextColor = KeralaGreen,
                        indicatorColor = KeralaGreenLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.List, contentDescription = "പഠനക്കുറിപ്പുകൾ") },
                    label = { Text("കുറിപ്പുകൾ (Notes)", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KeralaGreen,
                        selectedTextColor = KeralaGreen,
                        indicatorColor = KeralaGreenLight
                    )
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                },
                label = "TabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> SearchAndDisplayTab(
                        viewModel = viewModel,
                        uiState = uiState,
                        historyCount = historyList.size,
                        onHistorySelected = { item ->
                            viewModel.selectHistoryItem(item)
                            selectedTab = 0 // stay on study page
                        },
                        onBackToSearch = { viewModel.resetState() },
                        onBookmarkToggled = { id, current -> viewModel.toggleBookmark(id, current) },
                        historyList = historyList
                    )
                    1 -> HistoryNotebookTab(
                        history = historyList,
                        onSelectItem = { item ->
                            viewModel.selectHistoryItem(item)
                            selectedTab = 0 // go back to explain tab to view it
                        },
                        onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                        onToggleBookmark = { id, current -> viewModel.toggleBookmark(id, current) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndDisplayTab(
    viewModel: TutorViewModel,
    uiState: TutorUiState,
    historyCount: Int,
    onHistorySelected: (HistoryEntity) -> Unit,
    onBackToSearch: () -> Unit,
    onBookmarkToggled: (Int, Boolean) -> Unit,
    historyList: List<HistoryEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        when (uiState) {
            is TutorUiState.Idle -> {
                item { WelcomeHeader() }
                item {
                    TutorInputs(
                        viewModel = viewModel,
                        onExplainClick = { viewModel.explainTerm() }
                    )
                }
                if (historyList.isNotEmpty()) {
                    item {
                        Text(
                            text = "അവസാനം പഠിച്ച കാര്യങ്ങൾ (Recent Lessons)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = KeralaGreen,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(historyList.take(3)) { item ->
                        SearchHistoryQuickCard(item = item, onClick = { onHistorySelected(item) })
                    }
                }
            }
            is TutorUiState.Loading -> {
                item { WelcomeHeader() }
                item { LoadingCard() }
            }
            is TutorUiState.Success -> {
                // Check if current success item is bookmarked in database
                val isCurrentBookmarked = historyList.find { it.englishTerm.equals(uiState.englishTerm, ignoreCase = true) }?.isBookmarked ?: false
                
                item {
                    TutorExplanationCard(
                        state = uiState,
                        isBookmarked = isCurrentBookmarked,
                        onBackClick = onBackToSearch,
                        onBookmarkToggled = {
                            uiState.savedId?.let { id ->
                                onBookmarkToggled(id, isCurrentBookmarked)
                            }
                        }
                    )
                }
            }
            is TutorUiState.Error -> {
                item { WelcomeHeader() }
                item {
                    ErrorCard(
                        message = uiState.message,
                        onDismiss = onBackToSearch
                    )
                }
                item {
                    TutorInputs(
                        viewModel = viewModel,
                        onExplainClick = { viewModel.explainTerm() }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = KeralaGreenLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hand-built vector mascot illustration in a Box using Compose Canvas
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(2.dp, TraditionalGold, shape = CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                MascotDrawCanvas()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "കുട്ടുകാരൻ ട്യൂട്ടർ 🎓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = KeralaGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ഹലോ കൂട്ടുകാരാ! സയൻസിലെയും മാത്സിലെയും വലിയ ഡെഫനിഷനുകൾ കണ്ട് പേടിക്കണ്ട! വാക്കുകൾ ഇവിടെ കൊടുക്കൂ, നമുക്ക് നമ്മുടെ നാട്ടിൻപുറത്തെ രസകരമായ കാര്യങ്ങളുമായി കൂട്ടിയിണക്കി പഠിക്കാം!",
                    fontSize = 13.sp,
                    color = CharcoalNavy,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Draws a beautiful baby tutor elephant character strictly using Compose Canvas
 * elements (ears, eyes, trunk, and a small gold crown or pencil). This serves
 * as an exceptionally premium substitute for missing local bitmap assets!
 */
@Composable
fun MascotDrawCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // 1. Draw large circular ears (Kerala/Elephant tradition)
        drawCircle(
            color = Color(0xFFB0BEC5), // Slate gray
            radius = width * 0.25f,
            center = Offset(centerX - width * 0.22f, centerY - height * 0.05f)
        )
        drawCircle(
            color = Color(0xFFB0BEC5),
            radius = width * 0.25f,
            center = Offset(centerX + width * 0.22f, centerY - height * 0.05f)
        )
        // Inner ears pink
        drawCircle(
            color = Color(0xFFFFCDD2), // Soft pink
            radius = width * 0.15f,
            center = Offset(centerX - width * 0.22f, centerY - height * 0.05f)
        )
        drawCircle(
            color = Color(0xFFFFCDD2),
            radius = width * 0.15f,
            center = Offset(centerX + width * 0.22f, centerY - height * 0.05f)
        )

        // 2. Head
        drawCircle(
            color = Color(0xFFCFD8DC), // Light gray head
            radius = width * 0.32f,
            center = Offset(centerX, centerY + height * 0.02f)
        )

        // 3. Eyes (Cute cartoon style)
        drawCircle(
            color = Color.Black,
            radius = width * 0.04f,
            center = Offset(centerX - width * 0.11f, centerY - height * 0.02f)
        )
        drawCircle(
            color = Color.Black,
            radius = width * 0.04f,
            center = Offset(centerX + width * 0.11f, centerY - height * 0.02f)
        )
        // Eye reflections
        drawCircle(
            color = Color.White,
            radius = width * 0.012f,
            center = Offset(centerX - width * 0.12f, centerY - height * 0.03f)
        )
        drawCircle(
            color = Color.White,
            radius = width * 0.012f,
            center = Offset(centerX + width * 0.10f, centerY - height * 0.03f)
        )

        // 4. Cute blushing cheeks
        drawCircle(
            color = Color(0xFFFF8A80).copy(alpha = 0.6f),
            radius = width * 0.05f,
            center = Offset(centerX - width * 0.20f, centerY + height * 0.08f)
        )
        drawCircle(
            color = Color(0xFFFF8A80).copy(alpha = 0.6f),
            radius = width * 0.05f,
            center = Offset(centerX + width * 0.20f, centerY + height * 0.08f)
        )

        // 5. Trunk (ആനക്കൊമ്പ്/തുമ്പിക്കൈ)
        drawArc(
            color = Color(0xFFCFD8DC),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(centerX - width * 0.08f, centerY + height * 0.02f),
            size = androidx.compose.ui.geometry.Size(width * 0.16f, height * 0.22f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = width * 0.09f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )

        // 6. Cute small traditional tilak head decoration
        drawCircle(
            color = TraditionalGold,
            radius = width * 0.035f,
            center = Offset(centerX, centerY - height * 0.18f)
        )
    }
}

@Composable
fun TutorInputs(
    viewModel: TutorViewModel,
    onExplainClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ശാസ്ത്രപദം ഇവിടെ എഴുതൂ (Enter Technical Term)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = viewModel.englishTermInput,
                onValueChange = { viewModel.englishTermInput = it },
                placeholder = { Text("ഉദാ: Centripetal Force, Photosynthesis, Gravity...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("english_term_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KeralaGreen,
                    focusedLabelColor = KeralaGreen,
                    cursorColor = KeralaGreen
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "പുസ്തകത്തിലെ വിവരണം (English Definition - Optional)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = viewModel.englishDefinitionInput,
                onValueChange = { viewModel.englishDefinitionInput = it },
                placeholder = { Text("പാഠപുസ്തകത്തിലെ ഡെഫനിഷൻ ഇവിടെ കോപ്പി ചെയ്ത് വെക്കാം (കൂടുതൽ മനസ്സിലാക്കാൻ ഇത് സഹായിക്കും)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("english_definition_input"),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KeralaGreen,
                    focusedLabelColor = KeralaGreen,
                    cursorColor = KeralaGreen
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onExplainClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("explain_button"),
                colors = ButtonDefaults.buttonColors(containerColor = KeralaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "എനിക്ക് ലളിതമായി പറഞ്ഞു തരൂ! 💡",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TraditionalGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = KeralaGreen,
                strokeWidth = 4.dp,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "കൂട്ടുകാരൻ ആലോചിക്കുന്നു... 🐘💭",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = KeralaGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            val thinkingStatus = remember {
                val statuses = listOf(
                    "നമ്മുടെ നാട്ടിലെ ഒരു രസകരമായ ഉദാഹരണം ആലോചിക്കുകയാണ്... 🥥🚌",
                    "നിങ്ങൾക്ക് മനസ്സിലാകുന്ന ഏറ്റവും ലളിതമായ വാക്കുകളിലേക്ക് മാറ്റുന്നു... 📝✨",
                    "കെ.എസ്.ആർ.ടി.സി ബസും ചക്കയും നാളികേരവും ഒക്കെ കൂട്ടിച്ചേർക്കുന്നു... 🌴🚲",
                    "10-ാം ക്ലാസിലെ കൂട്ടുകാർക്കായി പാഠം ലളിതമാക്കുന്നു... 🎓❤️"
                )
                statuses
            }
            var activeStatusIndex by remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(3500)
                    activeStatusIndex = (activeStatusIndex + 1) % thinkingStatus.size
                }
            }

            AnimatedContent(
                targetState = activeStatusIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "StatusText"
            ) { index ->
                Text(
                    text = thinkingStatus[index],
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEF5350))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "തകരാറ്",
                    tint = Color(0xFFC62828)
                )
                Text(
                    text = "ശ്രദ്ധിക്കൂ കൂട്ടുകാരാ! (Alert!)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 13.sp,
                color = Color(0xFFB71C1C),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ശരി (Ok)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TutorExplanationCard(
    state: TutorUiState.Success,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkToggled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, TraditionalGold)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row (Term and Bookmark Star)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "വീണ്ടും തിരയുക (Search Again)",
                        tint = KeralaGreen
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.englishTerm,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = KeralaGreen,
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(
                    onClick = onBookmarkToggled,
                    modifier = Modifier.testTag("bookmark_active_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Bookmark Star",
                        tint = if (isBookmarked) TraditionalGoldDark else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Textbook definition (optional)
            if (state.englishDefinition.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Textbook Definition / വിവരണം:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.englishDefinition,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Section 1: എന്താണ് സംഗതി? (Simplified general meaning)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(KeralaGreenLight, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "എന്താണ് സംഗതി?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = KeralaGreen
                )
            }

            Text(
                text = state.malayalamExplanation,
                fontSize = 15.sp,
                color = CharcoalNavy,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 12.dp)
            )

            // Section 2: നമ്മുടെ നാട്ടിലെ ഉദാഹരണം (Local analogy card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, KsrtcOrange.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)), // Light warm orange
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚌", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "നമ്മുടെ നാടൻ ഉദാഹരണം",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KsrtcOrange
                            )
                            Text(
                                text = state.analogyTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE65100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.analogyDetails,
                        fontSize = 14.sp,
                        color = CharcoalNavy,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Section 3: Speech bubble of cheerleader
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(KeralaGreenLight, shape = CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MascotDrawCanvas()
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Speech bubble
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "കൂട്ടുകാരൻ AI 🎓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KeralaGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.tutorEncouragement,
                            fontSize = 13.sp,
                            color = CharcoalNavy,
                            lineHeight = 18.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Back selection button
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KeralaGreen),
                border = BorderStroke(1.dp, KeralaGreen)
            ) {
                Text(
                    text = "മറ്റൊരു വാക്ക് പഠിക്കാം! (Learn custom term)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SearchHistoryQuickCard(
    item: HistoryEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = KeralaGreen,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.englishTerm,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.analogyTitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = TraditionalGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun HistoryNotebookTab(
    history: List<HistoryEntity>,
    onSelectItem: (HistoryEntity) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onToggleBookmark: (Int, Boolean) -> Unit
) {
    var filterByBookmark by remember { mutableStateOf(false) }

    val displayedList = if (filterByBookmark) {
        history.filter { it.isBookmarked }
    } else {
        history
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "എന്റെ പഠനക്കുറിപ്പുകൾ (Saved Lessons) 📚",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = KeralaGreen
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !filterByBookmark,
                onClick = { filterByBookmark = false },
                label = { Text("എല്ലാം (${history.size})", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KeralaGreenLight,
                    selectedLabelColor = KeralaGreen
                )
            )

            FilterChip(
                selected = filterByBookmark,
                onClick = { filterByBookmark = true },
                label = { Text("പ്രിയപ്പെട്ടവ (${history.count { it.isBookmarked }})", fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFF8E1),
                    selectedLabelColor = TraditionalGoldDark
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TraditionalGoldDark
                    )
                }
            )
        }

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "📖",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (filterByBookmark) "പ്രിയപ്പെട്ട പാഠങ്ങളൊന്നും ഇതുവരെ അടയാളപ്പെടുത്തിയിട്ടില്ല!" else "പഠനക്കുറിപ്പുകൾ ഒന്നും ഇവിടെയില്ല! സയൻസ് പദങ്ങൾ തിരയുമ്പോൾ അവ ഇവിടെ കാണാം.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(displayedList) { item ->
                    HistoryNotebookItemCard(
                        item = item,
                        onClick = { onSelectItem(item) },
                        onDelete = { onDeleteItem(item.id) },
                        onBookmarkToggled = { onToggleBookmark(item.id, item.isBookmarked) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryNotebookItemCard(
    item: HistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onBookmarkToggled: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(1.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(KeralaGreenLight, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚌", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.englishTerm,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = KeralaGreen
                        )
                        Text(
                            text = item.analogyTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBookmarkToggled) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Bookmark",
                            tint = if (item.isBookmarked) TraditionalGoldDark else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFC62828).copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.malayalamExplanation,
                fontSize = 13.sp,
                color = CharcoalNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

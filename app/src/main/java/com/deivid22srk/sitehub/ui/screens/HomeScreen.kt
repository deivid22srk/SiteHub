package com.deivid22srk.sitehub.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.deivid22srk.sitehub.SiteHubApp
import com.deivid22srk.sitehub.data.model.SiteEntity
import com.deivid22srk.sitehub.ui.components.ImageCropper
import com.deivid22srk.sitehub.util.FaviconFetcher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToWebView: (Long, String, String) -> Unit,
    onNavigateToUserscripts: (Long, String, String) -> Unit
) {
    val app = LocalContext.current.applicationContext as SiteHubApp
    val sites by app.repository.getAllSites().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<SiteEntity?>(null) }
    var urlInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var showEditTitleDialog by remember { mutableStateOf<SiteEntity?>(null) }
    var showIconDialog by remember { mutableStateOf<SiteEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<SiteEntity?>(null) }
    var showShareDialog by remember { mutableStateOf<SiteEntity?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("SiteHub") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add site")
            }
        }
    ) { padding ->
        if (sites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Nenhum site adicionado",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Toque em + para adicionar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 88.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sites, key = { it.id }) { site ->
                    SiteGridItem(
                        site = site,
                        onClick = { onNavigateToWebView(site.id, site.url, site.title) },
                        onLongClick = { selectedSite = site }
                    )
                }
            }
        }
    }

    selectedSite?.let { site ->
        ModalBottomSheet(
            onDismissRequest = { selectedSite = null },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(site.faviconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(site.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            site.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SheetItem(
                    icon = { Icon(Icons.Default.OpenInBrowser, contentDescription = null) },
                    text = "Abrir site",
                    onClick = {
                        selectedSite = null
                        onNavigateToWebView(site.id, site.url, site.title)
                    }
                )

                SheetItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = "Editar título",
                    onClick = {
                        selectedSite = null
                        showEditTitleDialog = site
                    }
                )

                SheetItem(
                    icon = { Icon(Icons.Default.Image, contentDescription = null) },
                    text = "Ícone personalizado",
                    onClick = {
                        selectedSite = null
                        showIconDialog = site
                    }
                )

                SheetItem(
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    text = "Userscripts",
                    onClick = {
                        selectedSite = null
                        onNavigateToUserscripts(site.id, site.url, site.title)
                    }
                )

                SheetItem(
                    icon = { Icon(Icons.Default.Link, contentDescription = null) },
                    text = if (site.sharedGroupId > 0) "Compartilhando sessão" else "Compartilhar sessão",
                    onClick = {
                        selectedSite = null
                        showShareDialog = site
                    }
                )

                SheetItem(
                    icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    text = "Remover",
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        selectedSite = null
                        showDeleteDialog = site
                    }
                )
            }
        }
    }

    AnimatedVisibility(
        visible = showAddDialog,
        enter = fadeIn() + scaleIn(spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut() + scaleOut()
    ) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; urlInput = "" },
            title = { Text("Adicionar site") },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://exemplo.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                val normalizedUrl = if (!urlInput.startsWith("http")) "https://$urlInput" else urlInput
                                val title = FaviconFetcher.fetchTitle(normalizedUrl)
                                val favicon = FaviconFetcher.fetchBestFavicon(normalizedUrl)
                                app.repository.addSite(
                                    SiteEntity(url = normalizedUrl, title = title, faviconUrl = favicon)
                                )
                                isLoading = false
                                showAddDialog = false
                                urlInput = ""
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Carregando..." else "Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; urlInput = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    showEditTitleDialog?.let { site ->
        var newTitle by remember { mutableStateOf(site.title) }
        AlertDialog(
            onDismissRequest = { showEditTitleDialog = null },
            title = { Text("Editar título") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            scope.launch { app.repository.updateSite(site.copy(title = newTitle.trim())) }
                        }
                        showEditTitleDialog = null
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    showIconDialog?.let { site ->
        var iconUrl by remember { mutableStateOf("") }
        var cropSource by remember { mutableStateOf<Any?>(null) }

        val iconFilePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { cropSource = it }
        }

        AlertDialog(
            onDismissRequest = { showIconDialog = null; cropSource = null },
            title = { Text("Ícone personalizado") },
            text = {
                if (cropSource != null) {
                    ImageCropper(
                        imageSource = cropSource!!,
                        onCropComplete = { path ->
                            scope.launch { app.repository.updateSite(site.copy(faviconUrl = path)) }
                            showIconDialog = null
                            cropSource = null
                        },
                        onCancel = { cropSource = null }
                    )
                } else {
                    Column {
                        OutlinedTextField(
                            value = iconUrl,
                            onValueChange = { iconUrl = it },
                            label = { Text("URL da imagem") },
                            placeholder = { Text("https://exemplo.com/icon.png") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            TextButton(
                                onClick = { iconFilePicker.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Escolher imagem")
                            }
                            TextButton(
                                onClick = {
                                    if (iconUrl.isNotBlank()) cropSource = iconUrl.trim()
                                },
                                modifier = Modifier.weight(1f),
                                enabled = iconUrl.isNotBlank()
                            ) {
                                Text("Recortar URL")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (cropSource == null) {
                    TextButton(onClick = { showIconDialog = null }) {
                        Text("Fechar")
                    }
                }
            }
        )
    }

    showDeleteDialog?.let { site ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remover site") },
            text = { Text("Remover \"${site.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { app.repository.deleteSite(site) }
                        showDeleteDialog = null
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    showShareDialog?.let { site ->
        val otherSites = sites.filter { it.id != site.id }
        AlertDialog(
            onDismissRequest = { showShareDialog = null },
            title = { Text("Compartilhar sessão") },
            text = {
                Column {
                    Text(
                        "Compartilhe cookies e localStorage entre sites. Útil quando URLs diferentes levam ao mesmo serviço.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (site.sharedGroupId > 0) {
                        TextButton(
                            onClick = {
                                scope.launch { app.repository.unshareSession(site.id) }
                                showShareDialog = null
                            }
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Desvincular sessão")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (otherSites.isEmpty()) {
                        Text(
                            "Adicione mais sites para compartilhar.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        otherSites.forEach { other ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(onClick = {
                                        scope.launch { app.repository.shareSession(site.id, other.id) }
                                        showShareDialog = null
                                    })
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                AsyncImage(
                                    model = other.faviconUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(other.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        other.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (other.sharedGroupId > 0 && other.sharedGroupId == site.sharedGroupId) {
                                    Icon(
                                        Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showShareDialog = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SheetItem(
    icon: @Composable () -> Unit,
    text: String,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = textColor)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteGridItem(
    site: SiteEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .size(64.dp)
                .aspectRatio(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(site.faviconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = site.title,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = site.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

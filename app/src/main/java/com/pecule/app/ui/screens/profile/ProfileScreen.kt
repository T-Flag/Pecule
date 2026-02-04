package com.pecule.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.ui.components.NewSalaryDialog
import com.pecule.app.ui.components.NewSalaryState
import com.pecule.app.ui.components.NewSalaryViewModel
import com.pecule.app.ui.theme.PeculeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    newSalaryViewModel: NewSalaryViewModel = hiltViewModel()
) {
    val firstName by viewModel.firstName.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val newSalaryState by newSalaryViewModel.state.collectAsStateWithLifecycle()

    var editedFirstName by remember(firstName) { mutableStateOf(firstName) }
    var showNewSalaryDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val isFirstNameChanged = editedFirstName != firstName && editedFirstName.isNotBlank()

    LaunchedEffect(newSalaryState) {
        when (newSalaryState) {
            is NewSalaryState.Success -> {
                showNewSalaryDialog = false
                snackbarHostState.showSnackbar("Nouveau cycle créé avec succès")
                newSalaryViewModel.resetState()
            }
            is NewSalaryState.Error -> {
                snackbarHostState.showSnackbar((newSalaryState as NewSalaryState.Error).message)
                newSalaryViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        ProfileContent(
            firstName = editedFirstName,
            onFirstNameChange = { editedFirstName = it },
            isFirstNameChanged = isFirstNameChanged,
            onSaveFirstName = {
                viewModel.updateFirstName(editedFirstName)
            },
            selectedTheme = theme,
            onThemeChange = { viewModel.updateTheme(it) },
            onNewSalaryClick = { showNewSalaryDialog = true },
            onCategoriesClick = onNavigateToCategories,
            modifier = Modifier.padding(paddingValues)
        )
    }

    if (showNewSalaryDialog) {
        NewSalaryDialog(
            onDismiss = { showNewSalaryDialog = false },
            onConfirm = { amount, date ->
                newSalaryViewModel.createNewCycle(amount, date)
            }
        )
    }
}

@Composable
private fun ProfileContent(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    isFirstNameChanged: Boolean,
    onSaveFirstName: () -> Unit,
    selectedTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    onNewSalaryClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section Prénom
        Text(
            text = "Prénom",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("Votre prénom") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onSaveFirstName,
                enabled = isFirstNameChanged
            ) {
                Text("Enregistrer")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Nouveau salaire
        Text(
            text = "Nouveau cycle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ajoutez un nouveau salaire pour démarrer un nouveau cycle budgétaire. Les charges fixes seront automatiquement reportées.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNewSalaryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Nouveau salaire")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Catégories
        Text(
            text = "Catégories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Personnalisez les catégories de dépenses selon vos besoins.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCategoriesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Gérer les catégories")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section Thème
        Text(
            text = "Thème",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemeSelector(
            selectedTheme = selectedTheme,
            onThemeChange = onThemeChange
        )
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeOptions = listOf(
        ThemePreference.AUTO to "Automatique",
        ThemePreference.LIGHT to "Clair",
        ThemePreference.DARK to "Sombre"
    )

    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        themeOptions.forEach { (theme, label) ->
            ThemeOptionCard(
                label = label,
                isSelected = selectedTheme == theme,
                onClick = { onThemeChange(theme) }
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    PeculeTheme {
        ProfileContent(
            firstName = "Marie",
            onFirstNameChange = {},
            isFirstNameChanged = false,
            onSaveFirstName = {},
            selectedTheme = ThemePreference.AUTO,
            onThemeChange = {},
            onNewSalaryClick = {},
            onCategoriesClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentChangedPreview() {
    PeculeTheme {
        ProfileContent(
            firstName = "Sophie",
            onFirstNameChange = {},
            isFirstNameChanged = true,
            onSaveFirstName = {},
            selectedTheme = ThemePreference.DARK,
            onThemeChange = {},
            onNewSalaryClick = {},
            onCategoriesClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileContentDarkPreview() {
    PeculeTheme(darkTheme = true) {
        ProfileContent(
            firstName = "Pierre",
            onFirstNameChange = {},
            isFirstNameChanged = false,
            onSaveFirstName = {},
            selectedTheme = ThemePreference.DARK,
            onThemeChange = {},
            onNewSalaryClick = {},
            onCategoriesClick = {}
        )
    }
}

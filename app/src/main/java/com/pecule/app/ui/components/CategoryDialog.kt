package com.pecule.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.ui.theme.PeculeTheme

// Available icons for categories
val CATEGORY_ICONS = listOf(
    "restaurant" to Icons.Filled.Restaurant,
    "directions_car" to Icons.Filled.DirectionsCar,
    "home" to Icons.Filled.Home,
    "receipt_long" to Icons.Filled.Receipt,
    "sports_esports" to Icons.Filled.SportsEsports,
    "medical_services" to Icons.Filled.MedicalServices,
    "shopping_bag" to Icons.Filled.ShoppingBag,
    "payments" to Icons.Filled.Payments,
    "local_grocery_store" to Icons.Filled.LocalGroceryStore,
    "local_cafe" to Icons.Filled.LocalCafe,
    "flight" to Icons.Filled.Flight,
    "fitness_center" to Icons.Filled.FitnessCenter,
    "school" to Icons.Filled.School,
    "pets" to Icons.Filled.Pets,
    "child_care" to Icons.Filled.ChildCare,
    "phone" to Icons.Filled.Phone,
    "wifi" to Icons.Filled.Wifi,
    "celebration" to Icons.Filled.Celebration,
    "build" to Icons.Filled.Build,
    "attach_money" to Icons.Filled.AttachMoney,
    "more_horiz" to Icons.Filled.MoreHoriz
)

// Available colors for categories
val CATEGORY_COLORS = listOf(
    0xFF4CAF50L, // Vert
    0xFF2196F3L, // Bleu
    0xFFFF9800L, // Orange
    0xFF9C27B0L, // Violet
    0xFFE91E63L, // Rose
    0xFF00BCD4L, // Cyan
    0xFFFF5722L, // Orange foncé
    0xFFFFEB3BL, // Jaune
    0xFF795548L, // Marron
    0xFF607D8BL, // Gris bleuté
    0xFF3F51B5L, // Indigo
    0xFF009688L, // Teal
    0xFFF44336L, // Rouge
    0xFF673AB7L, // Violet foncé
    0xFF8BC34AL  // Vert clair
)

fun getIconForName(iconName: String): ImageVector {
    return CATEGORY_ICONS.find { it.first == iconName }?.second ?: Icons.Filled.MoreHoriz
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDialog(
    categoryToEdit: CategoryEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, color: Long) -> Unit
) {
    val isEditMode = categoryToEdit != null

    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(categoryToEdit?.icon ?: CATEGORY_ICONS.first().first) }
    var selectedColor by remember { mutableLongStateOf(categoryToEdit?.color ?: CATEGORY_COLORS.first()) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "Modifier la catégorie" else "Nouvelle catégorie",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Color picker
                Text(
                    text = "Couleur",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORY_COLORS.forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Icon picker
                Text(
                    text = "Icône",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORY_ICONS.forEach { (iconName, icon) ->
                        IconOption(
                            icon = icon,
                            isSelected = iconName == selectedIcon,
                            selectedColor = Color(selectedColor),
                            onClick = { selectedIcon = iconName }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedIcon, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditMode) "Modifier" else "Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ColorOption(
    color: Long,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color))
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Sélectionné",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun IconOption(
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, selectedColor, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
private fun CategoryDialogCreatePreview() {
    PeculeTheme {
        CategoryDialog(
            categoryToEdit = null,
            onDismiss = {},
            onConfirm = { _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun CategoryDialogEditPreview() {
    PeculeTheme {
        CategoryDialog(
            categoryToEdit = CategoryEntity(
                id = 10,
                name = "Vacances",
                icon = "flight",
                color = 0xFF2196F3L,
                isDefault = false
            ),
            onDismiss = {},
            onConfirm = { _, _, _ -> }
        )
    }
}

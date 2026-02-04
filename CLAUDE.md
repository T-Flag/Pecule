# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pecule is a budget and expense tracking Android application built with Jetpack Compose and Clean Architecture.

## Build Commands

```bash
# Build the application
./gradlew build

# Run unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests ConvertersTest

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Build debug APK
./gradlew assembleDebug

# Clean build
./gradlew clean
```

## Architecture

The project follows **Clean Architecture** with three layers:

### Data Layer (`data/`)
- **Room Database** (`local/database/`): SQLite persistence with entities (BudgetCycle, Expense, Income), DAOs, and type converters
- **DataStore** (`local/datastore/`): User preferences storage (theme, first name)
- **Repositories** (`repository/`): Abstract data access, expose Flows for reactive updates

### Domain Layer (`domain/`)
- Business models and logic
- `CategoryEntity` - Database entity for customizable categories
- `BalanceCalculator.kt` - Calculs de solde et pourcentage consommé
- `Transaction.kt` - Data class pour l'affichage unifié dépenses/revenus
- `CycleManager.kt` - Création de cycles et duplication des charges fixes
- `CategoryInitializer.kt` - Initialisation des 9 catégories par défaut
- `CsvExporter.kt` - Génération de fichiers CSV
- `PdfExporter.kt` - Génération de rapports PDF A4
- `ExportManager.kt` - Gestion de la création de fichiers et partage Android

### UI Layer (`ui/`)
- **Jetpack Compose** screens in `screens/` subdirectories
- **Material 3** with custom Soft Mint color palette
- Reusable components in `components/`
- Navigation setup in `navigation/`

## Key Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.0.21 | Language |
| Compose BOM | 2024.09.00 | UI framework |
| Room | 2.6.1 | Database |
| Hilt | 2.52 | Dependency injection |
| DataStore | 1.1.2 | Preferences |
| Coroutines | 1.9.0 | Async/reactive |

## Dependency Injection

Hilt is configured with:
- `PeculeApplication` annotated with `@HiltAndroidApp`
- `DatabaseModule` provides Room database and DAOs
- `DataStoreModule` provides UserPreferencesDataStore
- `MainActivity` annotated with `@AndroidEntryPoint`

## Repository Interfaces

Les repositories utilisent des interfaces pour la testabilité :
- `IUserPreferencesRepository` → `UserPreferencesRepository`
- `IBudgetCycleRepository` → `BudgetCycleRepository`
- `IExpenseRepository` → `ExpenseRepository`
- `IIncomeRepository` → `IncomeRepository`
- `ICategoryRepository` → `CategoryRepository`

Les bindings Hilt sont dans `di/RepositoryModule.kt`.

## Database Schema

Four main entities with foreign key relationships:
- **BudgetCycle**: Budget periods with amount and date range
- **Expense**: Linked to cycle and category via `categoryId`, fixed or variable
- **Income**: Linked to cycle, fixed or variable
- **CategoryEntity**: Categories with name, icon, color, and isDefault flag

Type converters handle `LocalDate` (epoch days).

## Version Catalog

Dependencies are managed in `gradle/libs.versions.toml`. Add new dependencies there rather than hardcoding versions in build.gradle.kts.

## Testing

Unit tests are in `app/src/test/java/com/pecule/app/`:

| Fichier | Tests | Description |
|---------|-------|-------------|
| `ConvertersTest.kt` | 3 | Room type converters (LocalDate) |
| `UserPreferencesTest.kt` | 4 | DataStore models |
| `BudgetCycleRepositoryTest.kt` | 10 | Repository CRUD |
| `CategoryRepositoryTest.kt` | 7 | Category CRUD and protection |
| `OnboardingViewModelTest.kt` | 6 | Onboarding logic |
| `OnboardingValidationTest.kt` | 18 | Form validation |
| `BalanceCalculatorTest.kt` | 6 | Balance calculations |
| `CycleManagerTest.kt` | 10 | Cycle creation and duplication |
| `DashboardViewModelTest.kt` | 8 | Dashboard logic |
| `TransactionDialogValidationTest.kt` | 16 | Transaction form validation |
| `AddTransactionViewModelTest.kt` | 23 | Add/edit transaction logic |
| `DeleteConfirmationTest.kt` | 4 | Delete confirmation logic |
| `BudgetViewModelTest.kt` | 12 | Budget screen logic |
| `BudgetSwipeDeleteTest.kt` | 5 | Swipe to delete transactions |
| `BudgetSwipeEditTest.kt` | 6 | Swipe to edit transactions |
| `ProfileViewModelTest.kt` | 9 | Profile screen logic |
| `CategoriesViewModelTest.kt` | 11 | Categories CRUD and validation |
| `StatisticsViewModelTest.kt` | 11 | Statistics screen logic |
| `CsvExporterTest.kt` | 10 | CSV export format |
| `PdfExporterTest.kt` | 8 | PDF export data preparation |

**Total : 187 tests**

Use JUnit 4 assertions. For coroutines, use `kotlinx-coroutines-test` with `runTest`.
Fake repositories are in test directories for mocking.

## Business Logic

### Budget Cycles
- A cycle starts on payday and ends the day before the next payday
- `endDate = null` means the cycle is currently open
- Balance formula: `BudgetCycle.amount + Σ(Income) - Σ(Expense)`

### Fixed vs Variable
- **Fixed expenses** (`isFixed = true`): Rent, subscriptions - duplicated automatically to each new cycle
- **Variable expenses** (`isFixed = false`): Groceries, outings - one-time entries
- **Fixed incomes** (`isFixed = true`): Pension, rental income - duplicated automatically
- **Variable incomes** (`isFixed = false`): Bonuses, sales - one-time entries

When a new salary is added:
1. Close current cycle (set `endDate` to day before new salary date)
2. Create new cycle with new salary
3. Duplicate all fixed expenses and incomes from previous cycle to new cycle

### Categories (French labels)
Default categories stored in `CategoryEntity` table:
- Salaire (id=1, icon=AttachMoney)
- Alimentation (id=2, icon=Restaurant)
- Transport (id=3, icon=DirectionsCar)
- Logement (id=4, icon=Home)
- Factures (id=5, icon=Receipt)
- Loisirs (id=6, icon=SportsEsports)
- Santé (id=7, icon=LocalHospital)
- Shopping (id=8, icon=ShoppingCart)
- Autre (id=9, icon=Category)

Categories are initialized by `CategoryInitializer.kt` on first app launch.

## UI Theme "Soft Mint"

| Token | Light | Dark |
|-------|-------|------|
| Primary | #80CBC4 | #80CBC4 |
| Background | #F5F7F8 | #121212 |
| Surface | #FFFFFF | #1E1E1E |
| Text Primary | #00695C | #B2DFDB |

- Card corner radius: 24.dp
- Button corner radius: 16.dp

## Code Conventions

- Use `suspend` for write operations (insert, update, delete)
- Use `Flow<>` for read operations (reactive updates)
- Never hardcode colors - use `MaterialTheme.colorScheme`
- Composables should have parameters for testability and previews
- French UI labels, English code

## UI Components (`ui/components/`)

| Component | Description |
|-----------|-------------|
| `SemiCircularGauge` | Canvas gauge showing percentage (0-100%) |
| `BalanceCard` | Card with balance, gauge, and percentage text |
| `TransactionItem` | List item for expense/income display |
| `SwipeableTransactionItem` | TransactionItem avec swipe left (supprimer) et right (modifier) |
| `AddTransactionDialog` | Universal dialog for create/edit transactions |
| `DeleteConfirmationDialog` | Dialog de confirmation avant suppression |
| `DonutChart` | Canvas donut chart for category breakdown |
| `NewSalaryDialog` | Dialog for creating new budget cycle |
| `CategoryDialog` | Dialog for create/edit categories with color/icon picker |
| `ExportDialog` | Dialog de choix format export (CSV/PDF) |
| `ShimmerEffect` | Animation shimmer réutilisable pour les états de chargement |
| `LoadingPlaceholders` | Placeholders shimmer (BalanceCard, TransactionItem, DonutChart) |

## Screens (`ui/screens/`)

| Screen | Features |
|--------|----------|
| Dashboard | Balance card, gauge, recent transactions, FAB |
| Budget | 3 tabs, transaction lists, totals, edit/delete, FAB |
| Statistics | Donut chart, cycle selector, category legend, summary |
| Profile | Edit name, theme selector, new salary button, manage categories |
| Categories | Category list, swipe edit/delete, FAB for new, color/icon picker |

## Theme System

Theme preference is stored in DataStore and observed in MainActivity.
- `ThemePreference.AUTO` → follows system setting
- `ThemePreference.LIGHT` → always light
- `ThemePreference.DARK` → always dark

PeculeTheme accepts `themePreference` parameter and applies the correct color scheme.

## Current State

### Completed ✅
- Project setup (Compose, Material 3, Hilt, Room, DataStore)
- Database entities (BudgetCycle, Expense, Income, Category)
- Repository interfaces and implementations
- Navigation with BottomNavBar
- Onboarding dialog with DatePicker
- Dashboard: BalanceCard, SemiCircularGauge, TransactionItem, FAB
- AddTransactionDialog (create/edit expenses and incomes)
- Budget screen: 3 tabs, totals, edit/delete
- Profile screen: edit name, theme selector, new salary button
- Statistics screen: DonutChart, cycle selector, category legend, summary
- CycleManager: create new cycle, auto-duplicate fixed expenses/incomes
- CategoryEntity migration: categories stored in Room database
- Custom categories UI: create, edit, delete with color/icon picker
- Loading states with shimmer placeholders on all screens
- Export CSV and PDF with format chooser dialog
- 187 unit tests passing

### App Features Complete 🎉
- Full budget cycle management
- Expense tracking (fixed & variable)
- Income tracking (fixed & variable)
- Visual statistics with donut chart
- Theme customization (Auto/Light/Dark)
- Data persistence with Room & DataStore
- CSV and PDF export

## CI/CD

GitHub Actions workflow in `.github/workflows/android-ci.yml`:
- Triggers on push and PR to main
- Runs all unit tests with `./gradlew test`
- Uploads test results as artifacts

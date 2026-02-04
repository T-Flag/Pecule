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
- Category enum with French labels and Material icons
- `BalanceCalculator.kt` - Calculs de solde et pourcentage consommé
- `Transaction.kt` - Data class pour l'affichage unifié dépenses/revenus
- `CycleManager.kt` - Création de cycles et duplication des charges fixes

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

Les bindings Hilt sont dans `di/RepositoryModule.kt`.

## Database Schema

Three main entities with foreign key relationships:
- **BudgetCycle**: Budget periods with amount and date range
- **Expense**: Linked to cycle, categorized, fixed or variable
- **Income**: Linked to cycle, fixed or variable

Type converters handle `LocalDate` (epoch days) and `Category` enum (string).

## Version Catalog

Dependencies are managed in `gradle/libs.versions.toml`. Add new dependencies there rather than hardcoding versions in build.gradle.kts.

## Testing

Unit tests are in `app/src/test/java/com/pecule/app/`:

| Fichier | Tests | Description |
|---------|-------|-------------|
| `ConvertersTest.kt` | 7 | Room type converters |
| `UserPreferencesTest.kt` | 4 | DataStore models |
| `BudgetCycleRepositoryTest.kt` | 10 | Repository CRUD |
| `OnboardingViewModelTest.kt` | 6 | Onboarding logic |
| `OnboardingValidationTest.kt` | 18 | Form validation |
| `BalanceCalculatorTest.kt` | 6 | Balance calculations |
| `DashboardViewModelTest.kt` | 6 | Dashboard logic |
| `TransactionDialogValidationTest.kt` | 17 | Transaction form validation |
| `AddTransactionViewModelTest.kt` | 22 | Add/edit transaction logic |
| `BudgetViewModelTest.kt` | 10 | Budget screen logic |
| `ProfileViewModelTest.kt` | 9 | Profile screen logic |
| `StatisticsViewModelTest.kt` | 9 | Statistics screen logic |
| `CycleManagerTest.kt` | 10 | Cycle creation and duplication |

**Total : 135 tests**

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

### Category Enum (French labels)
- SALARY: "Salaire"
- FOOD: "Alimentation"
- TRANSPORT: "Transport"
- HOUSING: "Logement"
- UTILITIES: "Factures"
- ENTERTAINMENT: "Loisirs"
- HEALTH: "Santé"
- SHOPPING: "Shopping"
- OTHER: "Autre"

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
| `AddTransactionDialog` | Universal dialog for create/edit transactions |
| `DonutChart` | Canvas donut chart for category breakdown |
| `NewSalaryDialog` | Dialog for creating new budget cycle |

## Screens (`ui/screens/`)

| Screen | Features |
|--------|----------|
| Dashboard | Balance card, gauge, recent transactions, FAB |
| Budget | 3 tabs, transaction lists, totals, edit/delete, FAB |
| Statistics | Donut chart, cycle selector, category legend, summary |
| Profile | Edit name, theme selector, new salary button |

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
- 135 unit tests passing

### App Features Complete 🎉
- Full budget cycle management
- Expense tracking (fixed & variable)
- Income tracking (fixed & variable)
- Visual statistics with donut chart
- Theme customization (Auto/Light/Dark)
- Data persistence with Room & DataStore

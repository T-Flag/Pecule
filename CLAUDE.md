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
- `ConvertersTest.kt` - Room type converters
- `UserPreferencesTest.kt` - DataStore models

Use JUnit 4 assertions. Room DAOs can be tested with in-memory database.

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

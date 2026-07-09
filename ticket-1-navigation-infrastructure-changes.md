# ticket-1/navigation-infrastructure — Branch Changes

## Overview
Add multi-screen navigation infrastructure (NavHost + bottom nav bar) with 3 screens: Chat, Scanner, Medicine Data. Also cleans up Phases-workdone docs and graphify-out artifacts.

## New Files

### `app/src/main/java/com/smartphoneaichat/ui/navigation/Screen.kt`
- Sealed class with 3 screens: `Chat`, `Scanner`, `MedicineData`
- Each has `route`, `label`, `icon` (Material Icons)

### `app/src/main/java/com/smartphoneaichat/ui/components/BottomNavigationBar.kt`
- `@Composable` bottom nav bar using Material 3 `NavigationBar`
- Iterates `Screen.all`, highlights selected route
- Dark theme colors

### `app/src/main/java/com/smartphoneaichat/App.kt`
- `Application` subclass with `appContainer` lazy delegate

## Modified Files

### `app/build.gradle.kts`
- Add `kotlin-parcelize` plugin
- Add `androidx.navigation:navigation-compose:2.7.7` dependency

### `app/src/main/AndroidManifest.xml`
- Add `android:name=".App"` to `<application>` tag

### `app/src/main/java/com/smartphoneaichat/MainActivity.kt`
- Rewrite: `NavHost` + `BottomNavigationBar` in a `Scaffold`
- NavController with `currentBackStackEntryAsState` for route tracking
- 3 composable destinations: Chat, Scanner, MedicineData
- `PlaceholderScreen` composable for Scanner/MedicineData
- `App` class lookup instead of raw `Application` cast

### `app/src/main/java/com/smartphoneaichat/domain/model/value/ConversationId.kt`
- Add `@Parcelize` and `Parcelable` implementation

### `app/src/main/java/com/smartphoneaichat/domain/model/value/MessageId.kt`
- Add `@Parcelize` and `Parcelable` implementation

### `app/src/main/java/com/smartphoneaichat/ui/components/Sidebar.kt`
- Lazy column key: `it.id` → `it.id.value`

### `app/src/main/java/com/smartphoneaichat/ui/screens/ChatScreen.kt`
- Lazy column key: `it.id` → `it.id.value`

## Deleted Files
- `Phases-workdone/` directory (7 docs, ~4.5k lines)
- `CODE_REVIEW.md`

## .gitignore Changes
- Add `graphify-out/` entries

## How to Replicate
1. Create the 3 new files above
2. Apply the 8 file modifications
3. Delete `Phases-workdone/` and `CODE_REVIEW.md`
4. Update `.gitignore`
This Jetpack Compose application is a simple fitness planner that allows users to browse available workouts, schedule their training sessions, and add personal notes with date and time details.  
The app consists of three main screens.

## Screen 1 
#### Workout List
- Displays a scrollable list of workout types using LazyColumn and Cards,
- Each card includes an image, title, and description,
- Clicking on a workout navigates to the details screen (Screen 2).

## Screen 2
#### Shows the workout image, title, and description
- Allows the user to:
  - Select a date using a `DatePickerDialog`,
  - Choose start and end times from dropdown menus,
  - Write a note about the workout and save it with the selected date and time,
- Saved notes are stored in the ViewModel, it helps to survive configuration changes,
- Includes a back button to return to the list.

## Screen 3
####  Notes & Search
- Displays all saved notes grouped by workout,
- Implements a search bar for filtering workouts and notes,
- Includes a Floating Action Button to add a new note:
  - Opens a dialog with a dropdown for workout selection and a text field for the note,
  - Notes are stored instantly via ViewModel.

## Extra Features

- Floating Action Button used for quick note creation on Screen 3,
- Dynamic dropdown menus for selecting time slots,
- Scrollable layout on Screen 2 for better usability on smaller screens,
- Material 3 Cards and consistent spacing for a clean, modern interface, 
- Toasts for user feedback when notes are saved,
- Date formatting in `dd.MM.yyyy` format for clarity.

## Lifecycle-Aware Components

- All user data (notes, selections, and UI states) are managed in a `ScoreViewModel`,
- These states are automatically preserved across recompositions and configuration changes,

## Effect Handlers

- `LaunchedEffect` is used in Screen 3 to filter notes dynamically whenever the search query or notes list changesand log search query updates for debugging or analytics,
- Ensures UI reacts instantly to state changes without unnecessary recompositions.


## Search Implementation

- The search bar on Screen 3 updates `searchQuery` in the ViewModel.  
- `LaunchedEffect` matches both workout titles and note content and displays only relevant results in the LazyColumn.

## Floating Action Button

- FAB on Screen 3 lets the user choose a workout from a dropdown list, add a custom note.
- Notes are saved to the ViewModel and immediately visible in the list below.

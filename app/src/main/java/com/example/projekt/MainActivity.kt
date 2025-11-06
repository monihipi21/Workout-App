package com.example.projekt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt.ui.theme.ProjektTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                MyApp()
        }
    }
}


class ScoreViewModel : ViewModel() {

    // Dane dla wszytskich ekranow
    // Notatki dla każdego workoutu klucz = tytuł, wartość = lista notatek
    private val _notes = MutableStateFlow<Map<String, MutableList<String>>>(emptyMap())
    val notes: StateFlow<Map<String, MutableList<String>>> = _notes.asStateFlow()

    fun saveNoteForItem(title: String, note: String) {
        if (note.isBlank()) return
        _notes.update { currentMap ->
            currentMap.toMutableMap().apply {
                val noteList = this[title] ?: mutableListOf()
                noteList.add(note)
                this[title] = noteList
            }
        }
    }


    // Screen1


    // Screen2
    val noteText = MutableStateFlow("") // tresc notatki
    val selectedDate = MutableStateFlow("") // data z kalendarzem

    val startTime = MutableStateFlow("")
    val endTime = MutableStateFlow("")


    fun getLastNoteForItem(title: String): String {
        return _notes.value[title]?.lastOrNull() ?: ""
    }


    // Screen3
    val searchQuery = MutableStateFlow("")  // Pole wyszukiwania
    val selectedWorkout = MutableStateFlow(sampleItems.first().title)  // Wybrany workout
    val newNoteText = MutableStateFlow("")  // Tekst nowej notatki
    val selectedFilterCategory = MutableStateFlow("All") // do filtrowania

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }


}


data class ListItem(
    val title: String,
    val imageRes: Int? = null,
    val description: Int?= null
)


@Composable
fun MyApp() {
    val navController = rememberNavController()
    val scoreViewModel: ScoreViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) } // dolny pasek
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "screen1",
            modifier = Modifier.padding(innerPadding) // żeby zawartość nie zasłaniała paska
        ) {
            composable("screen1") {
                Screen1(scoreViewModel, navController)
            }
            composable(
                "screen2/{itemTitle}",
                arguments = listOf(navArgument("itemTitle") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemTitle = backStackEntry.arguments?.getString("itemTitle") ?: ""
                Screen2(scoreViewModel, navController, itemTitle)
            }
            composable("screen3") {
                Screen3(scoreViewModel, navController)
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        // dwa parametry
        Pair("Items", "screen1"),
        Pair("Notes", "screen3")
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        // tworzymy przyciski dla każdej pozycji
        items.forEach { (title, route) ->
            NavigationBarItem(
                icon = {
                    if (route == "screen1")
                        Icon(Icons.Filled.Home, contentDescription = title)
                    else
                        Icon(Icons.Filled.Settings, contentDescription = title)
                },
                // Etykieta pod ikoną
                label = { Text(title) },
                selected = currentRoute == route,
                onClick = {
                    // jak nie jesteśmy już na tym ekranie, nawigujemy
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // usuwa z back stacku wszystko do "screen1"
                            popUpTo("screen1") { inclusive = false }
                            // zapobiega tworzeniu duplikatu ekranu
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun Screen1(viewModel: ScoreViewModel, navController: NavHostController) {

    // lista przewijalna
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // odstępy między elementami listy

    ) {

        // dla każdego elementu z listy sampleItems tworzony jest Card
        items(sampleItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth() // cała szerokość ekranu
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp) // cien pod karta
            ) {
                // wnetrze karty
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        // Po kliknięciu na kartę przechodzimy do Screen2
                        //     Przekazując tytuł elementu w  ścieżki
                        .clickable {
                            navController.navigate("screen2/${item.title}")},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    // obrazek
                    item.imageRes?.let { image ->
                        Image(
                            painter = painterResource(id = image),
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(220.dp),
                            contentScale = ContentScale.Crop // przycięcie żeby zdjęcie wypełniało ramkę
                        )
                    }
                    // Odstęp między zdjęciem a tytułem
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium

                    )
                }
            }
        }
    }

}


val sampleItems = listOf(
    ListItem("Aerobics Class", R.drawable.aerobicsclass, description = R.string.desc_aerobics),
    ListItem("Body Pump", R.drawable.bodypump, description = R.string.desc_bodypump),
    ListItem("Crossfit", R.drawable.crossfit, description = R.string.desc_crossfit),
    ListItem("Cycling Indoor", R.drawable.cyclingindoor, description = R.string.desc_cycling),
    ListItem("Kickboxing", R.drawable.kickboxing, description = R.string.desc_kickboxing),
    ListItem("Pilates", R.drawable.pilates, description = R.string.desc_pilates),
    ListItem("Rope Jumping", R.drawable.ropejumping, description = R.string.desc_ropejumping),
    ListItem("Strength Training", R.drawable.strengthtraining, description = R.string.desc_strengthtraining),
    ListItem("Yoga", R.drawable.yoga, description = R.string.desc_yoga),
    ListItem("Zumba", R.drawable.zumba, description = R.string.desc_zumba)
)



@Composable
fun Screen2(viewModel: ScoreViewModel, navController: NavHostController, itemTitle: String) {
    // Znajdujemy obiekt ListItem odpowiadający tytułowi przekazanemu w nawigacji
    val item = sampleItems.find { it.title == itemTitle }
    val context = LocalContext.current

    // stany z ViewModelu – notatkę, wybraną datę, godziny rozpoczęcia i zakończenia
    val note by viewModel.noteText.collectAsState(initial = viewModel.getLastNoteForItem(itemTitle))
    val selectedDate by viewModel.selectedDate.collectAsState()
    val startTime by viewModel.startTime.collectAsState()
    val endTime by viewModel.endTime.collectAsState()

    // Kalendarz DatePicker
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            // formatowanie daty
            val formattedDate = "%02d.%02d.%d".format(selectedDay, selectedMonth + 1, selectedYear)
            viewModel.selectedDate.value = formattedDate
        },
        year, month, day
    )

    // Lista godzin co 30 minut
    val timeOptions = remember {
        val times = mutableListOf<String>()
        for (h in 6..22) {
            times.add("%02d:00".format(h))
            times.add("%02d:30".format(h))
        }
        times
    }

    // Stany do rozwijania dropdownów
    var expandedStart by remember { mutableStateOf(false) }
    var expandedEnd by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState() // zeby dalo sie przewijac

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Zdjęcie
        item?.imageRes?.let { image ->
            Image(
                painter = painterResource(id = image),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tytuł i opis
        item?.let {
            Text(text = it.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            it.description?.let { descRes ->
                Text(text = stringResource(id = descRes))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk do wyboru daty treningu
        Button(onClick = { datePickerDialog.show() }) {
            Text(if (selectedDate.isEmpty()) "Select Date" else "Date: $selectedDate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Godzina rozpoczęcia
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Start Time", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = { expandedStart = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (startTime.isEmpty()) "Select start time" else startTime)
            }

            // Lista godzin startowych
            DropdownMenu(
                expanded = expandedStart,
                onDismissRequest = { expandedStart = false }
            ) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            viewModel.startTime.value = time
                            expandedStart = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Godzina zakończenia
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("End Time", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = { expandedEnd = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (endTime.isEmpty()) "Select end time" else endTime)
            }

            // Lista godzin koncowych
            DropdownMenu(
                expanded = expandedEnd,
                onDismissRequest = { expandedEnd = false }
            ) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            viewModel.endTime.value = time
                            expandedEnd = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // notatka
        TextField(
            value = note,
            onValueChange = { viewModel.noteText.value = it },
            label = { Text("Enter your note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Zapis notatki z data i godzinami
        Button(onClick = {
            val details = buildString {
                if (selectedDate.isNotEmpty()) append("Date: $selectedDate")
                if (startTime.isNotEmpty()) append(" | Start: $startTime")
                if (endTime.isNotEmpty()) append(" | End: $endTime")
            }
            val noteTextWithDetails = if (details.isNotEmpty())
                "${viewModel.noteText.value} ($details)"
            else
                viewModel.noteText.value

            viewModel.saveNoteForItem(itemTitle, noteTextWithDetails)
            Toast.makeText(context, "Note saved!", Toast.LENGTH_SHORT).show()

            // Reset pól po zapisaniu
            viewModel.noteText.value = ""
            viewModel.selectedDate.value = ""
            viewModel.startTime.value = ""
            viewModel.endTime.value = ""
        }) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk do powrotu
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

@Composable
fun Screen3(viewModel: ScoreViewModel, navController: NavHostController) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilterCategory by viewModel.selectedFilterCategory.collectAsState()

    var filteredNotes by remember { mutableStateOf(notes) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // filtrowanie po kategorii i wyszukiwaniu
    LaunchedEffect(notes, searchQuery, selectedFilterCategory) {
        filteredNotes = notes.filter { (title, noteList) ->
            (selectedFilterCategory == "All" || title == selectedFilterCategory) &&
                    (title.contains(searchQuery, ignoreCase = true) ||
                            noteList.any { it.contains(searchQuery, ignoreCase = true) })
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("My Notes", style = MaterialTheme.typography.headlineMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { filterMenuExpanded = true }) {
                    Text(selectedFilterCategory)
                }
                DropdownMenu(
                    expanded = filterMenuExpanded,
                    onDismissRequest = { filterMenuExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All") }, onClick = {
                        viewModel.selectedFilterCategory.value = "All"
                        filterMenuExpanded = false
                    })
                    sampleItems.forEach { item ->
                        DropdownMenuItem(text = { Text(item.title) }, onClick = {
                            viewModel.selectedFilterCategory.value = item.title
                            filterMenuExpanded = false
                        })
                    }
                }
                OutlinedButton(onClick = { viewModel.selectedFilterCategory.value = "All" }) {
                    Text("Reset")
                }
            }

            // Pole wyszukiwania
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search notes") },
                modifier = Modifier.fillMaxWidth()
            )

            // Lista notatek
            LazyColumn(
                modifier = Modifier.animateContentSize() // animacja przy zmianie wysokości
            ) {
                if (filteredNotes.isEmpty()) {
                    item {
                        Text("No notes found.", modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    items(filteredNotes.toList()) { (title, noteList) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)

                        ) {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                            noteList.forEach { note ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(note, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog dodawania nowej notatki
        if (showDialog) {
            var expanded by remember { mutableStateOf(false) }
            val selectedWorkout by viewModel.selectedWorkout.collectAsState()
            val newNoteText by viewModel.newNoteText.collectAsState()

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New Note") },
                text = {
                    Column {
                        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Selected workout: $selectedWorkout")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            sampleItems.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    onClick = {
                                        viewModel.selectedWorkout.value = item.title
                                        expanded = false
                                    }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newNoteText,
                            onValueChange = { viewModel.newNoteText.value = it },
                            label = { Text("Enter new note") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newNoteText.isNotBlank()) {
                            viewModel.saveNoteForItem(selectedWorkout, newNoteText)
                            viewModel.newNoteText.value = ""
                        }
                        showDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

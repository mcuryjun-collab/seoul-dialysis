package com.seoul.dialysis

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.seoul.dialysis.data.AppDatabase
import com.seoul.dialysis.data.Hospital
import com.seoul.dialysis.data.HospitalRepository
import com.seoul.dialysis.ui.*
import com.seoul.dialysis.ui.theme.SeoulDialysisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "dialysis.db").build()
        val hiraKey = ""
        val repo = HospitalRepository(this, db, hiraKey.ifBlank { null })
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DialysisViewModel(repo, applicationContext) as T
            }
        }
        val permLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE))
        setContent {
            SeoulDialysisTheme {
                val nav = rememberNavController()
                var selected by remember { mutableStateOf<Hospital?>(null) }
                var bottomTab by remember { mutableStateOf(0) }
                val viewModel: DialysisViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                val state by viewModel.state.collectAsState()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(selected = bottomTab == 0, onClick = { bottomTab = 0; nav.navigate("list") { launchSingleTop = true } }, icon = { Icon(Icons.Default.List, null) }, label = { Text("리스트") })
                            NavigationBarItem(selected = bottomTab == 1, onClick = { bottomTab = 1; nav.navigate("map") { launchSingleTop = true } }, icon = { Icon(Icons.Default.Map, null) }, label = { Text("지도") })
                            NavigationBarItem(selected = bottomTab == 2, onClick = { bottomTab = 2; nav.navigate("fav") { launchSingleTop = true } }, icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("즐겨찾기 ${state.favorites.size}") })
                        }
                    }
                ) { pad ->
                    NavHost(navController = nav, startDestination = "list", modifier = Modifier.padding(pad)) {
                        composable("list") { DialysisScreen(viewModel) { h -> selected = h; nav.navigate("detail") } }
                        composable("map") { MapScreen(hospitals = state.filtered) { h -> selected = h; nav.navigate("detail") } }
                        composable("fav") { FavoritesScreen(all = state.all, favorites = state.favorites, onToggle = { viewModel.toggleFavorite(it) }) { h -> selected = h; nav.navigate("detail") } }
                        composable("detail") {
                            selected?.let { h ->
                                val isFav = state.favorites.contains(h.id)
                                HospitalDetailScreen(hospital = h, isFav = isFav, onToggleFav = { viewModel.toggleFavorite(h.id) }) { nav.popBackStack() }
                            }
                        }
                    }
                }
            }
        }
    }
}

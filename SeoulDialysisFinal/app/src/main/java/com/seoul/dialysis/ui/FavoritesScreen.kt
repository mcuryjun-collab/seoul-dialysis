package com.seoul.dialysis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seoul.dialysis.data.Hospital

@Composable
fun FavoritesScreen(all: List<Hospital>, favorites: Set<Int>, onToggle: (Int)->Unit, onDetail: (Hospital)->Unit){
    val favHospitals = all.filter { favorites.contains(it.id) }
    if(favHospitals.isEmpty()){
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)){
                Icon(Icons.Default.Favorite, null, modifier = Modifier.size(48.dp))
                Text("즐겨찾기한 병원이 없습니다.")
                Text("리스트에서 하트 버튼을 눌러 저장하세요.", style = MaterialTheme.typography.bodySmall)
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)){
            items(favHospitals){ h ->
                HospitalCard(h, true, null, onClick = { onDetail(h) }, onFav = { onToggle(h.id) }, onCall = {}, onMap = {})
            }
        }
    }
}

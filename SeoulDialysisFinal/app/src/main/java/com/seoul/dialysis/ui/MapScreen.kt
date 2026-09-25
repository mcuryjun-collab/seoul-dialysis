package com.seoul.dialysis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.seoul.dialysis.data.Hospital

@Composable
fun MapScreen(hospitals: List<Hospital>, onHospitalClick: (Hospital) -> Unit){
    val seoul = LatLng(37.5665, 126.9780)
    val cameraPositionState = rememberCameraPositionState{ position = CameraPosition.fromLatLngZoom(seoul, 11f) }
    var selected by remember { mutableStateOf<Hospital?>(null) }

    Box(Modifier.fillMaxSize()){
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState){
            hospitals.forEach { h ->
                Marker(state = MarkerState(position = LatLng(h.lat, h.lng)), title = h.name, snippet = "${h.group.label} / ${h.grade}",
                    onClick = { selected = h; false })
            }
        }
        Card(Modifier.padding(16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha=0.92f))){
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)){
                Text("지도 · 마커 색상 = HDF/야간 그룹", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    LegendDot(Color(0xFF2E7D32), "둘 다 ${hospitals.count { it.hdf && it.night }}")
                    LegendDot(Color(0xFF0B6E99), "HDF ${hospitals.count { it.hdf && !it.night }}")
                    LegendDot(Color(0xFF6A4C93), "야간 ${hospitals.count { it.night && !it.hdf }}")
                    LegendDot(Color.Gray, "일반 ${hospitals.count { !it.hdf && !it.night }}")
                }
            }
        }
        selected?.let { h ->
            Card(Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)){
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)){
                    Text(h.name, style = MaterialTheme.typography.titleMedium)
                    Text("${h.gu} · ${h.address}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
                        Badge(containerColor = if(h.hdf) Color(0xFF0B6E99) else Color.Gray){ Text(if(h.hdf)"HDF" else "HDF X", color = Color.White) }
                        Badge(containerColor = if(h.night) Color(0xFF6A4C93) else Color.Gray){ Text(if(h.night)"야간" else "주간", color = Color.White) }
                        Badge{ Text(h.grade) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        Button(onClick = { onHospitalClick(h) }, modifier = Modifier.weight(1f)){ Text("상세보기") }
                        OutlinedButton(onClick = { selected = null }, modifier = Modifier.weight(1f)){ Text("닫기") }
                    }
                }
            }
        }
    }
}
@Composable fun LegendDot(color: Color, text: String){
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)){
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(5.dp)))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

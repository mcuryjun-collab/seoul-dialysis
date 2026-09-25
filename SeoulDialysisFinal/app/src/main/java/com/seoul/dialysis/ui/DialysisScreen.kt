package com.seoul.dialysis.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seoul.dialysis.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialysisScreen(vm: DialysisViewModel, onDetail: (Hospital) -> Unit){
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("서울 투석병원 ${state.filtered.size}곳", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.toggleFavOnly() }){
                        Icon(if(state.showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                    }
                    IconButton(onClick = { vm.toggleSortDistance() }){
                        Icon(if(state.sortByDistance) Icons.Default.NearMe else Icons.Default.LocationOn, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White, actionIconContentColor = Color.White)
            )
        }
    ){ pad ->
        Column(Modifier.padding(pad).fillMaxSize()){
            OutlinedTextField(
                value = state.query, onValueChange = vm::onQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("병원명, 구, 주소 검색") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, shape = RoundedCornerShape(12.dp)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                items(FilterType.values()){ f ->
                    val sel = state.filter==f
                    FilterChip(
                        selected = sel,
                        onClick = { vm.onFilter(f) },
                        label = { Text(when(f){
                            FilterType.ALL -> "전체 ${state.all.size}"
                            FilterType.HDF -> "HDF 가능"
                            FilterType.NIGHT -> "야간투석"
                            FilterType.BOTH -> "직장인 모드 (둘 다)"
                        }) },
                        leadingIcon = if(sel){ { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                items(vm.guList){ gu ->
                    val sel = state.gu==gu
                    AssistChip(onClick = { vm.onGu(gu) }, label = { Text(gu) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = if(sel) MaterialTheme.colorScheme.primaryContainer else Color.White))
                }
            }
            if(state.myLat!=null){
                Text("내 위치 기준 거리순 정렬 중",
                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            if(state.hiraCount!=null){
                Text("HIRA 실데이터 1등급 ${state.hiraCount}곳 확인됨",
                    Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
            }
            Row(Modifier.fillMaxWidth().padding(16.dp,4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)){
                StatCard("HDF 가능", state.all.count { it.hdf }, Color(0xFF0B6E99), Modifier.weight(1f))
                StatCard("야간 가능", state.all.count { it.night }, Color(0xFF6A4C93), Modifier.weight(1f))
                StatCard("둘 다", state.all.count { it.hdf && it.night }, Color(0xFF2E7D32), Modifier.weight(1f))
            }
            if(state.isLoading){
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){ CircularProgressIndicator() }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)){
                    state.grouped.forEach { (group, list) ->
                        if(list.isNotEmpty()){
                            item{ GroupHeader(group, list.size) }
                            items(list){ h ->
                                val isFav = state.favorites.contains(h.id)
                                val dist = if(state.myLat!=null && state.myLng!=null) h.distanceTo(state.myLat!!, state.myLng!!) else null
                                HospitalCard(h, isFav, dist,
                                    onClick = { onDetail(h) },
                                    onFav = { vm.toggleFavorite(h.id) },
                                    onCall = { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${h.phone}"))) },
                                    onMap = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(h.address)}"))) }
                                )
                            }
                        }
                    }
                    if(state.filtered.isEmpty()){
                        item{ Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center){ Text("조건에 맞는 병원이 없습니다.") } }
                    }
                }
            }
        }
    }
}

@Composable fun GroupHeader(group: HospitalGroup, count: Int){
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(
        when(group){ HospitalGroup.BOTH->Color(0xFFE8F5E9); HospitalGroup.HDF_ONLY->Color(0xFFE3F2FD); HospitalGroup.NIGHT_ONLY->Color(0xFFF3E5F5); HospitalGroup.GENERAL->Color(0xFFF5F5F5)}
    ).padding(12.dp,8.dp), verticalAlignment = Alignment.CenterVertically){
        Icon(when(group){ HospitalGroup.BOTH->Icons.Default.Star; HospitalGroup.HDF_ONLY->Icons.Default.WaterDrop; HospitalGroup.NIGHT_ONLY->Icons.Default.NightsStay; HospitalGroup.GENERAL->Icons.Default.LocalHospital}, null, Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column{ Text("${group.label} · ${count}곳", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall); Text(group.desc, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
    }
}
@Composable fun StatCard(label: String, count: Int, color: Color, modifier: Modifier){
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha=0.1f))){
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally){
            Text("$count", fontWeight = FontWeight.Bold, color=color, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
@Composable fun HospitalCard(h: Hospital, isFav: Boolean, dist: Double?, onClick:()->Unit, onFav:()->Unit, onCall:()->Unit, onMap:()->Unit){
    Card(Modifier.fillMaxWidth().clickable{ onClick() }, elevation = CardDefaults.cardElevation(2.dp)){
        Column(Modifier.padding(16.dp)){
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                Text(h.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Row{
                    IconButton(onClick = onFav, modifier = Modifier.size(24.dp)){ Icon(if(isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if(isFav) Color.Red else Color.Gray) }
                    Spacer(Modifier.width(4.dp))
                    Badge(containerColor = if(h.grade=="1등급") Color(0xFF2E7D32) else Color.Gray){ Text(h.grade, color = Color.White) }
                }
            }
            if(dist!=null){ Text(String.format("%.1f km · %s · %s", dist, h.gu, h.address), style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            else { Text("${h.gu} · ${h.address}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)){
                TagChip(if(h.hdf)"HDF 가능" else "HDF 불가", h.hdf, Icons.Default.WaterDrop)
                TagChip(if(h.night)"야간투석" else "주간만", h.night, Icons.Default.NightsStay)
                if(h.emergency) TagChip("응급", true, Icons.Default.Warning, Color(0xFFD32F2F))
            }
            Spacer(Modifier.height(8.dp))
            Text("침상 ${h.beds} · 전문의 ${h.doctors}명 · ${h.hours}", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                OutlinedButton(onClick = onCall, modifier = Modifier.weight(1f)){ Icon(Icons.Default.Call, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("전화") }
                Button(onClick = onMap, modifier = Modifier.weight(1f)){ Icon(Icons.Default.Map, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("지도") }
            }
        }
    }
}
@Composable fun TagChip(text: String, active: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = Color(0xFF0B6E99)){
    Row(Modifier.clip(RoundedCornerShape(6.dp)).background(if(active) color.copy(0.12f) else Color(0xFFEEEEEE)).padding(8.dp,4.dp), verticalAlignment = Alignment.CenterVertically){
        Icon(icon, null, Modifier.size(12.dp), tint = if(active) color else Color.Gray)
        Spacer(Modifier.width(3.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = if(active) color else Color.Gray, fontWeight = if(active) FontWeight.Bold else FontWeight.Normal)
    }
}

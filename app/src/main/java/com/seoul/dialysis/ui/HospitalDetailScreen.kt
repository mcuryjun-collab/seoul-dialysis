package com.seoul.dialysis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seoul.dialysis.data.Hospital

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDetailScreen(hospital: Hospital, isFav: Boolean = false, onToggleFav: (() -> Unit)? = null, onBack: () -> Unit){
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hospital.name) },
                navigationIcon = { IconButton(onClick = onBack){ Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if(onToggleFav!=null){
                        IconButton(onClick = onToggleFav){ Icon(if(isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if(isFav) Color.Red else Color.Gray) }
                    }
                }
            )
        }
    ){ padding ->
        Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)){
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)){
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)){
                    Text("그루핑 분류", style = MaterialTheme.typography.labelMedium)
                    Text(hospital.group.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(hospital.group.desc)
                    Divider()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                        AssistChip(onClick = {}, label = { Text(if(hospital.hdf) "HDF O" else "HDF X") })
                        AssistChip(onClick = {}, label = { Text(if(hospital.night) "야간 O" else "야간 X") })
                    }
                }
            }
            DetailRow("주소", hospital.address)
            DetailRow("자치구", hospital.gu)
            DetailRow("전화", hospital.phone)
            DetailRow("등급", hospital.grade)
            DetailRow("운영시간", hospital.hours)
            DetailRow("침상", "${hospital.beds} bed")
            DetailRow("전문의", "${hospital.doctors}명")
            DetailRow("응급", if(hospital.emergency) "가능" else "불가")
            DetailRow("좌표", "${hospital.lat}, ${hospital.lng}")
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))){
                Column(Modifier.padding(16.dp)){
                    Text("HDF란?", fontWeight = FontWeight.Bold)
                    Text("혈액투석여과(Hemodiafiltration)는 중분자 요독물질 제거가 우수해 투석 중 저혈압, 가려움, 장기 합병증 감소에 도움", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("야간투석이란?", fontWeight = FontWeight.Bold)
                    Text("직장인을 위해 저녁 6시~11시 운영. 이 앱에서 직장인 모드 = HDF+야간 둘 다 가능한 곳만 필터링", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("HIRA 연동 방법", fontWeight = FontWeight.Bold)
                    Text("data.go.kr에서 병원평가정보서비스 키 발급 후 HospitalRepository 생성자에 serviceKey 넣으면 1등급 실데이터로 교차검증 가능", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
@Composable fun DetailRow(label: String, value: String){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(start = 16.dp))
    }
    Divider(color = Color(0xFFEEEEEE))
}

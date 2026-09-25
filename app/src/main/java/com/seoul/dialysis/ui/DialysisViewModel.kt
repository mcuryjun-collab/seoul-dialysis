package com.seoul.dialysis.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seoul.dialysis.data.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context

data class DialysisUiState(
    val all: List<Hospital> = emptyList(),
    val filtered: List<Hospital> = emptyList(),
    val grouped: Map<HospitalGroup, List<Hospital>> = emptyMap(),
    val query: String = "",
    val filter: FilterType = FilterType.ALL,
    val gu: String = "전체",
    val favorites: Set<Int> = emptySet(),
    val showOnlyFavorites: Boolean = false,
    val sortByDistance: Boolean = false,
    val myLat: Double? = null,
    val myLng: Double? = null,
    val isLoading: Boolean = true,
    val hiraCount: Int? = null
)

class DialysisViewModel(
    private val repo: HospitalRepository,
    private val appContext: Context
) : ViewModel() {
    private val _state = MutableStateFlow(DialysisUiState())
    val state: StateFlow<DialysisUiState> = _state.asStateFlow()

    val guList = listOf("전체","강남구","강동구","강북구","강서구","관악구","광진구","구로구","금천구","노원구","도봉구","동대문구","동작구","마포구","서대문구","서초구","성동구","성북구","송파구","양천구","영등포구","용산구","은평구","종로구","중구","중랑구")

    init {
        viewModelScope.launch {
            val list = repo.loadHospitals()
            val fav = repo.getFavorites()
            _state.update { it.copy(all = list, favorites = fav) }
            applyFilter()
            val hira = repo.fetchHiraDialysisGrade1()
            if (hira.isNotEmpty()) {
                _state.update { it.copy(hiraCount = hira.size) }
            }
        }
    }

    fun onQuery(q: String){ _state.update { it.copy(query = q) }; applyFilter() }
    fun onFilter(f: FilterType){ _state.update { it.copy(filter = f) }; applyFilter() }
    fun onGu(g: String){ _state.update { it.copy(gu = g) }; applyFilter() }
    fun toggleFavOnly(){ _state.update { it.copy(showOnlyFavorites = !it.showOnlyFavorites) }; applyFilter() }
    fun toggleSortDistance(){ 
        if (_state.value.myLat==null) { requestLocation() } 
        _state.update { it.copy(sortByDistance = !it.sortByDistance) }; applyFilter() 
    }

    fun requestLocation(){
        viewModelScope.launch {
            try {
                val hasPerm = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!hasPerm) return@launch
                val client = LocationServices.getFusedLocationProviderClient(appContext)
                val loc = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
                loc?.let {
                    _state.update { s -> s.copy(myLat = it.latitude, myLng = it.longitude) }
                    applyFilter()
                }
            } catch (e: Exception){ e.printStackTrace() }
        }
    }

    fun toggleFavorite(id: Int){
        viewModelScope.launch {
            repo.toggleFavorite(id)
            val fav = repo.getFavorites()
            _state.update { it.copy(favorites = fav) }
            applyFilter()
        }
    }

    private fun applyFilter(){
        val s = _state.value
        var filtered = repo.filter(s.all, s.filter, s.query, s.gu, if(s.showOnlyFavorites) s.favorites else null)
        if (s.sortByDistance && s.myLat!=null && s.myLng!=null){
            filtered = filtered.sortedBy { it.distanceTo(s.myLat!!, s.myLng!!) }
        }
        _state.update { it.copy(filtered = filtered, grouped = repo.getGrouped(filtered), isLoading = false) }
    }
}

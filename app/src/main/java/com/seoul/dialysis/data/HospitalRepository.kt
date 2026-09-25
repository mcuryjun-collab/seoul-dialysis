package com.seoul.dialysis.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HospitalRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val hiraServiceKey: String? = null
) {
    private var cached: List<Hospital>? = null
    private val retrofit by lazy {
        Retrofit.Builder().baseUrl(HiraApiService.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    }
    private val hiraApi by lazy { retrofit.create(HiraApiService::class.java) }

    suspend fun loadHospitals(): List<Hospital> = withContext(Dispatchers.IO) {
        cached?.let { return@withContext it }
        val json = context.assets.open("hospitals.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Hospital>>() {}.type
        val list: List<Hospital> = Gson().fromJson(json, type)
        cached = list.sortedBy { it.name }
        list
    }

    suspend fun fetchHiraDialysisGrade1(): List<HiraItem> = withContext(Dispatchers.IO) {
        if (hiraServiceKey.isNullOrBlank()) return@withContext emptyList()
        try {
            val res = hiraApi.getDialysisHospitals(serviceKey = hiraServiceKey, numOfRows = 200)
            res.response?.body?.items?.item?.filter { it.asmGrd == "1" } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getGrouped(list: List<Hospital>) = list.groupBy { it.group }.toSortedMap(compareBy { it.ordinal })
    fun filter(list: List<Hospital>, filter: FilterType, query: String, gu: String, onlyFavorites: Set<Int>? = null): List<Hospital> {
        return list.filter { h ->
            val matchFilter = when(filter){
                FilterType.ALL -> true
                FilterType.HDF -> h.hdf
                FilterType.NIGHT -> h.night
                FilterType.BOTH -> h.hdf && h.night
            }
            val matchQuery = query.isBlank() || h.name.contains(query,true) || h.address.contains(query,true) || h.gu.contains(query,true)
            val matchGu = gu=="전체" || h.gu==gu
            val matchFav = onlyFavorites==null || onlyFavorites.contains(h.id)
            matchFilter && matchQuery && matchGu && matchFav
        }
    }
    suspend fun toggleFavorite(id: Int) {
        val dao = db.favoriteDao()
        if (dao.isFavorite(id)) dao.remove(id) else dao.add(FavoriteEntity(id))
    }
    suspend fun getFavorites(): Set<Int> = db.favoriteDao().getAll().map { it.hospitalId }.toSet()
    suspend fun isFavorite(id: Int) = db.favoriteDao().isFavorite(id)
}

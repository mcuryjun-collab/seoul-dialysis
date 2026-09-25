package com.seoul.dialysis.data

import com.google.gson.annotations.SerializedName
import kotlin.math.*

data class Hospital(
    val id: Int,
    val name: String,
    val gu: String,
    val address: String,
    val phone: String,
    val grade: String,
    val beds: Int,
    val doctors: Int,
    @SerializedName("hdf") val hdf: Boolean,
    @SerializedName("night") val night: Boolean,
    val hours: String,
    val emergency: Boolean,
    val lat: Double = 37.5665,
    val lng: Double = 126.9780
) {
    val group: HospitalGroup get() = when {
        hdf && night -> HospitalGroup.BOTH
        hdf && !night -> HospitalGroup.HDF_ONLY
        !hdf && night -> HospitalGroup.NIGHT_ONLY
        else -> HospitalGroup.GENERAL
    }
    fun distanceTo(lat: Double, lng: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat - this.lat)
        val dLng = Math.toRadians(lng - this.lng)
        val a = kotlin.math.sin(dLat/2).pow(2) + kotlin.math.cos(Math.toRadians(this.lat)) * kotlin.math.cos(Math.toRadians(lat)) * kotlin.math.sin(dLng/2).pow(2)
        return R * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1-a))
    }
}

enum class HospitalGroup(val label: String, val desc: String, val colorHex: String) {
    BOTH("HDF + 야간 모두 가능", "직장인·고효율 최적", "#2E7D32"),
    HDF_ONLY("HDF 가능", "고효율 혈액투석여과 가능", "#0B6E99"),
    NIGHT_ONLY("야간투석 가능", "직장인 야간 운영", "#6A4C93"),
    GENERAL("일반 혈액투석", "일반 혈액투석만", "#757575")
}

enum class FilterType(val label: String) {
    ALL("전체"),
    HDF("HDF 가능"),
    NIGHT("야간투석 가능"),
    BOTH("둘 다 가능 (직장인 모드)")
}

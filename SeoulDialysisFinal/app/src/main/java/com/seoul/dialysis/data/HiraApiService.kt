package com.seoul.dialysis.data

import retrofit2.http.GET
import retrofit2.http.Query

data class HiraResponse(val response: HiraBodyWrapper?)
data class HiraBodyWrapper(val body: HiraBody?)
data class HiraBody(val items: HiraItems?, val totalCount: Int?)
data class HiraItems(val item: List<HiraItem>?)
data class HiraItem(
    val ykiho: String?,
    val yadmNm: String?,
    val addr: String?,
    val asmGrd: String?,
    val asmNm: String?
)

interface HiraApiService {
    @GET("getHospAsmRstInfo")
    suspend fun getDialysisHospitals(
        @Query("serviceKey") serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1,
        @Query("asmGubun") asmGubun: String = "03",
        @Query("_type") type: String = "json"
    ): HiraResponse
    companion object {
        const val BASE_URL = "https://apis.data.go.kr/B551182/hospAsmRstInfoService/"
    }
}

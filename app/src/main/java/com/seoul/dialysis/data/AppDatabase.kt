package com.seoul.dialysis.data

import androidx.room.*

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val hospitalId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getAll(): List<FavoriteEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE hospitalId = :id)")
    suspend fun isFavorite(id: Int): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(fav: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE hospitalId = :id")
    suspend fun remove(id: Int)
    @Query("DELETE FROM favorites")
    suspend fun clear()
}

@Database(entities = [FavoriteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}

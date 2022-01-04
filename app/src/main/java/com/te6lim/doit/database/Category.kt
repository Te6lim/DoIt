package com.te6lim.doit.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "Work",

    @ColumnInfo(name = "is_default")
    var isDefault: Boolean = true,

    @ColumnInfo(name = "totalCreated")
    var totalCreated: Int = 0,

    @ColumnInfo(name = "totalFinished")
    var totalFinished: Int = 0,

    @ColumnInfo(name = "totalSuccess")
    var totalSuccess: Int = 0,

    @ColumnInfo(name = "totalFailure")
    var totalFailure: Int = 0,

    @ColumnInfo(name = "lateTodos")
    var lateTodos: Int = 0
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category_table ORDER BY is_default DESC")
    fun getAllLive(): LiveData<List<Category>>

    @Query("SELECT * FROM category_table ORDER BY is_default DESC")
    fun getAll(): List<Category>

    @Insert
    fun insert(category: Category)

    @Query("DELETE FROM category_table WHERE id = :catId")
    fun delete(catId: Int)

    @Query("SELECT * FROM category_table WHERE id = :key LIMIT 1")
    fun get(key: Int): Category?

    @Query("SELECT * FROM category_table WHERE is_default = :value LIMIT 1")
    fun getDefault(value: Boolean = true): Category

    @Update
    fun update(value: Category)
}

@Database(entities = [Category::class], version = 1, exportSchema = false)
abstract class CategoryDb : RoomDatabase() {

    abstract val dao: CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: CategoryDb? = null

        fun getInstance(context: Context): CategoryDb {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, CategoryDb::class.java, "categories"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
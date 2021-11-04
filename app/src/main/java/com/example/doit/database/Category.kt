package com.example.doit.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "is_default")
    var isDefault: Boolean = false
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category_table ORDER BY id DESC")
    fun getAll(): LiveData<List<Category>>

    @Insert
    fun insert(category: Category)

    @Query("DELETE FROM category_table WHERE name = :name")
    fun delete(name: String)

    @Query("SELECT * FROM category_table WHERE id = :key LIMIT 1")
    fun get(key: Int): Category?

    @Query("SELECT * FROM category_table WHERE is_default = :value LIMIT 1")
    fun getDefault(value: Boolean = true): Category
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
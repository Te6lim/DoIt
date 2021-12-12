package com.example.doit.database

import android.content.Context
import androidx.room.*

@Entity(tableName = "summary_table")
data class Summary(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "todosCreated") val todosCreated: Int,
    @ColumnInfo(name = "todosFinished") val todosFinished: Int,
    @ColumnInfo(name = "deadlinesMet") val deadlinesMet: Int,
    @ColumnInfo(name = "deadlinesUnmet") val deadlinesUnmet: Int,
    @ColumnInfo(name = "todosDiscarded") val todosDiscarded: Int,
    @ColumnInfo(name = "mostActiveCategory") val mostActiveCategory: Int,
    @ColumnInfo(name = "mostActiveRatio") val mostActiveRatio: Int,
    @ColumnInfo(name = "leastActiveCategory") val leastActiveCategory: Int,
    @ColumnInfo(name = "leastActiveRatio") val leastActiveRatio: Int,
    @ColumnInfo(name = "mostSuccessfulCategory") val mostSuccessfulCategory: Int,
    @ColumnInfo(name = "mostSuccessfulRatio") val mostSuccessfulRatio: Int,
    @ColumnInfo(name = "leastSuccessfulCategory") val leastSuccessfulCategory: Int,
    @ColumnInfo(name = "leastSuccessfulRatio") val leastSuccessfulRatio: Int
)

@Dao
interface SummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(summary: Summary)

    @Query("Select * from summary_table limit 1")
    fun getSummary(): Summary
}

@Database(entities = [Summary::class], version = 1, exportSchema = false)
abstract class SummaryDatabase : RoomDatabase() {
    abstract val summaryDao: SummaryDao
}

private lateinit var INSTANCE: SummaryDatabase

fun getInstance(context: Context): SummaryDatabase {
    if (!::INSTANCE.isInitialized) {
        synchronized(SummaryDatabase::class.java) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext, SummaryDatabase::class.java,
                "summary"
            ).build()
        }
    }
    return INSTANCE
}
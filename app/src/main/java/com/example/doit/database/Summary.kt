package com.example.doit.database

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "summary_table")
@Parcelize
data class Summary(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "todosCreated") var todosCreated: Int = 0,
    @ColumnInfo(name = "todosFinished") var todosFinished: Int = 0,
    @ColumnInfo(name = "deadlinesMet") var deadlinesMet: Int = 0,
    @ColumnInfo(name = "deadlinesUnmet") var deadlinesUnmet: Int = 0,
    @ColumnInfo(name = "todosDiscarded") var todosDiscarded: Int = 0,
    @ColumnInfo(name = "mostActiveCategory") var mostActiveCategory: Int = -1,
    @ColumnInfo(name = "leastActiveCategory") var leastActiveCategory: Int = -1,
    @ColumnInfo(name = "mostSuccessfulCategory") var mostSuccessfulCategory: Int = -1,
    @ColumnInfo(name = "mostSuccessfulRatio") var mostSuccessfulRatio: Int = 0,
    @ColumnInfo(name = "leastSuccessfulCategory") var leastSuccessfulCategory: Int = -1,
    @ColumnInfo(name = "leastSuccessfulRatio") var leastSuccessfulRatio: Int = 0
) : Parcelable

@Dao
interface SummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(summary: Summary)

    @Query("Select * from summary_table limit 1")
    fun getSummary(): LiveData<Summary>

    @Query("Select * from summary_table where :key = id")
    fun getSummary(key: Long): Summary
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
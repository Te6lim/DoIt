package com.example.doit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Todo::class], version = 1, exportSchema = false)
@TypeConverters(DateConverters::class, TimeConverters::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract val databaseDao: TodoDbDao

    companion object {

        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getInstance(context: Context): TodoDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, TodoDatabase::class.java, "todo_history"
                    )
                        .addTypeConverter(DateConverters())
                        .addTypeConverter(TimeConverters())
                        .fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
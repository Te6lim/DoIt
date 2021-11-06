package com.example.doit.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDbDao {

    @Insert
    fun insert(todo: Todo)

    @Update
    fun update(todo: Todo)

    @Query("SELECT * FROM todo_table WHERE :key = todoId")
    suspend fun get(key: Long): Todo?

    @Query("SELECT * FROM todo_table ORDER BY todoId DESC")
    fun getAll(): LiveData<List<Todo>?>

    @Query("DELETE FROM todo_table WHERE :key = todoId")
    fun delete(key: Long)

    @Query("DELETE FROM todo_table")
    fun clear()
}
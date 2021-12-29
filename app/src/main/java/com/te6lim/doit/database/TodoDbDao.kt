package com.te6lim.doit.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDbDao {

    @Insert
    fun insert(todo: Todo): Long

    @Update
    fun update(todo: Todo)

    @Query("SELECT * FROM todo_table WHERE :key = todoId")
    fun get(key: Long): Todo?

    @Query("SELECT * FROM todo_table ORDER BY deadline_date ASC")
    fun getAllLive(): LiveData<List<Todo>?>

    @Query("SELECT * FROM todo_table ORDER BY deadline_date ASC")
    fun getAll(): List<Todo>?

    @Query("DELETE FROM todo_table WHERE :key = todoId")
    fun delete(key: Long)

    @Query("DELETE FROM todo_table WHERE :catId = categoryId")
    fun clearByCategory(catId: Int)
}
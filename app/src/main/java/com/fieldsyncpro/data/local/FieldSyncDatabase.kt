package com.fieldsyncpro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fieldsyncpro.data.local.dao.TaskDao
import com.fieldsyncpro.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(TaskTypeConverters::class)
abstract class FieldSyncDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "fieldsync_pro.db"
    }
}

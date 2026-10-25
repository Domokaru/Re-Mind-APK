package com.hallauer.remind.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hallauer.remind.data.local.dao.ContactScheduleDao
import com.hallauer.remind.data.local.dao.MessageLogDao
import com.hallauer.remind.data.local.dao.PhraseDao
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.PhraseEntity
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        ContactScheduleEntity::class,
        PhraseEntity::class,
        MessageLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RemindDatabase : RoomDatabase() {

    abstract fun contactScheduleDao(): ContactScheduleDao
    abstract fun phraseDao(): PhraseDao
    abstract fun messageLogDao(): MessageLogDao

    companion object {
        @Volatile
        private var INSTANCE: RemindDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope? = null): RemindDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): RemindDatabase {
            return try {
                Room.databaseBuilder(
                    context.applicationContext,
                    RemindDatabase::class.java,
                    "remind_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
            } catch (t: Throwable) {
                android.util.Log.e("RemindDatabase", "Database creation failure. Clearing stale database file.", t)
                try {
                    context.applicationContext.deleteDatabase("remind_database.db")
                } catch (_: Throwable) {}
                Room.databaseBuilder(
                    context.applicationContext,
                    RemindDatabase::class.java,
                    "remind_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
            }
        }
    }
}

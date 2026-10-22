package com.hallauer.remind.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hallauer.remind.core.constants.SeedPhrases
import com.hallauer.remind.data.local.dao.ContactScheduleDao
import com.hallauer.remind.data.local.dao.MessageLogDao
import com.hallauer.remind.data.local.dao.PhraseDao
import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.MessageLogEntity
import com.hallauer.remind.data.local.entity.PhraseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ContactScheduleEntity::class,
        PhraseEntity::class,
        MessageLogEntity::class
    ],
    version = 1,
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

        fun getInstance(context: Context, scope: CoroutineScope): RemindDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RemindDatabase::class.java,
                    "remind_database.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate 50 initial seed phrases in the background
                            scope.launch(Dispatchers.IO) {
                                val database = getInstance(context, scope)
                                val initialPhrases = SeedPhrases.ALL_50_PHRASES.map { seed ->
                                    PhraseEntity(
                                        bucket = seed.bucket,
                                        text = seed.text,
                                        isSeed = true
                                    )
                                }
                                database.phraseDao().insertPhrases(initialPhrases)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

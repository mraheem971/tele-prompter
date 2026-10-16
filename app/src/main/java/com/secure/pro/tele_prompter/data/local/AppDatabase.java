package com.secure.pro.tele_prompter.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.secure.pro.tele_prompter.data.local.dao.ScriptDao;
import com.secure.pro.tele_prompter.data.local.entity.ScriptEntity;
import com.secure.pro.tele_prompter.data.model.ScriptTemplate;
import com.secure.pro.tele_prompter.utils.TextMetricsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ScriptEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ScriptDao scriptDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "teleprompter_database"
                    )
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                ScriptDao dao = INSTANCE.scriptDao();
                                List<ScriptTemplate> templates = ScriptTemplate.getDefaultTemplates();
                                List<ScriptEntity> initialScripts = new ArrayList<>();
                                for (ScriptTemplate t : templates) {
                                    ScriptEntity entity = new ScriptEntity(t.getTitle(), t.getContent(), t.getCategory());
                                    int words = TextMetricsHelper.calculateWordCount(t.getContent());
                                    entity.setWordCount(words);
                                    entity.setEstReadingTimeSeconds(TextMetricsHelper.calculateReadingTimeSeconds(words));
                                    initialScripts.add(entity);
                                }
                                dao.insertAll(initialScripts);
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

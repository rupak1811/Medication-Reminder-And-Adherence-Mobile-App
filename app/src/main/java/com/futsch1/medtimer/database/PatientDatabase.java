package com.futsch1.medtimer.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Patient.class}, version = 1)
public abstract class PatientDatabase extends RoomDatabase {
    private static volatile PatientDatabase INSTANCE;

    public abstract PatientDao PatientDao();

    public static PatientDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PatientDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    PatientDatabase.class, "patient_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

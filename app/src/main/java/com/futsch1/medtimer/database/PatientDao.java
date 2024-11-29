package com.futsch1.medtimer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.futsch1.medtimer.database.Patient;
import java.util.List;

@Dao
public interface PatientDao {
    @Insert
    void insert(Patient patient);

    @Query("SELECT * FROM Patients")
    List<Patient> getAllPatients();
    @Query("SELECT * FROM Patients WHERE phone = :phone AND password = :password LIMIT 1")
    Patient getPatient(String phone, String password);
}


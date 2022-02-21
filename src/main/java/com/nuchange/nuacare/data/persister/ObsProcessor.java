package com.nuchange.nuacare.data.persister;

public interface ObsProcessor {
    void migrateForm(Integer conceptId, String path);

    void migrateProgramForm(Integer conceptId, String path);

    void migrateForms(String path, String folder);
}

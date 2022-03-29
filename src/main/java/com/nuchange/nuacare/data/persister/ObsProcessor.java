package com.nuchange.nuacare.data.persister;

public interface ObsProcessor {
    void migrateForm(String conceptUuid, String path);

    void migrateProgramForm(String conceptUuid, String path);

    void migrateForms(String path, String folder);
}

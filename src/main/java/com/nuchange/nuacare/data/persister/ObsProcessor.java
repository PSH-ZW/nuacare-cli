package com.nuchange.nuacare.data.persister;

public interface ObsProcessor {
    void migrateForm(Integer conceptId, String path);
}

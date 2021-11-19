package com.nuchange.nuacare.data.persister.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class FormConcept {
    private String name;
    private String uuid;
    private Boolean addMore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getAddMore() {
        return addMore;
    }

    public void setAddMore(Boolean addMore) {
        this.addMore = addMore;
    }
}

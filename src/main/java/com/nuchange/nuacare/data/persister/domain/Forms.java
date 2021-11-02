package com.nuchange.nuacare.data.persister.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Forms {
    private String name;
    private String uuid;
    List<FormControl> controls;

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

    public List<FormControl> getControls() {
        return controls;
    }

    public void setControls(List<FormControl> controls) {
        this.controls = controls;
    }
}

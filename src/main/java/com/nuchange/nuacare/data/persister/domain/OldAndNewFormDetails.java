package com.nuchange.nuacare.data.persister.domain;

import com.nuchange.psiutil.model.FormLabel;

public class OldAndNewFormDetails {
    FormLabel oldFormLabel;
    FormLabel newFormLabel;
    Integer highestVersion;

    public FormLabel getOldFormLabel() {
        return oldFormLabel;
    }

    public void setOldFormLabel(FormLabel oldFormLabel) {
        this.oldFormLabel = oldFormLabel;
    }

    public FormLabel getNewFormLabel() {
        return newFormLabel;
    }

    public void setNewFormLabel(FormLabel newFormLabel) {
        this.newFormLabel = newFormLabel;
    }

    public Integer getHighestVersion() {
        return highestVersion;
    }

    public void setHighestVersion(Integer highestVersion) {
        this.highestVersion = highestVersion;
    }
}

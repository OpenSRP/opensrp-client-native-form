package com.vijay.jsonwizard.domain;

import androidx.annotation.Nullable;

public class MultiSelectItem {
    private String key;
    private String text;
    private String value;
    private String openmrsEntity;
    private String openmrsEntityId;
    private String openmrsEntityParent;

    public MultiSelectItem() {
    }

    public MultiSelectItem(String key, String text, String value, String openmrsEntity, String openmrsEntityId, String openmrsEntityParent) {
        this.key = key;
        this.text = text;
        this.value = value;
        this.openmrsEntity = openmrsEntity;
        this.openmrsEntityId = openmrsEntityId;
        this.openmrsEntityParent = openmrsEntityParent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOpenmrsEntity() {
        return openmrsEntity;
    }

    public void setOpenmrsEntity(String openmrsEntity) {
        this.openmrsEntity = openmrsEntity;
    }

    public String getOpenmrsEntityId() {
        return openmrsEntityId;
    }

    public void setOpenmrsEntityId(String openmrsEntityId) {
        this.openmrsEntityId = openmrsEntityId;
    }

    public String getOpenmrsEntityParent() {
        return openmrsEntityParent;
    }

    public void setOpenmrsEntityParent(String openmrsEntityParent) {
        this.openmrsEntityParent = openmrsEntityParent;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null){
            return false;
        }
        String key = ((MultiSelectItem)obj).getKey();
        return this.getKey().equals(key);
    }
}

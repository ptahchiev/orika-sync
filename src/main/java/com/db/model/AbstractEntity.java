package com.db.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy =  GenerationType.TABLE)
    private Long pk;
    
    @Column(name = "id", nullable = false)
    private String uid = java.util.UUID.randomUUID().toString();
    
    /* getters/setters */

    public Long getPk() {
        return pk;
    }

    public void setPk(final Long pk) {
        this.pk = pk;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }
    
    
}

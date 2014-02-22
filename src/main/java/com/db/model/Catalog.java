package com.db.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = Catalog.NAME)
@Table(name = Catalog.NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "pk" }), @UniqueConstraint(columnNames = { "id" }) })
public class Catalog extends AbstractEntity {

    public static final String NAME = "catalog";
}

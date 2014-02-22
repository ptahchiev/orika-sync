package com.db.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = Currency.NAME)
@Table(name = Currency.NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "pk", "id" }) })
public class Currency extends AbstractEntity {

    public static final String NAME = "currency";
}

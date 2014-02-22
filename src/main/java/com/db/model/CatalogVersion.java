package com.db.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = CatalogVersion.NAME)
@Table(name = CatalogVersion.NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "pk" }) })
public class CatalogVersion extends AbstractEntity {

    public static final String NAME = "catalog_version";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog", nullable = true, referencedColumnName = "pk")
    private Catalog catalog;

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(final Catalog catalog) {
        this.catalog = catalog;
    }
    
}

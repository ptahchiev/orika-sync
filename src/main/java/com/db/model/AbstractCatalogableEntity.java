package com.db.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity(name = AbstractCatalogableEntity.NAME)
public class AbstractCatalogableEntity extends AbstractEntity {

    public static final String NAME = "abstractCatalogableEntity";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_version", nullable = true, referencedColumnName = "pk")
    private CatalogVersion catalogVersion;

    public CatalogVersion getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(final CatalogVersion catalogVersion) {
        this.catalogVersion = catalogVersion;
    }
}

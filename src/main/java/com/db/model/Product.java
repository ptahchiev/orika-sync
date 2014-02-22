package com.db.model;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = Product.NAME)
@Table(name = Product.NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "pk", "id" }) })
public class Product extends AbstractCatalogableEntity {

    public static final String NAME = "product";
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, targetEntity = Price.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private Collection<Price> prices;

    public Collection<Price> getPrices() {
        return prices;
    }

    public void setPrices(final Collection<Price> prices) {
        this.prices = prices;
    }
}

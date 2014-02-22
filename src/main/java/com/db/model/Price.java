package com.db.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name = Price.NAME)
@Table(name = Price.NAME, uniqueConstraints = { @UniqueConstraint(columnNames = { "pk", "id" }) })
public class Price extends AbstractCatalogableEntity {
    
    public static final String NAME = "price";

    @Column(name = "value")
    private BigDecimal value;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_pk", nullable = true, referencedColumnName = "pk")
    private Currency currency;
    
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Product.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "product_pk", nullable = true, referencedColumnName = "pk")
    private Product product;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(final BigDecimal value) {
        this.value = value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(final Currency currency) {
        this.currency = currency;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(final Product product) {
        this.product = product;
    }
    
    
    
    
}

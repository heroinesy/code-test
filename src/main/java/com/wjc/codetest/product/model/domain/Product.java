package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "category")
    private String category;

    @Column(name = "name")
    private String name;

    private boolean active = true;

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
        this.active = true;
    }

    /** 상품 비활성화 (삭제 대체) */
    public void deactivate() {
        this.active = false;
    }

    /** 상품 재활성화 */
    public void activate() {
        this.active = true;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}
package com.wjc.codetest.product.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품 생성 요청 DTO
 * - Validation 추가 (@NotBlank)
 * - 기본 생성자(@NoArgsConstructor) 추가로 역직렬화 안정성 확보
 */

@Getter
@Setter
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "category는 비워둘 수 없습니다.")
    private String category;

    @NotBlank(message = "name은 비워둘 수 없습니다.")
    private String name;

    public CreateProductRequest(String category, String name) {
        this.category = category;
        this.name = name;
    }
}
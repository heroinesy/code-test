package com.wjc.codetest.product.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품 수정 요청 DTO
 * - Validation 추가 (@NotNull, @NotBlank)
 * - 기본 생성자(@NoArgsConstructor) 추가
 */

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductRequest {

    @NotNull(message = "id는 필수 항목입니다.")
    private Long id;

    @NotNull(message = "categorey는 비워둘 수 없습니다.")
    private String category;

    @NotNull(message = "name은 비워둘 수 없습니다.")
    private String name;

    public UpdateProductRequest(Long id, String category, String name) {
        this.id = id;
        this.category = category;
        this.name = name;
    }
}
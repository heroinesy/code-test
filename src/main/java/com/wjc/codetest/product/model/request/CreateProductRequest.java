package com.wjc.codetest.product.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품 생성 요청 DTO
 *
 * 1. 문제:
 *    - DTO 필드(category, name)에 null/공백 문자열 요청이 그대로 통과함
 *    - API 진입 전 입력 검증이 이뤄지지 않아 Controller-Service 단의 불필요한 예외 발생 가능
 *
 * 2. 원인:
 *    -  - DTO 단에서 입력 유효성 검증 로직 부재
 *
 * 3. 개선안:
 *    - @NotBlank 추가하여 문자열 필드에 대한 유효성 검증
 *    - @NoArgsConstructor 추가로 역직렬화 안정성 확보
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
package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품 목록 요청 DTO
 * - 기본 생성자(@NoArgsConstructor) 추가
 */

@Getter
@Setter
@NoArgsConstructor

public class GetProductListRequest {
    private String category;
    private int page;
    private int size;
}
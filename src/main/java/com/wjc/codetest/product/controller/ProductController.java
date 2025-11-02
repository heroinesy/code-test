package com.wjc.codetest.product.controller;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.model.response.ProductListResponse;
import com.wjc.codetest.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController
 *
 * 1. 문제:
 *    - REST 설계 원칙 미준수:
 *        • 모든 요청이 POST로 작성되어 HTTP 메서드의 의미가 불명확함
 *        • URI가 행위 중심(/create/product, /delete/product 등)으로 표현되어 REST 규칙 위배
 *    - 조회 로직 불명확:
 *        • 상품 목록 조회와 카테고리 목록 조회의 구분이 불명확하여 가독성과 유지보수성 저하
 *        • POST 요청으로 조회를 수행하여 캐싱·문서화·테스트 효율 저하
 *    - 삭제 로직 문제:
 *        • 실제 delete()를 수행하여 데이터 복구 불가
 *    - 응답 구조 단순화 부족:
 *        • 상태 코드가 항상 200 OK로 고정되어 예외 상황 구분 어려움
 *
 * 2. 원인:
 *    - 초기 코드 작성 시 REST 설계 규칙 및 리소스 중심 API 개념 미흡
 *    - 단순 CRUD 중심으로 빠르게 구현하면서 URI·HTTP 메서드 혼재 발생
 *    - Soft Delete(비활성화) 개념 미적용으로 데이터 보존 고려 부족
 *
 * 3. 개선안:
 *    - REST 표준 준수:
 *        • CRUD 행위를 HTTP 메서드와 리소스 중심 URI로 재구성
 *          → POST /products (상품 생성)
 *          → GET /products/{id} (상품 단건 조회)
 *          → PATCH /products/{id} (상품 수정)
 *          → GET /products (상품 목록 조회, pageable + 정렬 지원)
 *          → GET /products/categories (카테고리 목록 조회)
 *          → POST /products/{id}/active (활성/비활성화)
 *    - 상품 목록 조회 구조 정리:
 *        • GET 메서드와 Pageable 파라미터를 사용하여 페이징·정렬 자동화
 *        • category 파라미터 존재 여부에 따라 전체/필터링 조회 분기
 *    - Soft Delete 적용:
 *        • Product 엔티티에 active 필드 추가
 *        • delete() 대신 activate()/deactivate()로 상태 변경 처리
 *    - 전역 예외 처리(GlobalExceptionHandler) 적용
 */

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /** 상품 단건 조회 */
    @GetMapping(value = "/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "productId") Long productId){
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /** 상품 생성 */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto){
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    /** 상품 상태 활성화/비활성화 */
    @PostMapping(value = "/{productId}/active")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable(name = "productId") Long productId,
            @RequestParam boolean active
    ){
        productService.changeActive(productId, active);
        return ResponseEntity.ok().build();
    }

    /** 상품 수정 */
    @PatchMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest dto) {
        dto.setId(productId);
        Product product = productService.update(dto);
        return ResponseEntity.ok(product);
    }

    /**
     * 상품 목록 조회 (카테고리별 + 페이징 + 정렬)
     * - GET /products
     * - 예시 : /api/products?category=furniture&page=0&size=10&sort=name,desc
     */
    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(required = false) String category,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Product> products = (category == null)
                ? productService.getAll(pageable)
                : productService.getListByCategory(category, pageable);

        ProductListResponse response = new ProductListResponse(
                products.getContent(),
                products.getTotalPages(),
                products.getTotalElements(),
                products.getNumber()
        );

        return ResponseEntity.ok(response);
    }

    /** 카테고리 목록 조회 */
    @GetMapping(value = "/categories")
    public ResponseEntity<List<String>> getCategory(){
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}
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
    @PostMapping(value = "/create/product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto){
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    /** 상품 삭제 */
    @PostMapping(value = "/{productId}/active")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable(name = "productId") Long productId,
            @RequestParam boolean active
    ){
        productService.changeActive(productId, active);
        return ResponseEntity.ok().build();
    }

    /** 상품 수정 */
    @PostMapping(value = "/update/product")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequest dto){
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
package com.wjc.codetest.product.service;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * ProductService
 *
 * 1. 문제:
 *    - 트랜잭션 일관성 부재:
 *        • 데이터 생성(create), 수정(update), 삭제(delete)에서 트랜잭션이 누락되어 예외 발생 시 rollback 불가
 *        • 조회 메서드에서도 트랜잭션이 없으므로, 영속성 컨텍스트가 보장되지 않아 Lazy Loading 등에서 예외 가능
 *    - 성능 비효율:
 *        • 모든 메서드에 트랜잭션을 걸 경우, 단순 조회 시 불필요한 flush/락이 발생할 위험 존재
 *
 * 2. 원인:
 *    - 초기 코드 작성 시 CRUD 단위 예외 상황에 대한 고려 미흡
 *    - JPA의 트랜잭션 및 readOnly 옵션 활용 부족
 *
 * 3. 개선안:
 *    - 클래스 레벨에 @Transactional(readOnly = true) 적용하여 조회 계열 성능 최적화
 *    - 데이터 변경이 발생하는 메서드에만 별도의 @Transactional 추가
 *        → create(), update(), changeActive()
 *    - 예외 발생 시 트랜잭션 rollback 보장, 조회는 readOnly로 connection 점유 최소화
 */


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /** 상품 생성 */
    @Transactional
    public Product create(CreateProductRequest dto) {
        Product product = new Product(dto.getCategory(), dto.getName());
        return productRepository.save(product);
    }

    /** 상품 수정 */
    @Transactional
    public Product update(UpdateProductRequest dto) {
        Product product = getProductById(dto.getId());
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        Product updatedProduct = productRepository.save(product);
        return updatedProduct;
    }

    /** 상품 상태 활성화/비활성화 변경 */
    @Transactional
    public void changeActive(Long productId, boolean active) {
        Product product = getProductById(productId);

        if (active) {
            product.activate();
        } else {
            product.deactivate();
        }
        productRepository.save(product);
    }

    /** 상품 단건 조회 */
    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new RuntimeException("product not found");
        }
        return productOptional.get();
    }

    /** 목록 조회 */
    public Page<Product> getAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getListByCategory(String category, Pageable pageable) {
        return productRepository.findAllByCategory(category, pageable);
    }

    public List<String> getUniqueCategories() {
        return productRepository.findDistinctCategories();
    }
}
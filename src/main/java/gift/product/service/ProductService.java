package gift.product.service;
import gift.product.domain.Product;
import gift.product.domain.ProductNameValidator;
import gift.product.dto.ProductRequest;
import gift.product.dto.ProductResponse;
import gift.product.repository.ProductRepository;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.order.repository.OrderRepository;
import gift.wish.repository.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WishRepository wishRepository;
    private final OrderRepository orderRepository;

    public ProductService(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        WishRepository wishRepository,
        OrderRepository orderRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.wishRepository = wishRepository;
        this.orderRepository = orderRepository;
    }

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse getProduct(Long id) {
        return ProductResponse.from(productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다.")));
    }

    public ProductResponse createProduct(ProductRequest request) {
        validateName(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));
        return ProductResponse.from(productRepository.save(request.toEntity(category)));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        validateName(request.name());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        product.update(request.name(), request.price(), request.imageUrl(), category);
        return ProductResponse.from(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (wishRepository.existsByProductId(id) || orderRepository.existsByProductId(id)) {
            throw new IllegalArgumentException("위시리스트나 주문이 있는 상품은 삭제할 수 없습니다.");
        }
        productRepository.deleteById(id);
    }

    private void validateName(String name) {
        var errors = ProductNameValidator.validate(name);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}
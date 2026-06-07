package gift.category.service;
import gift.category.domain.Category;
import gift.category.dto.CategoryRequest;
import gift.category.dto.CategoryResponse;
import gift.category.repository.CategoryRepository;

import gift.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        return CategoryResponse.from(categoryRepository.save(request.toEntity()));
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));
        category.update(request.name(), request.color(), request.imageUrl(), request.description());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException("상품이 있는 카테고리는 삭제할 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }
}
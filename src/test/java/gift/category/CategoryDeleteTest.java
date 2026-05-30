package gift.category;
import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;

import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryDeleteTest {

    @Autowired MockMvc mockMvc;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;

    @Test
    void 상품이_있는_카테고리_삭제시_400() throws Exception {
        Category category = categoryRepository.save(new Category("카테고리삭제_테스트", "#fff", "http://test.url", null));
        productRepository.save(new Product("카테고리삭제_상품", 1000, "http://test.url", category));

        mockMvc.perform(delete("/api/categories/" + category.getId()))
            .andExpect(status().isBadRequest());
    }
}
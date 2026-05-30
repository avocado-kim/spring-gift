package gift.product;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.wish.domain.Wish;
import gift.wish.repository.WishRepository;
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
class ProductDeleteTest {

    @Autowired MockMvc mockMvc;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired WishRepository wishRepository;

    @Test
    void 위시가_있는_상품_삭제시_400() throws Exception {
        Category category = categoryRepository.save(new Category("상품삭제_테스트카테고리", "#fff", "http://test.url", null));
        Product product = productRepository.save(new Product("삭제테스트상품", 1000, "http://test.url", category));
        Member member = memberRepository.save(new Member("prod_delete@example.com", "pw"));
        wishRepository.save(new Wish(member, product));

        mockMvc.perform(delete("/api/products/" + product.getId()))
            .andExpect(status().isBadRequest());
    }
}
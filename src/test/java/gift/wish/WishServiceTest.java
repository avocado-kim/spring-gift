package gift.wish;
import gift.wish.repository.WishRepository;
import gift.wish.service.WishService;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.CONFLICT;

@SpringBootTest
@Transactional
class WishServiceTest {

    @Autowired WishService wishService;
    @Autowired WishRepository wishRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired MemberRepository memberRepository;

    @Test
    void 위시_중복_추가시_409() {
        Category category = categoryRepository.save(new Category("위시서비스_테스트카테고리", "#ffffff", "http://test.url", null));
        Product product = productRepository.save(new Product("위시서비스상품", 1000, "http://test.url", category));
        Member member = memberRepository.save(new Member("wishservice@example.com", "pw"));

        wishService.addWish(member.getId(), product.getId());

        assertThatThrownBy(() -> wishService.addWish(member.getId(), product.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(e -> {
                ResponseStatusException rse = (ResponseStatusException) e;
                assertThat(rse.getStatusCode().value()).isEqualTo(CONFLICT.value());
            });
    }
}
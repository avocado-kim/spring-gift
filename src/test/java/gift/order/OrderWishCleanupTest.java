package gift.order;
import gift.order.domain.Order;
import gift.order.service.OrderService;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import gift.wish.domain.Wish;
import gift.wish.repository.WishRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderWishCleanupTest {

    @Autowired OrderService orderService;
    @Autowired WishRepository wishRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OptionRepository optionRepository;
    @Autowired MemberRepository memberRepository;

    private Member member;
    private Option option;
    private Product product;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category("위시_테스트카테고리", "#ffffff", "http://test.url", null));
        product = productRepository.save(new Product("위시테스트상품", 100, "http://test.url", category));
        option = optionRepository.save(new Option(product, "위시테스트옵션", 10));
        member = memberRepository.save(new Member("wish_cleanup@example.com", "pw"));
        member.chargePoint(10000);
        memberRepository.save(member);
        wishRepository.save(new Wish(member, product));
    }

    @Test
    void 주문_완료_후_위시리스트_삭제() {
        assertThat(wishRepository.findByMember_IdAndProductId(member.getId(), product.getId())).isPresent();

        orderService.createOrder(member, option.getId(), 1, null);

        assertThat(wishRepository.findByMember_IdAndProductId(member.getId(), product.getId())).isEmpty();
    }
}
package gift.order;
import gift.order.service.OrderService;

import gift.category.domain.Category;
import gift.category.repository.CategoryRepository;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceTransactionTest {

    @Autowired OrderService orderService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OptionRepository optionRepository;
    @Autowired MemberRepository memberRepository;

    private Long categoryId;
    private Long productId;
    private Long optionId;
    private Long memberId;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(new Category("TX_TEST_카테고리", "#000000", "http://test.url", null));
        categoryId = category.getId();
        Product product = productRepository.save(new Product("TX상품", 1000, "http://test.url", category));
        productId = product.getId();
        Option option = optionRepository.save(new Option(product, "TX옵션", 10));
        optionId = option.getId();
        Member member = memberRepository.save(new Member("txtest@example.com", "pw")); // 포인트 0
        memberId = member.getId();
    }

    @AfterEach
    void tearDown() {
        optionRepository.deleteById(optionId);
        productRepository.deleteById(productId);
        categoryRepository.deleteById(categoryId);
        memberRepository.deleteById(memberId);
    }

    @Test
    void 포인트_부족시_재고_차감_롤백() {
        assertThatThrownBy(() -> orderService.createOrder(memberId, optionId, 1, null))
            .isInstanceOf(IllegalArgumentException.class);

        int quantityAfter = optionRepository.findById(optionId).orElseThrow().getQuantity();
        assertThat(quantityAfter).isEqualTo(10); // 롤백되어 원래 재고가 유지되어야 한다
    }
}
package gift.order;
import gift.order.service.KakaoMessagePort;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class OrderServiceTransactionTest {

    @Autowired OrderService orderService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired OptionRepository optionRepository;
    @Autowired MemberRepository memberRepository;
    @MockitoBean KakaoMessagePort kakaoMessagePort;

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
        assertThat(quantityAfter).isEqualTo(10);
    }

    @Test
    void 카카오_발송_실패시_주문_롤백() {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.chargePoint(10000);
        member.updateKakaoAccessToken("valid-token");
        memberRepository.save(member);

        doThrow(new RuntimeException("카카오 API 오류")).when(kakaoMessagePort).sendToMe(any(), any(), any());

        assertThatThrownBy(() -> orderService.createOrder(memberId, optionId, 1, null))
            .isInstanceOf(RuntimeException.class);

        assertThat(optionRepository.findById(optionId).orElseThrow().getQuantity()).isEqualTo(10);
        assertThat(memberRepository.findById(memberId).orElseThrow().getPoint()).isEqualTo(10000);
    }
}
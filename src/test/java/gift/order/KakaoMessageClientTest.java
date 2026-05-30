package gift.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.category.Category;
import gift.member.Member;
import gift.option.Option;
import gift.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;

class KakaoMessageClientTest {

    private final KakaoMessageClient client = new KakaoMessageClient(RestClient.builder());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 상품명에_따옴표_포함시_유효한_JSON() {
        Category category = new Category("테스트", "#000", "url", null);
        Product product = new Product("상품 \"특별\" 에디션", 1000, "url", category);
        Option option = new Option(product, "기본", 10);
        Member member = new Member("test@test.com", "pw");
        Order order = new Order(option, member, 1, "메시지");

        String json = client.buildTemplate(order, product);

        assertThatCode(() -> objectMapper.readTree(json))
            .as("결과가 유효한 JSON이어야 한다")
            .doesNotThrowAnyException();
    }
}
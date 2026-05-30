package gift.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.product.Product;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class KakaoMessageClient implements KakaoMessagePort {
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KakaoMessageClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public void sendToMe(String accessToken, Order order, Product product) {
        var templateObject = buildTemplate(order, product);

        var params = new LinkedMultiValueMap<String, String>();
        params.add("template_object", templateObject);

        restClient.post()
            .uri("https://kapi.kakao.com/v2/api/talk/memo/default/send")
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(params)
            .retrieve()
            .toBodilessEntity();
    }

    String buildTemplate(Order order, Product product) {
        var totalPrice = String.format("%,d", product.getPrice() * order.getQuantity());
        var messageText = "🎁 선물이 도착했어요!\n\n"
            + product.getName() + " (" + order.getOption().getName() + ")\n"
            + "수량: " + order.getQuantity() + "개\n"
            + "금액: " + totalPrice + "원"
            + (order.getMessage() != null && !order.getMessage().isBlank()
                ? "\n\n💌 " + order.getMessage() : "");

        try {
            return objectMapper.writeValueAsString(Map.of(
                "object_type", "text",
                "text", messageText,
                "link", Map.of(),
                "button_title", "선물 확인하기"
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 메시지 템플릿 생성 실패", e);
        }
    }
}
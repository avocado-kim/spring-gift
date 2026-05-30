package gift.order.service;
import gift.order.domain.Order;

import gift.product.domain.Product;

public interface KakaoMessagePort {
    void sendToMe(String accessToken, Order order, Product product);
}

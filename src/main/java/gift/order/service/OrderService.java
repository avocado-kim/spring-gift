package gift.order.service;
import gift.order.domain.Order;
import gift.order.dto.OrderResponse;
import gift.order.repository.OrderRepository;

import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.option.domain.Option;
import gift.option.repository.OptionRepository;
import gift.wish.repository.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final WishRepository wishRepository;
    private final KakaoMessagePort kakaoMessagePort;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        MemberRepository memberRepository,
        WishRepository wishRepository,
        KakaoMessagePort kakaoMessagePort
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.wishRepository = wishRepository;
        this.kakaoMessagePort = kakaoMessagePort;
    }

    public Page<OrderResponse> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMember_Id(memberId, pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse createOrder(Long memberId, Long optionId, int quantity, String message) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));
        Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 옵션입니다."));

        option.subtractQuantity(quantity);
        member.deductPoint(option.getProduct().getPrice() * quantity);

        Order saved = orderRepository.save(new Order(option, member, quantity, message));

        wishRepository.deleteByMember_IdAndProductId(memberId, option.getProduct().getId());

        sendKakaoMessage(member, saved, option);
        return OrderResponse.from(saved);
    }

    private void sendKakaoMessage(Member member, Order order, Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        kakaoMessagePort.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
    }
}
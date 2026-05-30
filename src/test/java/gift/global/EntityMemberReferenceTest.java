package gift.global;

import gift.member.Member;
import gift.order.Order;
import gift.order.OrderRepository;
import gift.wish.Wish;
import gift.wish.WishRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EntityMemberReferenceTest {

    @Autowired OrderRepository orderRepository;
    @Autowired WishRepository wishRepository;

    @Test
    void Order_getMember_직접_반환() {
        Order order = orderRepository.findAll().stream().findFirst().orElseThrow();
        Member member = order.getMember(); // compile error until @ManyToOne Member is added
        assertThat(member).isNotNull();
        assertThat(member.getEmail()).isNotBlank();
    }

    @Test
    void Wish_getMember_직접_반환() {
        Wish wish = wishRepository.findAll().stream().findFirst().orElseThrow();
        Member member = wish.getMember(); // compile error until @ManyToOne Member is added
        assertThat(member).isNotNull();
        assertThat(member.getEmail()).isNotBlank();
    }
}
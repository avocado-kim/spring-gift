package gift.order;

import gift.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderMemberReferenceTest {

    @Autowired OrderRepository orderRepository;

    @Test
    void Order_getMember_직접_반환() {
        Order order = orderRepository.findAll().stream().findFirst().orElseThrow();
        Member member = order.getMember();
        assertThat(member).isNotNull();
        assertThat(member.getEmail()).isNotBlank();
    }
}
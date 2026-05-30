package gift.wish;
import gift.wish.domain.Wish;
import gift.wish.repository.WishRepository;

import gift.member.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WishMemberReferenceTest {

    @Autowired WishRepository wishRepository;

    @Test
    void Wish_getMember_직접_반환() {
        Wish wish = wishRepository.findAll().stream().findFirst().orElseThrow();
        Member member = wish.getMember(); // compile error until @ManyToOne Member is added
        assertThat(member).isNotNull();
        assertThat(member.getEmail()).isNotBlank();
    }
}
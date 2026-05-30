package gift.member;
import gift.member.domain.Member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void chargePoint_정상_충전() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(500);
        assertThat(member.getPoint()).isEqualTo(500);
    }

    @Test
    void chargePoint_누적_충전() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(500);
        member.chargePoint(300);
        assertThat(member.getPoint()).isEqualTo(800);
    }

    @Test
    void chargePoint_0이하_예외() {
        Member member = new Member("test@example.com", "pw");
        assertThatThrownBy(() -> member.chargePoint(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chargePoint_음수_예외() {
        Member member = new Member("test@example.com", "pw");
        assertThatThrownBy(() -> member.chargePoint(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deductPoint_정상_차감() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(1000);
        member.deductPoint(400);
        assertThat(member.getPoint()).isEqualTo(600);
    }

    @Test
    void deductPoint_잔액과_동일한_금액_차감() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(500);
        member.deductPoint(500);
        assertThat(member.getPoint()).isEqualTo(0);
    }

    @Test
    void deductPoint_잔액_초과_예외() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(100);
        assertThatThrownBy(() -> member.deductPoint(101))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deductPoint_0이하_예외() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(100);
        assertThatThrownBy(() -> member.deductPoint(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deductPoint_음수_예외() {
        Member member = new Member("test@example.com", "pw");
        member.chargePoint(100);
        assertThatThrownBy(() -> member.deductPoint(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
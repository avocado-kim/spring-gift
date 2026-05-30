package gift.option;

import gift.category.Category;
import gift.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    private Option option;

    @BeforeEach
    void setUp() {
        Category category = new Category("테스트", "#000000", "http://img.url", null);
        Product product = new Product("상품", 1000, "http://img.url", category);
        option = new Option(product, "기본옵션", 10);
    }

    @Test
    void subtractQuantity_정상_차감() {
        option.subtractQuantity(3);
        assertThat(option.getQuantity()).isEqualTo(7);
    }

    @Test
    void subtractQuantity_전량_차감() {
        option.subtractQuantity(10);
        assertThat(option.getQuantity()).isEqualTo(0);
    }

    @Test
    void subtractQuantity_재고_초과_예외() {
        assertThatThrownBy(() -> option.subtractQuantity(11))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtractQuantity_0_예외() {
        assertThatThrownBy(() -> option.subtractQuantity(0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtractQuantity_음수_예외() {
        assertThatThrownBy(() -> option.subtractQuantity(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
package gift.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductNameValidatorTest {

    @Test
    void 유효한_이름_오류없음() {
        List<String> errors = ProductNameValidator.validate("맥북 프로 14");
        assertThat(errors).isEmpty();
    }

    @Test
    void 허용_특수문자_포함_유효() {
        List<String> errors = ProductNameValidator.validate("상품 (A+B) [특가]");
        assertThat(errors).isEmpty();
    }

    @Test
    void null_오류() {
        List<String> errors = ProductNameValidator.validate(null);
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 공백만_있는_이름_오류() {
        List<String> errors = ProductNameValidator.validate("   ");
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 최대길이_초과_오류() {
        List<String> errors = ProductNameValidator.validate("a".repeat(16));
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 최대길이_정확히_유효() {
        List<String> errors = ProductNameValidator.validate("a".repeat(15));
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"상품!", "상품@명", "상품#1", "상품$"})
    void 허용되지_않는_특수문자_오류(String name) {
        List<String> errors = ProductNameValidator.validate(name);
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 카카오_포함_오류() {
        List<String> errors = ProductNameValidator.validate("카카오선물");
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 카카오_포함_허용플래그_사용_유효() {
        List<String> errors = ProductNameValidator.validate("카카오선물", true);
        assertThat(errors).isEmpty();
    }
}

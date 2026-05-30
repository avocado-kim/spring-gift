package gift.option;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OptionNameValidatorTest {

    @Test
    void 유효한_이름_오류없음() {
        List<String> errors = OptionNameValidator.validate("기본 옵션 (A)");
        assertThat(errors).isEmpty();
    }

    @Test
    void null_오류() {
        List<String> errors = OptionNameValidator.validate(null);
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 공백만_있는_이름_오류() {
        List<String> errors = OptionNameValidator.validate("   ");
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 최대길이_초과_오류() {
        List<String> errors = OptionNameValidator.validate("a".repeat(51));
        assertThat(errors).isNotEmpty();
    }

    @Test
    void 최대길이_정확히_유효() {
        List<String> errors = OptionNameValidator.validate("a".repeat(50));
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"옵션!", "옵션@A", "옵션#1", "옵션$"})
    void 허용되지_않는_특수문자_오류(String name) {
        List<String> errors = OptionNameValidator.validate(name);
        assertThat(errors).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"옵션 (A+B)", "option [1]", "옵션-A/B_C&D"})
    void 허용_특수문자_조합_유효(String name) {
        List<String> errors = OptionNameValidator.validate(name);
        assertThat(errors).isEmpty();
    }
}
package gift.product.domain;

import gift.global.NameAllowedPattern;
import java.util.ArrayList;
import java.util.List;

public class ProductNameValidator {
    private static final int MAX_LENGTH = 15;

    private ProductNameValidator() {
    }

    public static List<String> validate(String name) {
        return validate(name, false);
    }

    public static List<String> validate(String name, boolean allowKakao) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add("상품 이름은 필수입니다.");
            return errors;
        }

        if (name.length() > MAX_LENGTH) {
            errors.add("상품 이름은 공백을 포함하여 최대 15자까지 입력할 수 있습니다.");
        }

        if (!NameAllowedPattern.ALLOWED.matcher(name).matches()) {
            errors.add("상품 이름에 허용되지 않는 특수 문자가 포함되어 있습니다. 사용 가능: ( ), [ ], +, -, &, /, _");
        }

        if (!allowKakao && name.contains("카카오")) {
            errors.add("\"카카오\"가 포함된 상품명은 담당 MD와 협의한 경우에만 사용할 수 있습니다.");
        }

        return errors;
    }
}

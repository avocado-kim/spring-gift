package gift.option;

import gift.global.NameAllowedPattern;
import java.util.ArrayList;
import java.util.List;

/*
 * Validates option names against the following rules:
 * - Must not be null or blank
 * - Maximum length of 50 characters (including spaces)
 * - Only Korean, English, digits, spaces, and selected special characters are allowed: ( ) [ ] + - & / _
 */
public class OptionNameValidator {
    private static final int MAX_LENGTH = 50;

    private OptionNameValidator() {
    }

    public static List<String> validate(String name) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add("옵션 이름은 필수입니다.");
            return errors;
        }

        if (name.length() > MAX_LENGTH) {
            errors.add("옵션 이름은 공백을 포함하여 최대 50자까지 입력할 수 있습니다.");
        }

        if (!NameAllowedPattern.ALLOWED.matcher(name).matches()) {
            errors.add("옵션 이름에 허용되지 않는 특수 문자가 포함되어 있습니다. 사용 가능: ( ), [ ], +, -, &, /, _");
        }

        return errors;
    }
}

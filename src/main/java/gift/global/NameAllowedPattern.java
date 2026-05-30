package gift.global;

import java.util.regex.Pattern;

public final class NameAllowedPattern {
    public static final Pattern ALLOWED =
        Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$");

    private NameAllowedPattern() {
    }
}

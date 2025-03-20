package part2.common.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MessageType {
    REQUEST(0),

    RESPONSE(1);

    private final int code;
}

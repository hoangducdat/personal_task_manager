package org.project.personal_task_manager.utils.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {
    public static String formatLocalDateTime(LocalDateTime input, String pattern) {
        var datetimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return datetimeFormatter.format(input);
    }
}

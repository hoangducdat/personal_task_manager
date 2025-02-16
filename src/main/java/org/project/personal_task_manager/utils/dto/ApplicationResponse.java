package org.project.personal_task_manager.utils.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.project.personal_task_manager.utils.constants.CommonConstants;
import org.project.personal_task_manager.utils.DateTimeUtils;


@NoArgsConstructor
@Setter
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponse<T> {
    private int status;
    private String timestamp;
    private T data;
    private Object error;

    public static <T> ApplicationResponse<T> of(int status, T data) {
        var applicationResponse = new ApplicationResponse<T>();
        applicationResponse.setStatus(status);
        applicationResponse.setTimestamp(getResponseTimestamp());
        applicationResponse.setData(data);
        return applicationResponse;
    }

    public static ApplicationResponse<Void> error(int status, Object error) {
        var applicationResponse = new ApplicationResponse<Void>();
        applicationResponse.setStatus(status);
        applicationResponse.setTimestamp(getResponseTimestamp());
        applicationResponse.setError(error);
        return applicationResponse;
    }

    private static String getResponseTimestamp() {
        return DateTimeUtils.formatLocalDateTime(
                LocalDateTime.now(),
                CommonConstants.DateTimeConstants.DATE_TIME_FORMAT_PATTERN
        );
    }
}

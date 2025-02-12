package org.project.personal_task_manager.utils.constants;


public class SecurityConstants {

    public static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    public static final String REGISTER_ENDPOINT = "/api/v1/auth/register";
    public static final String REFRESH_TOKEN_ENDPOINT = "/api/v1/refresh-token";
    public static final String[] PUBLIC_ENDPOINTS = {
            LOGIN_ENDPOINT,
            REGISTER_ENDPOINT,
            REFRESH_TOKEN_ENDPOINT
    };
}

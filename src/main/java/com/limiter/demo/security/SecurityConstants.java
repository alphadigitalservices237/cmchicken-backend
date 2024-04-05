package com.limiter.demo.security;

public class SecurityConstants {
    public static final long JWT_EXPIRATION = 262800000;
    public static final String JWT_SECRET = "secret";

    public static final String URLS = "api/v1/**";
    public static final String PUBLIC_URLS = "api/public/**";
}

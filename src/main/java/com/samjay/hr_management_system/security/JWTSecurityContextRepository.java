package com.samjay.hr_management_system.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JWTSecurityContextRepository implements ServerSecurityContextRepository {

    private final JWTAuthenticationManager JWTAuthenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {

        return Mono.empty();

    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/login") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars")) {

            return Mono.empty();

        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            return Mono.empty();

        }

        String authToken = authHeader.substring(7);

        Authentication authentication = new UsernamePasswordAuthenticationToken(authToken, authToken);

        return JWTAuthenticationManager.authenticate(authentication).map(SecurityContextImpl::new);

    }
}

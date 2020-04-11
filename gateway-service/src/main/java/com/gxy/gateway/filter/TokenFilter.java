package com.gxy.gateway.filter;



import com.jwt.base.client.utils.JWTUtil;
import io.jsonwebtoken.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * 验证token过滤器
 */
@Component
public class TokenFilter implements GlobalFilter, Ordered {
    @Value("${white_list}")
    private String[] whiteList;
    @Value("${black_list}")
    private String[] blackList;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri1 = exchange.getRequest().getURI();
        //1.判断白名单和黑名单
        if (whiteList != null && uri1 != null && Arrays.asList(whiteList).contains(uri1.getPath())) {
            return chain.filter(exchange);
        }
        //黑名单
        if (blackList != null && uri1 != null && Arrays.asList(blackList).contains(uri1)) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 blackList.\"}");
        }
        HttpHeaders headers = exchange.getRequest().getHeaders();
        //2.判断是否携带accesstoken
        boolean authorization = headers.containsKey(AUTHORIZATION);
        if (!authorization) {
            //未携带token
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 Unauthorized.\"}");
        }
        //3.验证accesstoken是否合法/是否失效
        String token = headers.getFirst(AUTHORIZATION);
        try {
            String jwtData[] = token.split("\\.");
            String data = jwtData[0] + "." + jwtData[1];//header+payload   jwtData[2]=签名
            boolean verify = JWTUtil.verify(data.getBytes(), jwtData[2]);//验证是拿加密数据和签名一起去验签
            if (!verify) {
                return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token error.\"}");
            }
        } catch (Exception e) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token error.\"}");
        }

        //解密token
        Claims body = null;
        try {
            body = Jwts.parser().setSigningKey(JWTUtil.getPublicKey()).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token expired.\"}");
        } catch (UnsupportedJwtException e) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token unsupported.\"}");
        } catch (MalformedJwtException e) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token malformed.\"}");
        } catch (SignatureException e) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token error.\"}");
        } catch (IllegalArgumentException e) {
            return getVoidMono(exchange, "{\"code\": \"401\",\"msg\": \"401 token illegal.\"}");
        }

        String jwt = new String();
        if (body.containsKey("userId")) {

        }
        String id = body.getId();//判断redis是否存在，存在则有效，不存在视为注销登录或者已强制退出

        ServerHttpRequest oldRequest = exchange.getRequest();
        URI uri = oldRequest.getURI();
        ServerHttpRequest newRequest = oldRequest.mutate().uri(uri).build();
        // 定义新的消息头
        HttpHeaders headerList = new HttpHeaders();
        headerList.putAll(exchange.getRequest().getHeaders());
        headerList.remove("Authorization");
        headerList.set("userId", jwt.toString());
        //重新设置Authorization给下游微服务使用
        newRequest = new ServerHttpRequestDecorator(newRequest) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headerList);
                return httpHeaders;
            }
        };

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private Mono<Void> getVoidMono(ServerWebExchange exchange, String s) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        originalResponse.setStatusCode(HttpStatus.OK);
        originalResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        byte[] response = s
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = originalResponse.bufferFactory().wrap(response);
        return originalResponse.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;//数字越小，优先级越高
    }
}

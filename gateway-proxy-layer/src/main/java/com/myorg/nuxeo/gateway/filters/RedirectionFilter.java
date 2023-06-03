package com.myorg.nuxeo.gateway.filters;

import com.myorg.nuxeo.gateway.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

/**
 * Filter to make changes to the request and append certain more params
 */
@Component
public class RedirectionFilter extends AbstractGatewayFilterFactory<RedirectionFilter.RedirectionFilterConfig> {

    public static final String FOLDER_PATH = "folderPath=";
    public static final String AND = "&";
    public static final String QUESTION = "?";
    private final Logger logger = LoggerFactory.getLogger(RedirectionFilter.class);

    private static final String X_CLIENT_ID = "X-Client-Id";
    private static final String CLIENTS_MAP = "clients";

    private final ApplicationProperties applicationProperties;

    @Autowired
    public RedirectionFilter(ApplicationProperties applicationProperties) {
        super(RedirectionFilterConfig.class);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public GatewayFilter apply(RedirectionFilterConfig config) {
        return new OrderedGatewayFilter((exchange, chain) -> {

            logger.info("Inside redirection filter");

            ServerHttpRequest serverHttpRequest = exchange.getRequest();
            Map<String, String> headers = serverHttpRequest.getHeaders().toSingleValueMap();


            String path = serverHttpRequest.getPath().toString();

            path=path.replaceAll("/smart-doc","").replace("/","-");

            if (path == null) {
                return chain.filter(exchange);
            }

            RouteDefinition requestRoute = getRoute("proxyLayer"+path);

            if (requestRoute == null) {
                return chain.filter(exchange);
            }

            String organisationId = headers.get(X_CLIENT_ID);

            if (!canRedirect(organisationId, requestRoute)) {
                return chain.filter(exchange);
            }

            URI redirectURI = null;
            try {
                redirectURI = buildURI(requestRoute,organisationId,serverHttpRequest.getURI().getQuery());
            } catch (URISyntaxException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            ServerHttpRequest modifiedRequest = exchange
                    .getRequest()
                    .mutate()
                    .uri(redirectURI)
                    .build();

            ServerWebExchange modifiedExchange = exchange
                    .mutate()
                    .request(modifiedRequest)
                    .build();

            modifiedExchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, redirectURI);

            return chain.filter(modifiedExchange);

        }, 1);
    }

    private URI buildURI(RouteDefinition route, String client, String query) throws URISyntaxException, UnsupportedEncodingException {
        Map<String, Object> metadata = route.getMetadata();
        String redirectURI = metadata.get("redirectURI").toString();
        Map<String, String> folders = (Map<String, String>) metadata.get("folders");
        String finalClient = client+"=";
        Map<String, String> result = folders.entrySet()
                .stream()
                .filter(map -> map.getValue().contains(finalClient))
                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
        Map.Entry<String,String> entry = result.entrySet().iterator().next();
        String folder=entry.getValue().replace(finalClient,"");
        return new URI(redirectURI + QUESTION +query+ AND + FOLDER_PATH +encodeValue(folder));
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    private boolean canRedirect(String organisationId, RouteDefinition routeDefinition) {
        Map<String, Object> metadata = routeDefinition.getMetadata();
        Map<String, String> organisations = (Map<String, String>) metadata.get(CLIENTS_MAP);

        return organisations.containsValue(organisationId);
    }

    private RouteDefinition getRoute(String clientPortalIdentifier) {
        List<RouteDefinition> routeDefinitions = applicationProperties.getRoutes();
        RouteDefinition requestRoute = null;

        for (RouteDefinition routeDefinition : routeDefinitions) {
            if (routeDefinition.getId().equals(clientPortalIdentifier)) {
                requestRoute = routeDefinition;
            }
        }

        return requestRoute;
    }

    public static class RedirectionFilterConfig {

    }
}

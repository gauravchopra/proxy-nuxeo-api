package com.myorg.nuxeo.proxylayer.configuration;


import okhttp3.Interceptor;
import org.nuxeo.client.NuxeoClient;
import org.nuxeo.client.spi.NuxeoClientException;
import org.nuxeo.client.spi.auth.BasicAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class NuxeoClientConfiguration {
    @Value("${nuxeo.baseURL}")
    private String baseUrl;

    @Value("${nuxeo.accessToken}")
    private String accessToken;


    @Value("${nuxeo.user}")
    private String nuxeoUser;
    @Value("${nuxeo.password}")
    private String nuxeoPassword;


        @Bean
        public CommonsRequestLoggingFilter logFilter() {
            CommonsRequestLoggingFilter filter
                    = new CommonsRequestLoggingFilter();
            filter.setIncludeQueryString(true);
            filter.setIncludePayload(true);
            filter.setMaxPayloadLength(10000);
            filter.setIncludeHeaders(false);
            filter.setAfterMessagePrefix("REQUEST DATA: ");
            return filter;
        }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NuxeoClient nuxeoClient() throws NuxeoClientException {

        return new NuxeoClient.Builder()
                .url(baseUrl)
                .authentication(new BasicAuthInterceptor(nuxeoUser, nuxeoPassword))
                .schemas("*")
                .connect();
    }
}

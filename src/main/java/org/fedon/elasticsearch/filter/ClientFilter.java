package org.fedon.elasticsearch.filter;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * @author Dmytro Fedonin
 *
 */
public class ClientFilter implements ClientRequestFilter {
    static ThreadLocal<String> cookieLocal = new ThreadLocal<>();

    public static void setCookie(String cookie) {
        cookieLocal.set(cookie);
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String cookie = cookieLocal.get();
        // TODO cash map?
        if (cookie != null && cookie.length() > 0) {
            requestContext.getHeaders().add("Cookie", cookie);
        }
    }
}

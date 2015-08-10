package org.fedon.elasticsearch.filter;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;
import org.fedon.elasticsearch.plugin.SessionService;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import com.cognoscience.api.skillcollider.IntegrationREST;

/**
 * @author Dmytro Fedonin
 *
 */
public class SessionFilter extends RestFilter {
    private ESLogger log = Loggers.getLogger(this.getClass());
    private IntegrationREST proxy;

    private LoadingCache<String, String> cache;
    private String referer = "/exp";
    private Client client;
    private String base = "rest";
    // static config in form of 'http://localhost:8080/skillcollider/rest' if not set will use refererAttr to find out from the first call
    final String baseAttr = "cors.session.rest.base";
    final String restContextAttr = "cors.session.rest.context";
    final String refererAttr = "cors.session.referer.url.pattern";
    final String durationAttr = "cors.session.cach.duration";
    final String sizeAttr = "cors.session.cach.size";

    public SessionFilter(SessionService service) {
        referer = service.getSettings().get(refererAttr, referer);
        String restBase = service.getSettings().get(baseAttr);
        base = service.getSettings().get(restContextAttr, base);
        ClientConfig cc = new ClientConfig();
        cc.register(ClientFilter.class);
        client = ClientBuilder.newClient(cc);
        if (restBase != null) {
            proxy = WebResourceFactory.newResource(IntegrationREST.class, client.target(restBase));
            base = restBase;
        }

        Long size = Long.parseLong(service.getSettings().get(sizeAttr, "100"));
        Long duration = Long.parseLong(service.getSettings().get(durationAttr, "300"));
        cache = CacheBuilder.newBuilder().maximumSize(size).expireAfterAccess(duration, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return notFound();
                    }
                });
    }

    @Override
    public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) throws Exception {
        // Allow OPTIONS without auth
        Method method = request.method();
        if (method == Method.OPTIONS) {
            log.info("++ options ++");
            filterChain.continueProcessing(request, channel);
            return;
        }
        String cookie = request.header("Kookie");
        String url = null;
        if (proxy == null) {
            url = request.header("Referer");
            if (url == null || url.length() < 10) {
                return;
            }
            base = url.substring(0, url.indexOf(referer, 5) + 1) + base;
            proxy = WebResourceFactory.newResource(IntegrationREST.class, client.target(base));
        }

        try {
            String result = cache.get(cookie);
            // in cash if result is empty string
            if (result.length() == 0) {
                log.debug("+++ in cache");
                filterChain.continueProcessing(request, channel);
                return;
            }
        } catch (Exception e) {
            log.warn("Cache exception: " + e.getMessage());
        }
        try {
            if (cookie != null && cookie.length() > 0) {
                ClientFilter.setCookie(cookie);
            }
            proxy.checkSession();
            log.debug("+++process accepted");
        } catch (Exception e) {
            log.warn("--- process rejected", e);
            cache.invalidate(cookie);
            return;
        }
        log.debug("Cache put: " + cookie);
        cache.put(cookie, "");
        filterChain.continueProcessing(request, channel);
    }

    String notFound() {
        log.debug("***");
        return "###";
    }
}

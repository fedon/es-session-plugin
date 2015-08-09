package org.fedon.elasticsearch.filter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

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
    private String referer = "/exp";
    private Client client;
    private String base = "rest";
    // private SessionService service;
    final String refererAttr = "cors.session.referer.url.pattern";
    final String baseAttr = "cors.session.rest.context";

    public SessionFilter(SessionService service) {
        // this.service = service;
        referer = service.getSettings().get(refererAttr, referer);
        base = service.getSettings().get(baseAttr, base);
        ClientConfig cc = new ClientConfig();
        cc.register(ClientFilter.class);
        client = ClientBuilder.newClient(cc);
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
        // TODO cash
        log.info("-- SessionFilter.process -- " + base + " ## " + cookie + " ## " + this);
        try {
            if (cookie != null && cookie.length() > 0) {
                ClientFilter.setCookie(cookie);
            }
            proxy.checkSession();
            log.debug("+++process accepted");
        } catch (Exception e) {
            log.warn("--- process rejected", e);
            return;
        }
        filterChain.continueProcessing(request, channel);
    }
}

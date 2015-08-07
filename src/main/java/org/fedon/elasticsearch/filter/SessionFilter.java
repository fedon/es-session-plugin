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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import com.cognoscience.api.skillcollider.IntegrationREST;

/**
 * @author Dmytro Fedonin
 *
 */
public class SessionFilter extends RestFilter {
    private ESLogger log = Loggers.getLogger(this.getClass());
    private IntegrationREST client;
    // TODO build base from Referer
    static String base = "http://localhost:8080/skillcollider/rest";

    public SessionFilter() {
        ClientConfig cc = new ClientConfig();
        cc.register(ClientFilter.class);
        Client resource = ClientBuilder.newClient(cc);
        client = WebResourceFactory.newResource(IntegrationREST.class, resource.target(base));
    }

    @Override
    public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) throws Exception {
        String cookie = request.header("Kookie");
        // TODO cash
        log.debug("-- SessionFilter.process -- " + request.header("Referer") + " ## " + request.header("Kookie"));
        Method method = request.method();
        if (method == Method.OPTIONS) {
            log.info("++ options ++");
            filterChain.continueProcessing(request, channel);
            return;
        }
        try {
            if (cookie != null && cookie.length() > 0) {
                ClientFilter.setCookie(cookie);
            }
            client.checkSession();
            log.debug("+++process accepted");
        } catch (Exception e) {
            log.warn("--- process rejected", e);
            return;
        }
        filterChain.continueProcessing(request, channel);
    }
}

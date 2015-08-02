package org.fedon.elasticsearch.filter;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;

/**
 * @author Dmytro Fedonin
 *
 */
public class SessionFilter extends RestFilter {
    private ESLogger log = Loggers.getLogger(this.getClass());

    @Override
    public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) throws Exception {
        log.info("-- SessionFilter.process --");
        filterChain.continueProcessing(request, channel);
    }
}

package org.fedon.elasticsearch.plugin;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.fedon.elasticsearch.filter.SessionFilter;

/**
 * @author Dmytro Fedonin
 *
 */
public class SessionService extends AbstractLifecycleComponent<SessionService> {
    private ESLogger log = Loggers.getLogger(this.getClass());
    private final Settings settings;
    private final RestController restController;

    /**
     * @param settings
     */
    @Inject
    protected SessionService(Settings settings, RestController restController) {
        super(settings);
        this.settings = settings;
        this.restController = restController;
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        log.info("== SessionService.doStart ==");
        restController.registerFilter(new SessionFilter(this));
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doClose() throws ElasticsearchException {
        // TODO Auto-generated method stub

    }

    public Settings getSettings() {
        return settings;
    }

    public RestController getRestController() {
        return restController;
    }
}

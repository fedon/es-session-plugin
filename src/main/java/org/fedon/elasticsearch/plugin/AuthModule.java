package org.fedon.elasticsearch.plugin;

import org.elasticsearch.common.inject.AbstractModule;

/**
 * @author Dmytro Fedonin
 *
 */
public class AuthModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SessionService.class).asEagerSingleton();
    }
}

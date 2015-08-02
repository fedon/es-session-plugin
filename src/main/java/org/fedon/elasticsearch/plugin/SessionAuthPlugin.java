package org.fedon.elasticsearch.plugin;

import java.util.Collection;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;

/**
 * @author Dmytro Fedonin
 *
 */
public class SessionAuthPlugin extends AbstractPlugin {

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        return ImmutableList.<Class<? extends LifecycleComponent>> of(SessionService.class);
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        return ImmutableList.<Class<? extends Module>> of(AuthModule.class);
    }

    @Override
    public String name() {
        return "cors-session-auth";
    }

    @Override
    public String description() {
        return "Auth es call by cors Web Application";
    }
}

package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.ApplicationInitializedEvent;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class ComponentTracker {
    private final Logger LOG = Logger.getLogger(getClass().getName());
    private OpenNMSEventManager m_eventManager;
    private Set<String> m_expectedComponents = new HashSet<String>();

    public ComponentTracker(final OpenNMSEventManager eventManager) {
        m_eventManager = eventManager;
    }

    public void track(final Class<?> clazz) {
        LOG.warning("ComponentTracker: Watching for " + clazz.getName() + " initialization.");
        m_expectedComponents.add(clazz.getName());
    }

    public void ready(final Class<?> clazz) {
        LOG.warning("ComponentTracker: Component " + clazz.getName() + " is ready.");
        m_expectedComponents.remove(clazz.getName());
        onReady();
    }

    private void onReady() {
        if (m_expectedComponents.size() == 0) {
            LOG.warning("ComponentTracker: All anticipated components are ready.  Firing ApplicationInitializedEvent.");
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override public void execute() {
                    m_eventManager.fireEvent(new ApplicationInitializedEvent());
                }
            });
        } else {
            LOG.warning("ComponentTracker: Waiting for " + m_expectedComponents.size() + " more components to initialize.");
            LOG.info("ComponentTracker: Components remaining: " + m_expectedComponents);
        }
    }

}

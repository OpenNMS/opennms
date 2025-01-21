package org.opennms.features.datachoices.internal.usagestatistics;


import org.opennms.features.datachoices.internal.StateManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;


public class SuccessLoginEventListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(SuccessLoginEventListener.class);

    private final StateManager m_stateManager;
    private final EventSubscriptionService eventSubscriptionService;

    public SuccessLoginEventListener(EventSubscriptionService eventSubscriptionService, StateManager stateManager) {
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
        this.m_stateManager = Objects.requireNonNull(stateManager);
    }

    @Override
    public void onEvent(IEvent event) {
        if(EventConstants.AUTHENTICATION_SUCCESS_UEI.equals(event.getUei())) {
            try {
                if (Boolean.TRUE.equals(m_stateManager.isEnabled())) {
                    String username = event.getParm("user").getValue().getContent();
                    if (!username.equals("rtc")) {
                        CsvLogger.logToCsv(username);
                    }
                }
            } catch (IOException e) {
                LOG.warn("Failed check opt-in status. Assuming user opted out.", e);
            }
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void init() {
            eventSubscriptionService.addEventListener(this);
    }

    public void destroy() {
        eventSubscriptionService.removeEventListener(this);
    }

}

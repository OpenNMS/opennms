package org.opennms.features.datachoices.internal.usagestatistics;


import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;

import java.util.Objects;


public class SuccessLoginEventListener implements EventListener {

    private final EventSubscriptionService eventSubscriptionService;

    public SuccessLoginEventListener(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
    }

    @Override
    public void onEvent(IEvent event) {
        if(EventConstants.AUTHENTICATION_SUCCESS_UEI.equals(event.getUei())) {

            String username = event.getParm("user").getValue().getContent();
            if (!username.equals("rtc")) {
                CsvLogger.logToCsv(username, event.getTime());
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

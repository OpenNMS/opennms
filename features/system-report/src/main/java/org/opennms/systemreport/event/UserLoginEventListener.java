package org.opennms.systemreport.event;

import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class UserLoginEventListener implements EventListener {

    private EventSubscriptionService eventSubscriptionService;

    @Override
    public void onEvent(IEvent event) {
        if(EventConstants.AUTHENTICATION_SUCCESS_UEI.equals(event.getUei())) {

            String username = event.getParm("user").getValue().getContent();
            if (!username.equals("rtc")) {
                CsvUtils.logUserDataToCsv(username, event.getTime());
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


    public EventSubscriptionService getEventSubscriptionService() {
        return eventSubscriptionService;
    }

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

}

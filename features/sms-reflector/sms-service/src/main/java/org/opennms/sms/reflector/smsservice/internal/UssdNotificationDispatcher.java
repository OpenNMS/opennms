/**
 * 
 */
package org.opennms.sms.reflector.smsservice.internal;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.IUSSDNotification;
import org.smslib.USSDResponse;

/**
 * @author jeffg
 *
 */
public class UssdNotificationDispatcher implements IUSSDNotification {

    private static Logger log = LoggerFactory.getLogger(UssdNotificationDispatcher.class);

    private Collection<IUSSDNotification> m_listenerList;

    public UssdNotificationDispatcher() {
    }

    public UssdNotificationDispatcher(List<IUSSDNotification> listeners){
        m_listenerList = listeners;
    }

    public void process(String gatewayId, USSDResponse msg) {

        log.debug( "Forwarding message to registered listeners: " + getListeners() + " : " + msg );

        for( IUSSDNotification listener : getListeners() )
        {
            if (listener != this) {
                listener.process( gatewayId, msg );
            }
        }

    }

    private Collection<IUSSDNotification> getListeners() {
        return m_listenerList;
    }

    public void setListenerList(List<IUSSDNotification> list){
        m_listenerList = list;
    }

}

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
 * <p>UssdNotificationDispatcher class.</p>
 *
 * @author jeffg
 * @version $Id: $
 */
public class UssdNotificationDispatcher implements IUSSDNotification {

    private static Logger log = LoggerFactory.getLogger(UssdNotificationDispatcher.class);

    private Collection<IUSSDNotification> m_listenerList;

    /**
     * <p>Constructor for UssdNotificationDispatcher.</p>
     */
    public UssdNotificationDispatcher() {
    }

    /**
     * <p>Constructor for UssdNotificationDispatcher.</p>
     *
     * @param listeners a {@link java.util.List} object.
     */
    public UssdNotificationDispatcher(List<IUSSDNotification> listeners){
        m_listenerList = listeners;
    }

    /** {@inheritDoc} */
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

    /**
     * <p>setListenerList</p>
     *
     * @param list a {@link java.util.List} object.
     */
    public void setListenerList(List<IUSSDNotification> list){
        m_listenerList = list;
    }

}

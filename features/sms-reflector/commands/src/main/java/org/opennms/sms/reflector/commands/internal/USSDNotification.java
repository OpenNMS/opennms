
/**
 * <p>USSDNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.sms.reflector.commands.internal;

import org.opennms.core.utils.ThreadCategory;
import org.smslib.IUSSDNotification;
import org.smslib.USSDResponse;
public class USSDNotification implements IUSSDNotification {
    /** {@inheritDoc} */
    public void process(String gatewayId, USSDResponse ussdResponse) {
        debugf(">>> Inbound USSD detected from gateway %s : %s",  gatewayId, ussdResponse.getContent());
        debugf(">>> USSD session status: %s", ussdResponse.getSessionStatus());
    }
    
    private void debugf(String format, Object ...args){
        ThreadCategory log = ThreadCategory.getInstance(USSDNotification.class);
        
        if(log.isDebugEnabled()){
            log.debug(String.format(format, args));
        }
    }
    
}

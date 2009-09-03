/**
 * 
 */
package org.opennms.sms.reflector.commands.internal;

import org.smslib.IUSSDNotification;
import org.smslib.USSDResponse;

public class USSDNotification implements IUSSDNotification {
    public void process(String gatewayId, USSDResponse ussdResponse) {
        System.out.println(">>> Inbound USSD detected from gateway " + gatewayId + ": " + ussdResponse.getContent());
        System.out.println(">>> USSD session status: " + ussdResponse.getSessionStatus());
    }
    
}
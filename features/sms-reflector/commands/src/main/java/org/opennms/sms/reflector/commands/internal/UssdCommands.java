package org.opennms.sms.reflector.commands.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.smslib.USSDRequest;

/**
 * Public API representing an example OSGi service
 *
 * @author ranger
 * @version $Id: $
 */
public class UssdCommands implements CommandProvider
{
    private SmsService m_service;
    private USSDNotification m_ussdNotification;
    
    /**
     * <p>setService</p>
     *
     * @param svc a {@link org.opennms.sms.reflector.smsservice.SmsService} object.
     */
    public void setService(SmsService svc) {
        m_service = svc;
    }
    
    /**
     * <p>_ussdSend</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     */
    public void _ussdSend(CommandInterpreter intp) {
        String data = intp.nextArgument();
        String gwId = intp.nextArgument();
        
        if (data == null || gwId == null) {
            intp.println("usage: ussdSend <data> <gatewayID>");
        }
        intp.println("Data is : " + data);
        intp.println("Gateway ID is : " + gwId);
        
        USSDRequest req = new USSDRequest(data);
        req.setGatewayId(gwId);
        intp.println("USSD raw request: " + req.toString());

        try {
            m_service.sendUSSDRequest(req, gwId);
        } catch (Exception e) {
            intp.println("Exception sending USSD request: " + e.getMessage());
            intp.printStackTrace(e);
        }
    }
    
    /**
     * <p>getHelp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---USSD Commands---");
        buffer.append("\n\t").append("ussdSend <data> <gatewayID>");
        buffer.append("\n");
        return buffer.toString(); 
    } 

}


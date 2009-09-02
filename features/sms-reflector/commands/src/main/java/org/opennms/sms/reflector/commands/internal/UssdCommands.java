package org.opennms.sms.reflector.commands.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.smslib.Service;
import org.smslib.USSDRequest;

/**
 * Public API representing an example OSGi service
 */
public class UssdCommands implements CommandProvider
{
    private Service m_service;
    
    public void _ussdSend(CommandInterpreter intp) {
        String data = intp.nextArgument();
        String gwId = intp.nextArgument();
        
        if (data == null || gwId == null) {
            intp.println("usage: ussdSend <data> <gatewayID>");
        }
        intp.println("Data is : " + data);
        intp.println("Gateway ID is : " + gwId);
        
        USSDRequest req = new USSDRequest(data);
        intp.println("USSD raw request: " + req.toString());

        try {
            m_service.sendUSSDRequest(req, gwId);
        } catch (Exception e) {
            intp.println("Exception sending USSD request: " + e.getMessage());
            intp.printStackTrace(e);
        }
    }
    
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---USSD Commands---");
        buffer.append("\n\t").append("ussdSend <data> <gatewayID>");
        buffer.append("\n");
        return buffer.toString(); 
    } 

}


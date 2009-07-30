package org.opennms.sms.reflector.commands.internal;

import java.util.Enumeration;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.modem.ModemGateway;
import org.smslib.modem.SerialModemGateway;

/**
 * Public API representing an example OSGi service
 */
public class SmsCommands implements CommandProvider
{
    private BundleContext m_context;
    
    public SmsCommands(BundleContext context) {
        m_context = context;
    }
    
    
    public Object _smsSend(CommandInterpreter intp) {
        String port = intp.nextArgument();
        if (port == null) {
            intp.print("usage: smsSend <port> <phonenumber> <msg>");
            return null;
        }
        String phoneno = intp.nextArgument();
        if (phoneno == null) {
            intp.print("usage: smsSend <port> <phonenumber> <msg>");
            return null;
        }
        String msgText = intp.nextArgument();
        if (msgText == null) {
            intp.print("usage: smsSend <port> <phonenumber> <msg>");
            return null;
        }
        
        intp.println("Port is : " + port);
        intp.println("Phone is : " + phoneno);
        intp.println("Message Text is : " + msgText);
        
        Service srv = null;
        try {
            OutboundMessage msg;
            OutboundNotification outboundNotification = new OutboundNotification();
            System.out.println("Example: Send message from a serial gsm modem.");
            System.out.println(Library.getLibraryDescription());
            System.out.println("Version: " + Library.getLibraryVersion());
            srv = new Service();
            SerialModemGateway gateway = new SerialModemGateway("modem."+port, port, 57600, "SonyEricsson", "W780i");
            gateway.setInbound(true);
            gateway.setOutbound(true);
            srv.setOutboundNotification(outboundNotification);
            srv.addGateway(gateway);
            srv.startService();

            printGatewayInfo(gateway);

            // Send a message synchronously.
            msg = new OutboundMessage(phoneno, msgText);
            // msg = new OutboundMessage("+19194125045",
            // "If you can read this then I got SMSLib to work from my mac!");
            srv.sendMessage(msg);
            System.out.println(msg);
            
            Thread.sleep(2000);
            
        } catch (Exception e) {
            intp.println("Exception Stopping Sending Message: ");
            intp.printStackTrace(e);

        } finally {
            if (srv != null) try {
                srv.stopService();
            } catch (Exception e) {
                intp.println("Exception Stopping Service Occurred: ");
                intp.printStackTrace(e);

            }
        }

        return null;
   } 

    public Object _listPorts(CommandInterpreter intp) { 
        Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();
        
        while(commPorts.hasMoreElements()) {
            CommPortIdentifier commPort = commPorts.nextElement();
            System.err.println(commPort.getName());
        }

        return null; 
   } 

   public String getHelp() { 
       StringBuffer buffer = new StringBuffer(); 
       buffer.append("---Sms Commands---\n\t"); 
       buffer.append("listPorts\n\t"); 
       buffer.append("smsSend <modemPort> <phonenumber> <text>\n\t"); 
       return buffer.toString(); 
   } 

   public class OutboundNotification implements IOutboundMessageNotification {
       public void process(String gatewayId, OutboundMessage msg) {
           System.out.println("Outbound handler called from Gateway: "
                   + gatewayId);
           System.out.println(msg);
       }
   }

   private void printGatewayInfo(AGateway gw) throws Exception {
       System.out.println();
       System.out.println(gw);
       if (gw instanceof ModemGateway) {
           ModemGateway gateway = (ModemGateway) gw;
           System.out.println();
           System.out.println("Modem Information:");
           System.out.println("  Manufacturer: " + gateway.getManufacturer());
           System.out.println("  Model: " + gateway.getModel());
           System.out.println("  Serial No: " + gateway.getSerialNo());
           System.out.println("  SIM IMSI: " + gateway.getImsi());
           System.out.println("  Signal Level: " + gateway.getSignalLevel() + "%");
           System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
           System.out.println();
       }
   }


}


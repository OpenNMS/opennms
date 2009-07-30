package org.opennms.sms.reflector.commands.internal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.smslib.AGateway;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;
import org.smslib.crypto.AESKey;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.modem.ModemGateway;
import org.smslib.modem.SerialModemGateway;

/**
 * Public API representing an example OSGi service
 */
public class SmsCommands implements CommandProvider
{
	private String m_port;
    private BundleContext m_context;
    
    public SmsCommands(BundleContext context) {
        m_context = context;
    }
    
    
    public Object _smsSend(CommandInterpreter intp) {
        //String port = intp.nextArgument();
        if (m_port == null) {
            intp.print("please initialize port usage: initializePort <port>");
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
        
        intp.println("Port is : " + m_port);
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
            SerialModemGateway gateway = new SerialModemGateway("modem."+m_port, m_port, 57600, "SonyEricsson", "W780i");
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
    
    public Object _checkMessages(CommandInterpreter intp){
    	if (m_port == null) {
            intp.print("please initialize port usage: initializePort <port>");
            return null;
        }
    	
    	List<InboundMessage> msgList;
    	
    	InboundNotification inboundNotification = new InboundNotification();
    	
    	CallNotification callNotification = new CallNotification();
    	
    	GatewayStatusNotification statusNotification = new GatewayStatusNotification();
    	
    	Service srv = null;
    	try{
    		System.out.println("Example: Read messages from a serial gsm modem");
    		System.out.println(Library.getLibraryDescription());
    		System.out.println("Version: " + Library.getLibraryVersion());
    		
    		srv = new Service();
    		
    		SerialModemGateway gateway = new SerialModemGateway("modem."+ m_port, m_port, 57600, "SonyEricsson", "W780i");
    		gateway.setProtocol(Protocols.PDU);
    		gateway.setInbound(true);
    		gateway.setOutbound(true);
    		gateway.setSimPin("0000");
    		
    		srv.setInboundNotification(inboundNotification);
    		srv.setCallNotification(callNotification);
    		srv.setGatewayStatusNotification(statusNotification);
    		
    		srv.addGateway(gateway);
    		
    		srv.startService();
    		
    		printGatewayInfo(gateway);
    		
    		msgList = new ArrayList<InboundMessage>();
    		srv.readMessages(msgList, MessageClasses.UNREAD);
    		
    		for(InboundMessage msg : msgList)
    			System.out.println(msg);
    		
    		System.out.println("Now Sleeping - Hit <enter> to stop service.");
    		System.in.read(); System.in.read();
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally{
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
    
    public Object _initializePort(CommandInterpreter intp){
    	String port = intp.nextArgument();
    	
    	if(port == null){
    		intp.print("please initialize port usage: initializePort <port>");
    		return null;
    	}
    	
    	Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();
        
        while(commPorts.hasMoreElements()) {
            CommPortIdentifier commPort = commPorts.nextElement();
            if(port.equals(commPort.getName())){
            	m_port = port;
            	System.err.println("the port has been initialized as: " + port);
            }
            //System.err.println(commPort.getName());
        }
    	
    	return null;
    }

   public String getHelp() { 
       StringBuffer buffer = new StringBuffer(); 
       buffer.append("---Sms Commands---\n\t");
       buffer.append("initializePort <modemPort>\n\t");
       buffer.append("listPorts\n\t"); 
       buffer.append("smsSend <modemPort> <phonenumber> <text>\n\t"); 
       buffer.append("checkMessages\n\t");
       return buffer.toString(); 
   } 

   public class OutboundNotification implements IOutboundMessageNotification {
       public void process(String gatewayId, OutboundMessage msg) {
           System.out.println("Outbound handler called from Gateway: "
                   + gatewayId);
           System.out.println(msg);
       }
   }
   
   public class InboundNotification implements IInboundMessageNotification{

	   public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) {
		   if(msgType == MessageTypes.INBOUND) System.out.println(">>> New Inbound message detected from Gateway: " + gatewayId);
		   else if(msgType == MessageTypes.STATUSREPORT) System.out.println(">>> New Inbound Status Report message detected from Gateway: " + gatewayId);
		   System.out.println(msg);
		   
	   }
	   
   }
   
   public class CallNotification implements ICallNotification{

	   public void process(String gatewayId, String callerId) {
		   System.out.println(">>> New called detected from Gateway: " + gatewayId + " : " + callerId);
	   }
	   
   }
   
   public class GatewayStatusNotification implements IGatewayStatusNotification{

	   public void process(String gatewayId, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
		   System.out.println(">>> Gateway Status change from: " + gatewayId + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
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


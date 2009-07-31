package org.opennms.sms.reflector.commands.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.framework.internal.core.FrameworkCommandInterpreter;
import org.osgi.framework.BundleContext;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.TimeoutException;
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
    
    private Service m_service;
    private OutboundNotification m_outboundNotification;
    private InboundNotification m_inboundNotification;
    private CallNotification m_callNotification;
    private GatewayStatusNotification m_gatewayStatusNotification;
    
    public SmsCommands() {
        
    }
    
    public void stopService(){
    	if (m_service != null){
    		try {
    			m_service.stopService();
    		} catch (Exception e) {
	          System.out.println("Exception Stopping Service Occurred: ");
	          //intp.printStackTrace(e);
	          e.printStackTrace();
    		}
    	}
    }
    
    public Object _smsSend(CommandInterpreter intp) {
        //String port = intp.nextArgument();
        
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
        
        intp.println("Phone is : " + phoneno);
        intp.println("Message Text is : " + msgText);
        
        try {
            OutboundMessage msg;
            
            System.out.println("Example: Send message from a serial gsm modem.");
            System.out.println(Library.getLibraryDescription());
            System.out.println("Version: " + Library.getLibraryVersion());
            
            // Send a message synchronously.
            msg = new OutboundMessage(phoneno, msgText);
            // msg = new OutboundMessage("+19194125045",
            // "If you can read this then I got SMSLib to work from my mac!");
            m_service.sendMessage(msg);
            intp.println(msg);
            
            Thread.sleep(2000);
            
        } catch (Exception e) {
            intp.println("Exception Stopping Sending Message: ");
            intp.printStackTrace(e);

        } 

        return null;
   }
    
    public Object _checkMessages(CommandInterpreter intp){
    	
    	List<InboundMessage> msgList;
    	
    	try{
//    		System.out.println("Example: Read messages from a serial gsm modem");
//    		System.out.println(Library.getLibraryDescription());
//    		System.out.println("Version: " + Library.getLibraryVersion());
    		
    		msgList = new ArrayList<InboundMessage>();
    		m_service.readMessages(msgList, MessageClasses.UNREAD);
    		
    		for(InboundMessage msg : msgList)
    			intp.println(msg);
    		
    	}catch(Exception e){
    		intp.printStackTrace(e);
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
    	
    	try{
	    	m_outboundNotification = new OutboundNotification();
	    	m_inboundNotification = new InboundNotification();
	    	m_callNotification = new CallNotification();
	    	m_gatewayStatusNotification = new GatewayStatusNotification();
	        
	        m_service = new Service();
	        SerialModemGateway gateway = new SerialModemGateway("modem."+ port, port, 57600, "SonyEricsson", "W760");
	        gateway.setProtocol(Protocols.PDU);
	        gateway.setInbound(true);
	        gateway.setOutbound(true);
	        gateway.setSimPin("0000");
	        
	        m_service.setOutboundNotification(m_outboundNotification);
	        m_service.setInboundNotification(m_inboundNotification);
	        m_service.setCallNotification(m_callNotification);
	        m_service.setGatewayStatusNotification(m_gatewayStatusNotification);
	        m_service.addGateway(gateway);
	        m_service.startService();
	        
	        printGatewayInfo(gateway, intp);
    	
    	}catch(Exception e){
    		intp.printStackTrace(e);
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
		   if(msgType == MessageTypes.INBOUND){
			   System.out.println(">>> New Inbound message detected from Gateway: " + gatewayId);
		   }else if(msgType == MessageTypes.STATUSREPORT){
			   System.out.println(">>> New Inbound Status Report message detected from Gateway: " + gatewayId);
		   }
		   
		   System.out.println("msg text: " + msg.getText());
//		   try {
		   	//deletes the message
//			m_service.deleteMessage(msg);
//		   }catch(Exception e){
//			   e.printStackTrace();
//		   }
		   
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

   private void printGatewayInfo(AGateway gw, CommandInterpreter intp) throws Exception {
	   intp.println();
	   intp.println(gw);
       if (gw instanceof ModemGateway) {
           ModemGateway gateway = (ModemGateway) gw;
           intp.println();
           intp.println("Modem Information:");
           intp.println("  Manufacturer: " + gateway.getManufacturer());
           intp.println("  Model: " + gateway.getModel());
           intp.println("  Serial No: " + gateway.getSerialNo());
           intp.println("  SIM IMSI: " + gateway.getImsi());
           intp.println("  Signal Level: " + gateway.getSignalLevel() + "%");
           intp.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
           intp.println();
       }
   }


}


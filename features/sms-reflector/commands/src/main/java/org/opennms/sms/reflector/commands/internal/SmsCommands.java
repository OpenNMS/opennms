package org.opennms.sms.reflector.commands.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.sms.ping.SmsPinger;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.smslib.AGateway;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Library;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.USSDRequest;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.modem.ModemGateway;
import org.smslib.modem.SerialModemGateway;
import org.springframework.osgi.context.BundleContextAware;

/**
 * Public API representing an example OSGi service
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsCommands implements CommandProvider, BundleContextAware
{
    private String m_port;
    private Service m_service;
    private OutboundNotification m_outboundNotification;
    private InboundNotification m_inboundNotification;
    private CallNotification m_callNotification;
    private GatewayStatusNotification m_gatewayStatusNotification;
    private ConfigurationAdmin m_configAdmin;
    private BundleContext m_context;

    /**
     * <p>Constructor for SmsCommands.</p>
     *
     * @param configAdmin a {@link org.osgi.service.cm.ConfigurationAdmin} object.
     */
    public SmsCommands(ConfigurationAdmin configAdmin) {
        m_configAdmin = configAdmin;
    }

    /**
     * <p>stopService</p>
     */
    public void stopService(){
        if (m_service != null){
            try {
                m_service.stopService();
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "Exception Stopping Service Occurred");
            }
        }
    }

    /**
     * <p>smsSend</p>
     *
     * @param msg a {@link org.smslib.OutboundMessage} object.
     */
    public void smsSend(final OutboundMessage msg){
        try{
            m_service.sendMessage(msg);
        }catch(final Exception e){
            LogUtils.debugf(this, e, "error sending message (%s)", msg);
        }
    }

    /**
     * <p>checkMessages</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<InboundMessage> checkMessages(){
        List<InboundMessage> msgList = new ArrayList<InboundMessage>();
        try{
            m_service.readMessages(msgList, MessageClasses.UNREAD);

        }catch(final Exception e){
            LogUtils.warnf(this, e, "unable to check messages");
        }

        return msgList;
    }

    /**
     * <p>_smsPing</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object _smsPing(CommandInterpreter intp) {
        try {
            Long latency = SmsPinger.ping(intp.nextArgument());

            if(latency == null){
                intp.println("Ping Timedout");
            }else{
                intp.println("Ping roundtrip time: " + latency);
            }

        } catch (final Exception e) {
            intp.printStackTrace(e);
        }
        return null;
    }

    /**
     * <p>_smsSend</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
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

            debugf("Example: Send message from a serial gsm modem.");
            debugf(Library.getLibraryDescription());
            debugf("Version: %s",Library.getLibraryVersion());

            // Send a message synchronously.
            msg = new OutboundMessage(phoneno, msgText);
            // msg = new OutboundMessage("+19194125045",
            // "If you can read this then I got SMSLib to work from my mac!");
            m_service.sendMessage(msg);
            intp.println(msg);

            Thread.sleep(2000);

        } catch (Exception e) {
            intp.println("Exception Sending Message: ");
            intp.printStackTrace(e);
        } 

        return null;
    }
    
    /**
     * <p>unused_ussdSend</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     */
    public void unused_ussdSend(CommandInterpreter intp) {
        String data = intp.nextArgument();
        String gwId = intp.nextArgument();
        
        if (data == null || gwId == null) {
            intp.println("usage: ussdSend <data> <gatewayID>");
        }
        intp.println("Data is : " + data);
        intp.println("Gateway ID is : " + gwId);
        
        USSDRequest req = new USSDRequest(data);
        intp.println("USSD request to send: " + req.toString());

        if (m_service == null) {
            intp.println("Service object is null, cannot send");
            return;
        }
        try {
            m_service.sendUSSDRequest(req, gwId);
        } catch (Exception e) {
            intp.println("Exception sending USSD request: " + e.getMessage());
            intp.printStackTrace(e);
        }
    }

    /**
     * <p>_checkMessages</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
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

    /**
     * <p>_listPorts</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object _listPorts(CommandInterpreter intp) { 
        Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();

        while(commPorts.hasMoreElements()) {
            CommPortIdentifier commPort = commPorts.nextElement();
            debugf(commPort.getName());
        }

        return null; 
    }

    /**
     * <p>listPorts</p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration<CommPortIdentifier> listPorts(){
        return CommPortIdentifier.getPortIdentifiers();
    }

    /**
     * <p>_initializePort</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
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
            m_port = port;

        }catch(Exception e){
            intp.printStackTrace(e);
        }
        return null;
    }

    /**
     * <p>_debug</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object _debug(CommandInterpreter intp) {
        intp.println( "m_configAdmin is " + m_configAdmin );



        return null;
    }

    /**
     * <p>_showConfigs</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object _showConfigs(CommandInterpreter intp) {
        try {
            Configuration[] configs = m_configAdmin.listConfigurations(null);
            if (configs == null) {
                intp.println("No configurations found.");
            }
            else {
                for(Configuration config : configs) {
                    intp.printDictionary(config.getProperties(), "PID: "+config.getPid());
                }
            }
        }
        catch (Exception e) {
            intp.printStackTrace(e);
        }

        return null;
    }

    /**
     * <p>_configureSmsService</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object _configureSmsService(CommandInterpreter intp) {

        try {

            String id = intp.nextArgument();
            String port = intp.nextArgument();
            String baudRate = intp.nextArgument();
            String manufacturer = intp.nextArgument();
            String model = intp.nextArgument();
            String usage = intp.nextArgument();

            GatewayGroupImpl gatewayGroup = new GatewayGroupImpl();
            List<AGateway> gateways = new ArrayList<AGateway>();

            SerialModemGateway gateway = new SerialModemGateway("modem." + id, port, new Integer(baudRate), manufacturer, model);
            gateway.setProtocol(Protocols.PDU);
            gateway.setInbound(true);
            gateway.setOutbound(true);
            gateway.setSimPin("0000");

            gateways.add(gateway);

            gatewayGroup.setGateways(gateways.toArray(new AGateway[0]));

            Properties properties = new Properties();
            properties.put("gatewayUsageType", usage);

            getBundleContext().registerService(GatewayGroup.class.getName(), gatewayGroup, properties);
        }
        catch(Exception e) {
            intp.printStackTrace(e);
        }

        return null;
    }

    /**
     * <p>_paxLog</p>
     *
     * @param intp a {@link org.eclipse.osgi.framework.console.CommandInterpreter} object.
     * @return a {@link java.lang.Object} object.
     */
    @SuppressWarnings("unchecked")
    public Object _paxLog(CommandInterpreter intp) {

        try {

            String level = intp.nextArgument();

            String prefix = intp.nextArgument();

            Configuration config = m_configAdmin.getConfiguration("org.ops4j.pax.logging", null);
            Dictionary properties = config.getProperties();
            if (level == null) {
                if (properties == null) {
                    intp.println("Not current configuration");
                } else {
                    intp.printDictionary(properties, "Current Configuration");
                }
                return null;
            }

            if (properties == null) {
                intp.println("Creating a new configuraiton");
                properties = new Properties();
                properties.put("log4j.rootLogger", "DEBUG, A1");
                properties.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
                properties.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
                properties.put("log4j.appender.A1.layout.ConversionPattern", "%-4r [%t] %-5p %c %x - %m%n");
            } else {
                intp.println("Found an existing configuration");
                intp.printDictionary(properties, "Existing");
            }

            if (prefix == null) {
                intp.println("Setting default config to "+level);
                properties.put("log4j.rootLogger", level+", A1");
            } else {
                intp.println("Setting log level for "+prefix+" to "+level);
                properties.put("log4j.logger."+prefix, level);
            }


            intp.println("Setting new log configuration");
            intp.printDictionary(properties, "New");
            config.update(properties);

        } 
        catch (Exception e) {
            intp.printStackTrace(e);
        }

        return null;
    }

    /**
     * <p>getHelp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHelp() { 
        StringBuffer buffer = new StringBuffer(); 
        buffer.append("---Sms Commands---");
        buffer.append("\n\t").append("debug");
        buffer.append("\n\t").append("checkMessages");
        buffer.append("\n\t").append("configureSmsService <modemId> <port> <baudRate> <manufacturer> <model> <usage>");
        buffer.append("\n\t").append("initializePort <modemPort>");
        buffer.append("\n\t").append("listPorts"); 
        buffer.append("\n\t").append("paxLog ERROR|WARN|INFO|DEBUG [prefix]"); 
        buffer.append("\n\t").append("smsSend <phonenumber> <text>"); 
        buffer.append("\n");
        return buffer.toString(); 
    } 

    public class OutboundNotification implements IOutboundMessageNotification {
        public void process(String gatewayId, OutboundMessage msg) {
            debugf("Outbound handler called from Gateway: %s", gatewayId);
            debugf(msg.toString());
        }

    }

    public class InboundNotification implements IInboundMessageNotification{

        public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) {
            if(msgType == MessageTypes.INBOUND){
                debugf(">>> New Inbound message detected from Gateway: %s", gatewayId);
            }else if(msgType == MessageTypes.STATUSREPORT){
                debugf(">>> New Inbound Status Report message detected from Gateway: %s", gatewayId);
            }

            debugf("msg text: %s", msg.getText());

        }

    }

    public class CallNotification implements ICallNotification{

        public void process(String gatewayId, String callerId) {
            debugf(">>> New called detected from Gateway: %s : %s", gatewayId, callerId);
        }

    }

    public class GatewayStatusNotification implements IGatewayStatusNotification{

        public void process(String gatewayId, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
            debugf(">>> Gateway Status change from: %s, OLD:  %s -> NEW: %s", gatewayId, oldStatus, newStatus );
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

    /** {@inheritDoc} */
    public void setBundleContext(BundleContext m_context) {
        this.m_context = m_context;
    }

    /**
     * <p>getBundleContext</p>
     *
     * @return a {@link org.osgi.framework.BundleContext} object.
     */
    public BundleContext getBundleContext() {
        return m_context;
    }
    
    private void debugf(String format, Object ...args){
        ThreadCategory log = ThreadCategory.getInstance(SmsCommands.class);
        
        if(log.isDebugEnabled()){
            log.debug(String.format(format, args));
        }
    }

}

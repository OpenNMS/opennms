/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector.commands.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.sms.ping.SmsPinger;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.smslib.*;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;
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
public class SmsCommands implements CommandProvider, BundleContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(SmsCommands.class);
    private Service m_service;
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
                LOG.debug("Exception Stopping Service Occurred", e);
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
            LOG.debug("error sending message ({})", msg, e);
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
            LOG.warn("unable to check messages", e);
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

            LOG.debug("Example: Send message from a serial gsm modem.");
            LOG.debug(Library.getLibraryDescription());
            LOG.debug("Version: {}",Library.getLibraryVersion());

            // Send a message synchronously.
            msg = new OutboundMessage(phoneno, msgText);
            // msg = new OutboundMessage("+19194125045",
            // "If you can read this then I got SMSLib to work from my mac!");
            m_service.sendMessage(msg);
            intp.println(msg);

            Thread.sleep(2000);

        } catch (Throwable e) {
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
        } catch (Throwable e) {
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

        }catch(Throwable e){
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
            LOG.debug(commPort.getName());
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
            OutboundNotification m_outboundNotification = new OutboundNotification();
            InboundNotification m_inboundNotification = new InboundNotification();
            CallNotification m_callNotification = new CallNotification();
            GatewayStatusNotification m_gatewayStatusNotification = new GatewayStatusNotification();

            m_service = Service.getInstance();
            SerialModemGateway gateway = new SerialModemGateway("modem."+ port, port, 57600, "SonyEricsson", "W760");
            gateway.setProtocol(Protocols.PDU);
            gateway.setInbound(true);
            gateway.setOutbound(true);
            gateway.setSimPin("0000");

            m_service.setOutboundMessageNotification(m_outboundNotification);
            m_service.setInboundMessageNotification(m_inboundNotification);
            m_service.setCallNotification(m_callNotification);
            m_service.setGatewayStatusNotification(m_gatewayStatusNotification);
            m_service.addGateway(gateway);
            m_service.startService();

            printGatewayInfo(gateway, intp);

        }catch(Throwable e){
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
        catch (Throwable e) {
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

            Dictionary<String,String> properties = new Hashtable<String,String>();
            properties.put("gatewayUsageType", usage);

            getBundleContext().registerService(GatewayGroup.class, gatewayGroup, properties);
        }
        catch(Throwable e) {
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
    public Object _paxLog(CommandInterpreter intp) {

        try {

            String level = intp.nextArgument();

            String prefix = intp.nextArgument();

            Configuration config = m_configAdmin.getConfiguration("org.ops4j.pax.logging", null);
            
            Dictionary<String,Object> properties = config.getProperties();
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
                properties = new Hashtable<String,Object>();
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
        catch (Throwable e) {
            intp.printStackTrace(e);
        }

        return null;
    }

    /**
     * <p>getHelp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
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
        @Override
        public void process(AGateway gateway, OutboundMessage msg) {
            LOG.debug("Outbound handler called from Gateway: {}", gateway.getGatewayId());
            LOG.debug(msg.toString());
        }
    }

    public class InboundNotification implements IInboundMessageNotification{

        @Override
        public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
            if(msgType == MessageTypes.INBOUND){
                LOG.debug(">>> New Inbound message detected from Gateway: {}", gateway.getGatewayId());
            }else if(msgType == MessageTypes.STATUSREPORT){
                LOG.debug(">>> New Inbound Status Report message detected from Gateway: {}", gateway.getGatewayId());
            }

            LOG.debug("msg text: {}", msg.getText());
        }
    }

    public class CallNotification implements ICallNotification{

        @Override
        public void process(AGateway gateway, String callerId) {
            LOG.debug(">>> New called detected from Gateway: {} : {}", gateway.getGatewayId(), callerId);
        }

    }

    public class GatewayStatusNotification implements IGatewayStatusNotification{

        @Override
        public void process(AGateway gateway, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
            LOG.debug(">>> Gateway Status change from: {}, OLD:  {} -> NEW: {}", gateway.getGatewayId(), oldStatus, newStatus );
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
    @Override
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
}

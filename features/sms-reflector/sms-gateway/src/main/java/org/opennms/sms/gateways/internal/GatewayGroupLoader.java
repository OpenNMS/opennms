package org.opennms.sms.gateways.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.AGateway.Protocols;
import org.smslib.modem.SerialModemGateway;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>GatewayGroupLoader class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayGroupLoader implements InitializingBean {
    
    private static Logger log = LoggerFactory.getLogger(GatewayGroupLoader.class); 

    private Properties m_configProperties;
    private GatewayGroup[] m_gatewayGroups;
    private GatewayGroupRegistrar m_gatewayGroupRegistrar;
    
    
    /**
     * <p>Constructor for GatewayGroupLoader.</p>
     *
     * @param gatewayGroupRegistrar a {@link org.opennms.sms.gateways.internal.GatewayGroupRegistrar} object.
     * @param configURL a {@link java.net.URL} object.
     */
    public GatewayGroupLoader(GatewayGroupRegistrar gatewayGroupRegistrar, URL configURL) {
        this(gatewayGroupRegistrar, loadProperties(configURL));
    }
    
    /**
     * <p>Constructor for GatewayGroupLoader.</p>
     *
     * @param gatewayGroupRegistrar a {@link org.opennms.sms.gateways.internal.GatewayGroupRegistrar} object.
     * @param configProperties a {@link java.util.Properties} object.
     */
    public GatewayGroupLoader(GatewayGroupRegistrar gatewayGroupRegistrar, Properties configProperties) {
        m_gatewayGroupRegistrar = gatewayGroupRegistrar;
        m_configProperties = configProperties;
    }
    
    /**
     * <p>getGatewayGroups</p>
     *
     * @return an array of {@link org.opennms.sms.reflector.smsservice.GatewayGroup} objects.
     */
    public GatewayGroup[] getGatewayGroups() {
        return m_gatewayGroups;
    }
	
    /**
     * <p>load</p>
     */
    public void load() {

        Properties modemProperties = m_configProperties;
        
        String modems = System.getProperty("org.opennms.sms.gateways.modems");
        
        if (modems == null || "".equals(modems.trim())) {
            modems = modemProperties.getProperty("modems");
        }

        String[] tokens = modems.split("\\s+");

        final AGateway[] gateways = new AGateway[tokens.length];

        if (tokens.length == 0) {
            m_gatewayGroups = new GatewayGroup[0];
        } else {
            for(int i = 0; i < tokens.length; i++){
                String modemId = tokens[i];
                String port = modemProperties.getProperty(modemId + ".port");
                
                if (port == null) {
                    throw new IllegalArgumentException("No port defined for modem with id " + modemId );
                }
                int baudRate = Integer.parseInt(modemProperties.getProperty(modemId + ".baudrate", "9600"));
                String manufacturer = modemProperties.getProperty(modemId + ".manufacturer");
                String model = modemProperties.getProperty(modemId + ".model");
                String pin = modemProperties.getProperty(modemId+".pin", "0000");

                infof("Create SerialModemGateway(%s, %s, %d, %s, %s)", modemId, port, baudRate, manufacturer, model);

                SerialModemGateway gateway = new SerialModemGateway(modemId, port, baudRate, manufacturer, model);
                gateway.setProtocol(Protocols.PDU);
                gateway.setInbound(true);
                gateway.setOutbound(true);
                gateway.setSimPin(pin);

                gateways[i] = gateway;
            }


            GatewayGroup gatewayGroup = new GatewayGroup() {

                public AGateway[] getGateways() {
                    return gateways;
                }

            };

            m_gatewayGroups  = new GatewayGroup[] { gatewayGroup };

        }

    }

    private static Properties loadProperties(URL configURL) {
        Properties modemProperties = new Properties();
        InputStream in = null;

        try{
            in = configURL.openStream();
            modemProperties.load(in);
        } catch (final IOException e) {
            LogUtils.errorf(GatewayGroupLoader.class, e, "Unable to load properties.");
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (final IOException e) {
                    LogUtils.warnf(GatewayGroupLoader.class, e, "unable to close config stream");
                }
            }
        }
        return modemProperties;
    }
	
	private void infof(String fmt, Object... args) {
	    if (log.isInfoEnabled()) {
	        log.info(String.format(fmt, args));
	    }
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void afterPropertiesSet() throws Exception {
		load();
		for(GatewayGroup group : getGatewayGroups()){
			m_gatewayGroupRegistrar.registerGatewayGroup(group);
		}
	}
}

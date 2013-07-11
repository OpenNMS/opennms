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

package org.opennms.sms.gateways.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(GatewayGroupLoader.class);
    
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

                @Override
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
            LOG.error("Unable to load properties.", e);
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (final IOException e) {
                    LOG.warn("unable to close config stream", e);
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
	@Override
	public void afterPropertiesSet() throws Exception {
		load();
		for(GatewayGroup group : getGatewayGroups()){
			m_gatewayGroupRegistrar.registerGatewayGroup(group);
		}
	}
}

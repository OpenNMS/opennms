/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.netmgt.config.tl1d.Tl1Element;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.Tl1ConfigurationDao;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

/**
 * OpenNMS TL1 Daemon!
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class Tl1d extends AbstractServiceDaemon implements PausableFiber, InitializingBean, EventListener {

    /*
     * The last status sent to the service control manager.
     */
    private volatile int m_status = START_PENDING;
    private volatile Thread m_tl1MesssageProcessor;
    private volatile EventIpcManager m_eventManager;
	private volatile Tl1ConfigurationDao m_configurationDao;

    private final BlockingQueue<Tl1AutonomousMessage> m_tl1Queue = new LinkedBlockingQueue<Tl1AutonomousMessage>();
    private final List<Tl1Client> m_tl1Clients = new ArrayList<Tl1Client>();

    /**
     * <p>Constructor for Tl1d.</p>
     */
    public Tl1d() {
        super("OpenNMS.Tl1d");
    }
	
	/**
	 * <p>setConfigurationDao</p>
	 *
	 * @param configurationDao a {@link org.opennms.netmgt.dao.Tl1ConfigurationDao} object.
	 */
	public void setConfigurationDao(Tl1ConfigurationDao configurationDao) {
	    m_configurationDao = configurationDao;
	}


    /**
     * <p>setEventManager</p>
     *
     * @param eventManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    /**
     * <p>onInit</p>
     */
    public synchronized void onInit() {
        log().info("onInit: Initializing Tl1d connections." );
    
        //initialize a factory of configuration
    
        List<Tl1Element> configElements = m_configurationDao.getElements();
    
        for(Tl1Element element : configElements) {
            try {
                Tl1Client client = (Tl1Client) Class.forName(element.getTl1ClientApi()).newInstance();
                client.setHost(element.getHost());
                client.setPort(element.getPort());
                client.setTl1Queue(m_tl1Queue);
                client.setMessageProcessor((Tl1AutonomousMessageProcessor) Class.forName(element.getTl1MessageParser()).newInstance());
                client.setLog(log());
                client.setReconnectionDelay(element.getReconnectDelay());
                m_tl1Clients.add(client);
            } catch (InstantiationException e) {
                log().error("onInit: could not instantiate specified class.", e);
            } catch (IllegalAccessException e) {
                log().error("onInit: could not access specified class.", e);
            } catch (ClassNotFoundException e) {
                log().error("onInit: could not find specified class.", e);
            }
        }

        log().info("onInit: Finished Initializing Tl1d connections.");  
    }

    /**
     * <p>onStart</p>
     */
    public synchronized void onStart() {
        log().info("onStart: Initializing Tl1d message processing." );
        m_tl1MesssageProcessor = new Thread("Tl1-Message-Processor") {
            public void run() {
                doMessageProcessing();
            }
        };

        m_tl1MesssageProcessor.start();

        for (Tl1Client client : m_tl1Clients) {
            client.start();
        }
        log().info("onStart: Finished Initializing Tl1d connections.");
    }

	/**
	 * <p>onStop</p>
	 */
	public synchronized void onStop() {
		for (Tl1Client client : m_tl1Clients) {
			client.stop();
		}
		m_tl1MesssageProcessor.interrupt();
	}


    private void processMessage(Tl1AutonomousMessage message) {
        log().debug("processMessage: Processing message: "+message);

        EventBuilder bldr = new EventBuilder(Tl1AutonomousMessage.UEI, "Tl1d");
        bldr.setHost(message.getHost());
        bldr.setInterface(message.getHost()); //interface is the IP
        bldr.setService("TL-1"); //Service it TL-1
        bldr.setSeverity(message.getId().getHighestSeverity());
        
        bldr.setTime(message.getTimestamp());
        bldr.addParam("raw-message", message.getRawMessage());
        bldr.addParam("alarm-code", message.getId().getAlarmCode());
        bldr.addParam("atag", message.getId().getAlarmTag());
        bldr.addParam("verb", message.getId().getVerb());
        bldr.addParam("autoblock", message.getAutoBlock().getBlock());
        bldr.addParam("aid",message.getAutoBlock().getAid());
        bldr.addParam("additionalParams",message.getAutoBlock().getAdditionalParams());
        
        m_eventManager.sendNow(bldr.getEvent());
        log().debug("processMessage: Message processed: "+ message);
    }


    /**
     * <p>onPause</p>
     */
    public void onPause() {
    }

    /**
     * <p>onResume</p>
     */
    public void onResume() {
    }

    /**
     * Returns the current status of the service.
     *
     * @return The service's status.
     */
    public synchronized int getStatus() {
        return m_status;
    }

    private void doMessageProcessing() {
        log().debug("doMessageProcessing: Processing messages.");
        boolean cont = true;
        while (cont ) {
            try {
                Tl1AutonomousMessage message = m_tl1Queue.take();
                processMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log().debug("doMessageProcessing: Exiting processing messages.");
    }

    /** {@inheritDoc} */
    public void onEvent(Event e) {
    }

}

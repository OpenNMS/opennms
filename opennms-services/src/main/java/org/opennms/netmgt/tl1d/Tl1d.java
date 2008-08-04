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
import org.opennms.netmgt.utils.EventBuilder;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class Tl1d extends AbstractServiceDaemon implements PausableFiber, InitializingBean {

    private static final String TL1_UEI = "uei.opennms.org/api/tl1d/message";

    /*
     * The last status sent to the service control manager.
     */
    private int m_status = START_PENDING;
    private BlockingQueue<Tl1Message> m_tl1Queue;
    private Thread m_tl1MesssageProcessor;
    private ArrayList<Tl1Client> m_tl1Clients;
    private EventIpcManager m_eventManager;
	private Tl1ConfigurationDao m_configurationDao;

    public Tl1d() {
        super("OpenNMS.Tl1d");
    }
	
	public void setConfigurationDao(Tl1ConfigurationDao configurationDao) {
	    m_configurationDao = configurationDao;
	}


    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    public synchronized void onInit() {
        log().info("onInit: Initializing Tl1d connections." );
        m_tl1Queue = new LinkedBlockingQueue<Tl1Message>();
    
        //initialize a factory of configuration
    
        List<Tl1Element> configElements = m_configurationDao.getElements();
    
        m_tl1Clients = new ArrayList<Tl1Client>();
    
        for(Tl1Element element : configElements) {
            m_tl1Clients.add(new Tl1ClientImpl(m_tl1Queue, element.getHost(), element.getPort(), log()));
        }
    
    
        log().info("onInit: Finished Initializing Tl1d connections.");  
    }

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

	public synchronized void onStop() {
		for (Tl1Client client : m_tl1Clients) {
			client.stop();
		}
		m_tl1MesssageProcessor.interrupt();
	}


    private void processMessage(Tl1Message message) {
        log().debug("processMessage: Processing message: "+message);

        EventBuilder bldr = new EventBuilder(TL1_UEI, "Tl1d");
        bldr.setHost(message.getHost());
        bldr.setTime(message.getTimestamp());
//        bldr.setSeverity(message.getSeverity());
//        bldr.setLogMessage(message.getMessage());
        
        //TODO: Work to do here, yet
        bldr.addParam("tl1message", message.getRawMessage());
        m_eventManager.sendNow(bldr.getEvent());
        log().debug("processMessage: Message processed: "+message);
    }


    public void onPause() {
    }

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
                Tl1Message message = m_tl1Queue.take();
                processMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log().debug("doMessageProcessing: Exiting processing messages.");
    }

}

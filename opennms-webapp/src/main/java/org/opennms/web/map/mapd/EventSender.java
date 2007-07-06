//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.map.mapd;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.web.map.MapsConstants;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.web.Util;

/**
 */
public class EventSender extends Object {
    protected static EventProxy m_proxy;

    protected static String m_url;

    protected static String m_username;

    protected static String m_password;

    protected static Category log = ThreadCategory.getInstance(MapsConstants.LOG4J_CATEGORY);

    private static EventSender singleton;
    
   
    
    private EventSender(){
    	//uses the same proxy of rtc
        m_proxy = Util.createEventProxy();
    }

    void sendSubscribeEvent(EventProxy proxy, String url, String username, String password) throws IllegalArgumentException, EventProxyException {
    	log.debug("Sending subscribe Event     proxy:"+proxy+  " url:"+url+"  username:"+ username+"  passwd:"+password);
        if (proxy == null || url == null || username == null || password == null) {
            log.error("Cannot take null parameters");
        	throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Event event = new Event();
        event.setSource("MapPostSubscriber");
        event.setUei(EventConstants.MAP_SUBSCRIBE_EVENT_UEI);
        event.setHost("host");
        event.setTime(EventConstants.formatToString(new java.util.Date()));

        Parms parms = new Parms();

        // URL
        Value value = new Value();
        value.setContent(url);
        Parm parm = new Parm();
        parm.setParmName(EventConstants.PARM_URL);
        parm.setValue(value);
        parms.addParm(parm);

        // User
        value = new Value();
        value.setContent(username);
        parm = new Parm();
        parm.setParmName(EventConstants.PARM_USER);
        parm.setValue(value);
        parms.addParm(parm);

        // Password
        value = new Value();
        value.setContent(password);
        parm = new Parm();
        parm.setParmName(EventConstants.PARM_PASSWD);
        parm.setValue(value);
        parms.addParm(parm);

        event.setParms(parms);

        proxy.send(event);

        log.info("Subscription requested for " + username + " to " + url);
    }
    
  
    
    void sendOpenMapEvent(EventProxy proxy, int mapId, String mapFactory) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || mapFactory==null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Event event = new Event();
        event.setSource("MapEventSender");
        event.setUei(EventConstants.OPEN_MAP_EVENT_UEI);
        event.setHost("host");
        event.setTime(EventConstants.formatToString(new java.util.Date()));

        Parms parms = new Parms();

        // Maps Factory
        Value value = new Value();
        value.setContent(mapFactory);
        Parm parm = new Parm();
        parm.setParmName(EventConstants.PARM_MAP_FACTORY);
        parm.setValue(value);
        parms.addParm(parm);

        // Map's id
        value = new Value();
        value.setContent(""+mapId);
        parm = new Parm();
        parm.setParmName(EventConstants.PARM_MAP_ID);
        parm.setValue(value);
        parms.addParm(parm);

        event.setParms(parms);

        proxy.send(event);

        log.info("Open map event for mapid="+mapId +" and mapFactory "+mapFactory+ " sent.");
    }
    
    
    void sendCloseMapEvent(EventProxy proxy, int mapId, String mapFactory) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || mapFactory==null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Event event = new Event();
        event.setSource("MapEventSender");
        event.setUei(EventConstants.CLOSE_MAP_EVENT_UEI);
        event.setHost("host");
        event.setTime(EventConstants.formatToString(new java.util.Date()));

        Parms parms = new Parms();

        // Maps Factory
        Value value = new Value();
        value.setContent(mapFactory);
        Parm parm = new Parm();
        parm.setParmName(EventConstants.PARM_MAP_FACTORY);
        parm.setValue(value);
        parms.addParm(parm);

        // Map's id
        value = new Value();
        value.setContent(""+mapId);
        parm = new Parm();
        parm.setParmName(EventConstants.PARM_MAP_ID);
        parm.setValue(value);
        parms.addParm(parm);

        event.setParms(parms);

        proxy.send(event);

        log.info("Close map event for mapid="+mapId +" and mapFactory "+mapFactory+ " sent.");
    }

    void sendUnsubscribeEvent(EventProxy proxy, String url) throws IllegalArgumentException, EventProxyException {
        if (proxy == null || url == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Event event = new Event();
        event.setSource("MapEventSender");
        event.setUei(EventConstants.MAP_UNSUBSCRIBE_EVENT_UEI);
        event.setHost("host");
        event.setTime(EventConstants.formatToString(new java.util.Date()));

        // URL
        Parms parms = new Parms();
        Value value = new Value();
        value.setContent(url);
        Parm parm = new Parm();
        parm.setParmName(EventConstants.PARM_URL);
        parm.setValue(value);
        parms.addParm(parm);

        event.setParms(parms);

        proxy.send(event);

        log.info("Unsubscription sent for " + url);
    }

    String subscribe() throws IllegalArgumentException, EventProxyException {
        sendSubscribeEvent(m_proxy, m_url, m_username, m_password);
        return (m_url);
    }



    void close() {
        m_proxy = null;
    }

    /**
     * Init the EventSender creating an EventProxy
     */
    public static void init() {
    	log = ThreadCategory.getInstance(MapsConstants.LOG4J_CATEGORY);
    	log.debug("Initializing Map's EventSender...");
    	if(singleton!=null) // already initialized
    		return;
        m_username = Vault.getProperty("opennms.map-client.http-post.username");
        m_password = Vault.getProperty("opennms.map-client.http-post.password");
        m_url = Vault.getProperty("opennms.map-client.http-post.url");
        log.debug("Initializing Map's EventSender with opennms.map-client.http-post.username "+m_username+ ", opennms.map-client.http-post.password "+m_password+ ", opennms.map-client.http-post.url "+m_url);
        if (m_url == null) {
        	log.error("Property 'opennms.map-client.http-post.url' is null");
            throw new IllegalArgumentException("Property 'opennms.map-client.http-post.url' is null");
        }
        singleton=new EventSender();
        log.debug("MapEventSender initialized: url=" + m_url + ", user=" + m_username);
    }

    /**
     * do the client subscription to server service 
     * @throws EventProxyException
     */
    public static void doSubscription()throws EventProxyException{
    	if(singleton==null)
    		throw new IllegalStateException("Call init() before.");   
    	singleton.subscribe();
            // Close the subscription JMS connection.
           // subscriber.close();
    }
    
    
    /**
     * do the client unsubscription to server service 
     * @throws EventProxyException
     */
    public static void doUnsubscription() throws IllegalArgumentException, EventProxyException {
    	if(singleton==null)
    		throw new IllegalStateException("Call init() before.");
    	singleton.sendUnsubscribeEvent(m_proxy, m_url);
    }

    /**
     * subscribe the map/mapFactory couple on server side service
     * @param mapid
     * @param mapFactory
     * @throws IllegalArgumentException
     * @throws EventProxyException
     */
    public static void subscribeMap(int mapid, String mapFactory) throws IllegalArgumentException, EventProxyException {
    	if(singleton==null)
    		throw new IllegalStateException("Call init() before.");
    	singleton.sendOpenMapEvent(m_proxy, mapid, mapFactory);
    }
    
    /**
     * unsubscribe the map/mapFactory couple from server side service
     * @param mapid
     * @param mapFactory
     * @throws IllegalArgumentException
     * @throws EventProxyException
     */
    public static void unsubscribeMap(int mapid, String mapFactory) throws IllegalArgumentException, EventProxyException {
    	if(singleton==null)
    		throw new IllegalStateException("Call init() before.");
    	singleton.sendCloseMapEvent(m_proxy, mapid, mapFactory);
    }  
    
}

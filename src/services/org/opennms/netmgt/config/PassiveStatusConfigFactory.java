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
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.passive.PassiveEvent;
import org.opennms.netmgt.config.passive.PassiveStatusConfiguration;
import org.opennms.netmgt.config.passive.StatusKey;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;

/**
 * This is the singleton class used to load the configuration from the
 * passive-status-configuration.xml. This provides convenience methods to get the configured
 * categories and their information, add/delete categories from category groups.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class PassiveStatusConfigFactory implements PassiveStatusConfig {
    /**
     * The singleton instance of this factory
     */
    private static PassiveStatusConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private PassiveStatusConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * 
     */
    private PassiveStatusConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        Reader rdr = new InputStreamReader(new FileInputStream(configFile));
        marshallReader(rdr);
        rdr.close();
    }
    
    public PassiveStatusConfigFactory(Reader rdr) throws MarshalException, ValidationException {
        marshallReader(rdr);
    }
    
    private synchronized PassiveStatusConfiguration marshallReader(Reader rdr) throws MarshalException, ValidationException {
        m_config = (PassiveStatusConfiguration) Unmarshaller.unmarshal(PassiveStatusConfiguration.class, rdr);
        return m_config;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.PASSIVE_CONFIG_FILE_NAME);

        m_singleton = new PassiveStatusConfigFactory(cfgFile.getPath());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized PassiveStatusConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("getInstance: The factory has not been initialized");

        return m_singleton;
    }

	public static void setInstance(PassiveStatusConfig singleton) {
		m_singleton=singleton;
		m_loaded=true;
	}
	
    /**
     * Return the PassiveStatus configuration.
     * 
     * @return the PassiveStatus configuration
     */
    private synchronized PassiveStatusConfiguration getConfig() {
        return m_config;
    }

    /*
     *  (non-Javadoc)
     * @see org.opennms.netmgt.config.PassiveStatusConfig#getUEIList()
     */
    public List getUEIList() {
        List passiveEvents = getConfig().getPassiveEvents().getPassiveEventCollection();
        List ueis = new ArrayList();
        for (Iterator it = passiveEvents.iterator(); it.hasNext();) {
            PassiveEvent event = (PassiveEvent) it.next();
            ueis.add(event.getUei());
        }
        return ueis;
    }
    
    public PassiveStatusValue getPassiveStatusValue(Event e) {
        PassiveStatusKey key = new PassiveStatusKey(getMatchedNodeLabel(e), getMatchedIpAddr(e), getMatchedServiceName(e));
        return new PassiveStatusValue(key, getMatchedStatus(e));
    }

    public PollStatus getMatchedStatus(Event e) {
        PassiveEvent pe = getPassiveEventByUei(e.getUei());
        String tokenValue = null;
        String eventToken = pe.getStatusKey().getStatus().getEventToken().getName();
        if (pe.getStatusKey().getNodeLabel().getEventToken().getIsParm() == true) {
            tokenValue = EventUtil.getNamedParmValue("parm["+ eventToken +"]", e);
        } else {
            tokenValue = getEventField(eventToken, e);
        }
        return PollStatus.decodePollStatus(tokenValue, e.getLogmsg().getContent());
    }

    public String getMatchedServiceName(Event e) {
        PassiveEvent pe = getPassiveEventByUei(e.getUei());
        String tokenValue = null;
        String eventToken = pe.getStatusKey().getServiceName().getEventToken().getName();
        if (pe.getStatusKey().getNodeLabel().getEventToken().getIsParm() == true) {
            tokenValue = EventUtil.getNamedParmValue("parm["+ eventToken +"]", e);
        } else {
            tokenValue = getEventField(eventToken, e);
        }
        return tokenValue;
    }

    public String getMatchedIpAddr(Event e) {
        PassiveEvent pe = getPassiveEventByUei(e.getUei());
        String tokenValue = null;
        String eventToken = pe.getStatusKey().getIpaddr().getEventToken().getName();
        if (pe.getStatusKey().getNodeLabel().getEventToken().getIsParm() == true) {
            tokenValue = EventUtil.getNamedParmValue("parm["+ eventToken +"]", e);
        } else {
            tokenValue = getEventField(eventToken, e);
        }
        return tokenValue;
    }

    /**
     * This method returns the nodelabel value by using the config to determine this value
     * based on field and parm values of a passive event.
     * 
     * @param e
     * @return
     */
    public String getMatchedNodeLabel(Event e) {
        
        PassiveEvent pe = getPassiveEventByUei(e.getUei());
        String tokenValue = null;
        String eventToken = pe.getStatusKey().getNodeLabel().getEventToken().getName();
        if (pe.getStatusKey().getNodeLabel().getEventToken().getIsParm() == true) {
            tokenValue = EventUtil.getNamedParmValue("parm["+ eventToken +"]", e);
        } else {
            tokenValue = getEventField(eventToken, e);
        }
        return tokenValue;
    }

    private String getEventField(String eventToken, Event e) {
        if (eventToken.equalsIgnoreCase("descr")) {
            return e.getDescr();
        } else if (eventToken.equalsIgnoreCase("distPoller")) {
            return e.getDistPoller();
        } else if (eventToken.equalsIgnoreCase("host")) {
            return e.getHost();
        } else if (eventToken.equalsIgnoreCase("ifAlias")) {
            return e.getIfAlias();
        } else if (eventToken.equalsIgnoreCase("interface")) {
            return e.getInterface();
        } else if (eventToken.equalsIgnoreCase("service")) {
            return e.getService();
        } else if (eventToken.equalsIgnoreCase("severity")) {
            return e.getSeverity();
        } else if (eventToken.equalsIgnoreCase("snmpHost")) {
            return e.getSnmphost();
        } else if (eventToken.equalsIgnoreCase("source")) {
            return e.getSource();
        } else if (eventToken.equalsIgnoreCase("logGroup")) {
            return e.getLogmsg().getContent();
        } else if (eventToken.equalsIgnoreCase("masterStation")) {
            return e.getMasterStation();
        } else if (eventToken.equalsIgnoreCase("mouseOverText")) {
            return e.getMouseovertext();
        } else if (eventToken.equalsIgnoreCase("operInstruct")) {
            return e.getOperinstruct();
        }
        return null;
    }

    /**
     * Use this method to verify that the event is quailified to be processed
     * by the passive status keeper.
     * 
     * @param e
     *      The event to be analyzed
     * @return
     *      true or false
     */
    public boolean isPassiveStatusEvent(Event e) {
        if (!getUEIList().contains(e.getUei()))
            return false;
        return eventContainsRequiredParms(e);
    }

    /**
     * 
     * @param e
     * @param labels
     * @return
     */
    private boolean eventContainsRequiredParms(Event e) {
        
        /*
         * First check to see if the config for this event requires parms
         */
        if (!isParmRequired(e)) {
            return true;
        }
        
        /*
         * Check to see if the parms required by the configuration are actually
         * in the event.
         */
        List passiveStatusParmNames = getPassiveStatusParmNames(e);
        Parms parms = e.getParms();
        if (parms != null && passiveStatusParmNames != null) {
            List labelList = getParmsLabels(parms);
            if (labelList.containsAll(passiveStatusParmNames))
                return true;
        }
        return false;
    }

    /**
     * Goes through the list of configured passive events looking for the configured
     * event for any status key that uses an event parm vs.
     * event field.
     *
     * @return hasParm
     *      true or false
     */
    private boolean isParmRequired(Event e) {
        PassiveEvent pe = getPassiveEventByUei(e.getUei());
        StatusKey key = pe.getStatusKey();
        boolean hasParm = false;
        if ( key.getNodeLabel().getEventToken().getIsParm() ||
                key.getIpaddr().getEventToken().getIsParm() ||
                key.getServiceName().getEventToken().getIsParm() ||
                key.getStatus().getEventToken().getIsParm())
            hasParm = true;
        return hasParm;
    }
    
    /**
     * Returns a list of parms required in the passive status configuration
     * for this event.
     * 
     * @param e
     * @return parms
     *      List containing strings representing the names a parms required
     *      for the passive status key.
     */
    private List getPassiveStatusParmNames(Event e) {
        List parms = new ArrayList();
        PassiveEvent pe = getPassiveEventByUei(e.getUei());
        StatusKey key = pe.getStatusKey();
        if (key.getNodeLabel().getEventToken().getIsParm())
            parms.add(key.getNodeLabel().getEventToken().getName());
        if (key.getIpaddr().getEventToken().getIsParm())
            parms.add(key.getIpaddr().getEventToken().getName());
        if (key.getServiceName().getEventToken().getIsParm())
            parms.add(key.getServiceName().getEventToken().getName());
        if (key.getStatus().getEventToken().getIsParm())
            parms.add(key.getStatus().getEventToken().getName());
        return parms;
    }
    
    /**
     * Get the configured passive event based on UEI.
     * @param uei
     * @return pe
     *      the configured passive event
     */
    private PassiveEvent getPassiveEventByUei(String uei) {
        PassiveEvent pe = null;
        Collection eventList = m_config.getPassiveEvents().getPassiveEventCollection();
        for (Iterator iter = eventList.iterator(); iter.hasNext();) {
            PassiveEvent event = (PassiveEvent) iter.next();
            if (event.getUei().equals(uei)) {
                pe = event;
            }
        }
        return pe;
    }
    

    private List getParmsLabels(Parms parms) {
        List labels = new ArrayList();
        Collection parmColl = parms.getParmCollection();
        for (Iterator it = parmColl.iterator(); it.hasNext();) {
            labels.add(((Parm) it.next()).getParmName());
        }
        return labels;
    }

}

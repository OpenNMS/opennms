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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
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

    /**
     * This method determines the nodelabel value by using the config to determine this value
     * based on field and parm values of a passive event.
     * 
     * @param e
     * @return The PollStatus from the event
     */
    public PollStatus getMatchedStatus(Event e) {
        String eventToken = getPassiveEventByUei(e.getUei()).getStatusKey().getStatus().getEventToken().getName();
        String expr = getPassiveEventByUei(e.getUei()).getStatusKey().getStatus().getEventToken().getValue();
        String pattern = getPassiveEventByUei(e.getUei()).getStatusKey().getStatus().getEventToken().getFormat();
        boolean isParm = getPassiveEventByUei(e.getUei()).getStatusKey().getStatus().getEventToken().getIsParm();
        
        String tokenValue = getValueFromFieldOrParm(e, eventToken, isParm);
        return PollStatus.decodePollStatus(parseExpression(tokenValue, expr, pattern), e.getLogmsg().getContent());
    }

    /**
     * This method determines the nodelabel value by using the config to determine this value
     * based on field and parm values of a passive event.
     * 
     * @param e
     * @return The correct value of the servicename portion of the status key
     */
    public String getMatchedServiceName(Event e) {
        String eventToken = getPassiveEventByUei(e.getUei()).getStatusKey().getServiceName().getEventToken().getName();
        String expr = getPassiveEventByUei(e.getUei()).getStatusKey().getServiceName().getEventToken().getValue();
        String pattern = getPassiveEventByUei(e.getUei()).getStatusKey().getServiceName().getEventToken().getFormat();
        boolean isParm = getPassiveEventByUei(e.getUei()).getStatusKey().getServiceName().getEventToken().getIsParm();
        
        String tokenValue = getValueFromFieldOrParm(e, eventToken, isParm);
        return parseExpression(tokenValue, expr, pattern);
    }

    /**
     * This method determines the nodelabel value by using the config to determine this value
     * based on field and parm values of a passive event.
     * 
     * @param e
     * @return The correct value of the ipaddr portion of the status key
     */
    public String getMatchedIpAddr(Event e) {
        String eventToken = getPassiveEventByUei(e.getUei()).getStatusKey().getIpaddr().getEventToken().getName();
        String expr = getPassiveEventByUei(e.getUei()).getStatusKey().getIpaddr().getEventToken().getValue();
        String pattern = getPassiveEventByUei(e.getUei()).getStatusKey().getIpaddr().getEventToken().getFormat();
        boolean isParm = getPassiveEventByUei(e.getUei()).getStatusKey().getIpaddr().getEventToken().getIsParm();
        
        String tokenValue = getValueFromFieldOrParm(e, eventToken, isParm);
        return parseExpression(tokenValue, expr, pattern);
    }

    /**
     * This method determines the nodelabel value by using the config to determine this value
     * based on field and parm values of a passive event.
     * 
     * @param e
     * @return The correct value of the nodelabel portion of the status key
     */
    public String getMatchedNodeLabel(Event e) {
        String eventToken = getPassiveEventByUei(e.getUei()).getStatusKey().getNodeLabel().getEventToken().getName();
        String expr = getPassiveEventByUei(e.getUei()).getStatusKey().getNodeLabel().getEventToken().getValue();
        String pattern = getPassiveEventByUei(e.getUei()).getStatusKey().getNodeLabel().getEventToken().getFormat();
        boolean isParm = getPassiveEventByUei(e.getUei()).getStatusKey().getNodeLabel().getEventToken().getIsParm();

        String tokenValue = getValueFromFieldOrParm(e, eventToken, isParm);
        return parseExpression(tokenValue, expr, pattern);
    }

    /**
     * Wish there was a better way to do this!
     * @param eventToken
     * @param e
     * @return String value from the event field specified in @param eventToken
     */
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
        log().debug("isPassiveStatusEvent: Received valid UEI: "+e.getUei()+", checking parms...");
        return eventContainsRequiredParms(e);
    }

    private Category log() {
        return ThreadCategory.getInstance("PassiveStatusConfigFactory.class");
    }

    private String getValueFromFieldOrParm(Event e, String eventToken, boolean isParm) {
        String tokenValue;
        if (isParm == true) {
            tokenValue = EventUtil.getNamedParmValue("parm["+ eventToken +"]", e);
        } else {
            tokenValue = getEventField(eventToken, e);
        }
        return tokenValue;
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
        boolean hasAllParms = false;
        List passiveStatusParmNames = getPassiveStatusParmNames(e);
        Parms parms = e.getParms();
        if (parms != null && passiveStatusParmNames != null) {
            List labelList = getParmsLabels(parms);
            if (labelList.containsAll(passiveStatusParmNames))
                hasAllParms = true;
        }
        log().debug("eventContainsRequiredParms: this passive event has all parms required in configuration: "+Boolean.toString(hasAllParms));
        return hasAllParms;
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
        boolean hasParm = false;
        if ( getPassiveEventByUei(e.getUei()).getStatusKey().getNodeLabel().getEventToken().getIsParm() ||
                getPassiveEventByUei(e.getUei()).getStatusKey().getIpaddr().getEventToken().getIsParm() ||
                getPassiveEventByUei(e.getUei()).getStatusKey().getServiceName().getEventToken().getIsParm() ||
                getPassiveEventByUei(e.getUei()).getStatusKey().getStatus().getEventToken().getIsParm())
            hasParm = true;
        log().debug("isParmRequired: This passive event requires parms: "+Boolean.toString(hasParm));
        return hasParm;
    }
    
    /**
     * Parses regular expressions and returns either the expr string
     * or the back reference(s) within the expression.  If the string begins
     * with "~", then it is treated as a regular expression begining with the
     * second character otherwise the expression is treated as a literal and
     * is returned without matching.
     * 
     * If no grouping is used in the string, then the entire match (group 0) is
     * returned.  If there is one or more groups, then the groups are returned
     * concatenated into one string.
     * 
     * Use the pattern to do a very limited printf style formatting of the string
     * using $1 - $9 to reference back references of the expr.  Example:
     *      value = "Channel 9"
     *      expr = "~^(Channel) (9)"
     *      formatPattern = "$1-$2"
     *      
     *      retValue will be: "Channel-9"
     *      
     * @param value
     * @param expr
     * @param formatPattern
     * @return a formatted regex/the literal/or empty string
     */
    public String parseExpression(String value, String expr, String formatPattern) {
        String retValue = "";
        if (expr.startsWith("~")) {
            Pattern p = Pattern.compile(expr.substring(1));
            Matcher m = p.matcher(value);
    
            if (m.matches()) {
                if (m.groupCount() == 0 || formatPattern == null) {
                    retValue = m.group(0);
                } else {
                    retValue = applyFormat(formatPattern, m);
                }
            }
            
        } else {
            //this, in-fact, makes the the field/parm value unused and takes
            //the literal of expr (the value in the config)
            retValue = expr;
        }
        
        return retValue;
    }

    /**
     * Use formatPattern to reference matching groups in the matcher m.  Use
     * '$[0-9]' to referenece a group in the matcher. 
     * 
     * @param formatPattern
     * @param m
     * @return a string representing the format in formatPattern
     */
    private String applyFormat(String formatPattern, Matcher m) {
        String retValue = "";
        //Loop through the expression looking for $
        for (int i=0; i<formatPattern.length(); i++) {
            String nextChar = formatPattern.substring(i, i+1);
            if (nextChar.equals("$") && i+1 <= formatPattern.length()) {
                nextChar = formatPattern.substring(++i, i+1);
                if (nextChar.matches("[0123456789]") && Integer.parseInt(nextChar) <= m.groupCount()) {
                    retValue += m.group(Integer.parseInt(nextChar));
                }
            } else {
                retValue += nextChar;
            }
        }
        return retValue;
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

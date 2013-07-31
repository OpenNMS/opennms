/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.event;

import javax.xml.bind.annotation.XmlRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.opennms.xmlns.xsd.event package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(ObjectFactory.class);



    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.opennms.xmlns.xsd.event
     * 
     */
    public ObjectFactory() {
    	LOG.debug("ObjectFactory initialized");
    }

    /**
     * Create an instance of {@link AlarmData }
     * 
     */
    public AlarmData createAlarmData() {
    	LOG.debug("createAlarmData");
        return new AlarmData();
    }

    /**
     * Create an instance of {@link Value }
     * 
     */
    public Value createValue() {
    	LOG.debug("createValue");
        return new Value();
    }

    /**
     * Create an instance of {@link Events }
     * 
     */
    public Events createEvents() {
    	LOG.debug("createEvents");
        return new Events();
    }

    /**
     * Create an instance of {@link Maskelement }
     * 
     */
    public Maskelement createMaskelement() {
    	LOG.debug("createMaskelement");
        return new Maskelement();
    }

    /**
     * Create an instance of {@link Log }
     * 
     */
    public Log createLog() {
    	LOG.debug("createLog");
        return new Log();
    }

    /**
     * Create an instance of {@link Forward }
     * 
     */
    public Forward createForward() {
    	LOG.debug("createForward");
        return new Forward();
    }

    /**
     * Create an instance of {@link Parms }
     * 
     */
    @Deprecated
    public Parms createParms() {
    	LOG.debug("createParms");
        return new Parms();
    }

    /**
     * Create an instance of {@link Event }
     * 
     */
    public Event createEvent() {
    	LOG.debug("createEvent");
        return new Event();
    }

    /**
     * Create an instance of {@link Header }
     * 
     */
    public Header createHeader() {
    	LOG.debug("createHeader");
        return new Header();
    }

    /**
     * Create an instance of {@link Logmsg }
     * 
     */
    public Logmsg createLogmsg() {
    	LOG.debug("createLogmsg");
        return new Logmsg();
    }

    /**
     * Create an instance of {@link Mask }
     * 
     */
    public Mask createMask() {
    	LOG.debug("createMask");
        return new Mask();
    }

    /**
     * Create an instance of {@link Operaction }
     * 
     */
    public Operaction createOperaction() {
    	LOG.debug("createOperaction");
        return new Operaction();
    }

    /**
     * Create an instance of {@link Autoacknowledge }
     * 
     */
    public Autoacknowledge createAutoacknowledge() {
    	LOG.debug("createAutoacknowledge");
        return new Autoacknowledge();
    }

    /**
     * Create an instance of {@link Parm }
     * 
     */
    public Parm createParm() {
    	LOG.debug("createParm");
        return new Parm();
    }

    /**
     * Create an instance of {@link Snmp }
     * 
     */
    public Snmp createSnmp() {
    	LOG.debug("createSnmp");
        return new Snmp();
    }

    /**
     * Create an instance of {@link Script }
     * 
     */
    public Script createScript() {
    	LOG.debug("createScript");
        return new Script();
    }

    /**
     * Create an instance of {@link EventReceipt }
     * 
     */
    public EventReceipt createEventReceipt() {
    	LOG.debug("createEventReceipt");
        return new EventReceipt();
    }

    /**
     * Create an instance of {@link Correlation }
     * 
     */
    public Correlation createCorrelation() {
    	LOG.debug("createCorrelation");
        return new Correlation();
    }

    /**
     * Create an instance of {@link Autoaction }
     * 
     */
    public Autoaction createAutoaction() {
    	LOG.debug("createAutoaction");
        return new Autoaction();
    }

    /**
     * Create an instance of {@link Tticket }
     * 
     */
    public Tticket createTticket() {
    	LOG.debug("createTticket");
        return new Tticket();
    }

}

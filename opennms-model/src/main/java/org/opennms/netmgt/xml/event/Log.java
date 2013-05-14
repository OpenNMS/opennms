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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.xml.event;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class Log.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="log")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Log implements Serializable {
	private static final long serialVersionUID = 8526177705077223094L;

	//--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _header.
     */
	@XmlElement(name="header", required=false)
    private org.opennms.netmgt.xml.event.Header _header;

    /**
     * Field _events.
     */
	@XmlElement(name="events", required=true)
    private org.opennms.netmgt.xml.event.Events _events;


      //----------------/
     //- Constructors -/
    //----------------/

    public Log() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'events'.
     * 
     * @return the value of field 'Events'.
     */
    public org.opennms.netmgt.xml.event.Events getEvents(
    ) {
        return this._events;
    }

    /**
     * Returns the value of field 'header'.
     * 
     * @return the value of field 'Header'.
     */
    public org.opennms.netmgt.xml.event.Header getHeader(
    ) {
        return this._header;
    }

    /**
     * Sets the value of field 'events'.
     * 
     * @param events the value of field 'events'.
     */
    public void setEvents(
            final org.opennms.netmgt.xml.event.Events events) {
        this._events = events;
    }

    /**
     * Sets the value of field 'header'.
     * 
     * @param header the value of field 'header'.
     */
    public void setHeader(
            final org.opennms.netmgt.xml.event.Header header) {
        this._header = header;
    }

        @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("header", _header)
    		.append("events", _events)
    		.toString();
    }
}

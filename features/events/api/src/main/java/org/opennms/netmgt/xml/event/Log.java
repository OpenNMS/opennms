/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.xml.event;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    @Valid
    private org.opennms.netmgt.xml.event.Header _header;

    /**
     * Field _events.
     */
    @XmlElement(name="events", required=true)
    @Size(min=1)
    @Valid
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

    public void addEvent(final Event event) {
        assertEventsExists();
        this._events.addEvent(event);
    }

    public void addAllEvents(final Log log) {
        assertEventsExists();
        this._events.addAllEvents(log.getEvents());
    }


    protected void assertEventsExists() {
        if (this._events == null) {
            this._events = new Events();
        }
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

    public void clear() {
        this._events = new Events();
    }

    @Override
    public String toString() {
        return new OnmsStringBuilder(this).toString();
    }

}

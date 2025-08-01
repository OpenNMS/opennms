/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

import org.opennms.core.ipc.sink.api.Message;

/**
 * Class Log.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="log")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Log implements Message,Serializable {
    private static final long serialVersionUID = 7684449895077223094L;

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

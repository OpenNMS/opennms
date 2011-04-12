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

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("header", _header)
    		.append("events", _events)
    		.toString();
    }
}

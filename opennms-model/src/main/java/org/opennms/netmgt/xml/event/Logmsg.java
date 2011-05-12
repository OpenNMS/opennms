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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The event logmsg with the destination attribute defining
 *  if event is for display only, logonly, log and display or
 * neither. A
 *  destination attribute of 'donotpersist' indicates that Eventd
 * is not to
 *  persist the event to the database. The optional notify
 * attributed can be 
 *  used to suppress notices on a particular event (by default it
 * is true - 
 *  i.e. a notice will be sent.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="logmsg")
@XmlAccessorType(XmlAccessType.FIELD)
public class Logmsg implements Serializable {

    private static final long serialVersionUID = -7173862847984790914L;

    //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

	/**
     * internal content storage
     */
	@XmlValue
    private java.lang.String _content = "";

    /**
     * Field _notify.
     */
	@XmlAttribute(name="notify")
    private Boolean _notify = true;

    /**
     * Field _dest.
     */
    @XmlAttribute(name="dest")
    private java.lang.String _dest = "logndisplay";


      //----------------/
     //- Constructors -/
    //----------------/

    public Logmsg() {
        super();
        setContent("");
        setDest("logndisplay");
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteNotify(
    ) {
        this._notify = null;
    }

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     * 
     * @return the value of field 'Content'.
     */
    public java.lang.String getContent(
    ) {
        return this._content;
    }

    /**
     * Returns the value of field 'dest'.
     * 
     * @return the value of field 'Dest'.
     */
    public java.lang.String getDest(
    ) {
        return this._dest;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public Boolean getNotify(
    ) {
        return this._notify == null? false : this._notify;
    }

    /**
     * Method hasNotify.
     * 
     * @return true if at least one Notify has been added
     */
    public boolean hasNotify(
    ) {
        return this._notify != null;
    }

    /**
     * Returns the value of field 'notify'.
     * 
     * @return the value of field 'Notify'.
     */
    public Boolean isNotify() {
        return getNotify();
    }

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     * 
     * @param content the value of field 'content'.
     */
    public void setContent(
            final java.lang.String content) {
        this._content = content;
    }

    /**
     * Sets the value of field 'dest'.
     * 
     * @param dest the value of field 'dest'.
     */
    public void setDest(
            final java.lang.String dest) {
        this._dest = dest;
    }

    /**
     * Sets the value of field 'notify'.
     * 
     * @param notify the value of field 'notify'.
     */
    public void setNotify(
            final boolean notify) {
        this._notify = notify;
    }

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("content", _content)
    		.append("notify", _notify)
    		.append("dest", _dest)
    		.toString();
    }
}

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The operator action to be taken when this event occurs
 *  with state controlling if action takes place. The menutext gets
 *  displayed in the UI.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="operaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Operaction {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
	@XmlValue
    private java.lang.String _content = "";

    /**
     * Field _state.
     */
	@XmlAttribute(name="state")
    private java.lang.String _state = "on";

    /**
     * Field _menutext.
     */
	@XmlAttribute(name="menutext", required=true)
    private java.lang.String _menutext;


      //----------------/
     //- Constructors -/
    //----------------/

    public Operaction() {
        super();
        setContent("");
        setState("on");
    }


      //-----------/
     //- Methods -/
    //-----------/

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
     * Returns the value of field 'menutext'.
     * 
     * @return the value of field 'Menutext'.
     */
    public java.lang.String getMenutext(
    ) {
        return this._menutext;
    }

    /**
     * Returns the value of field 'state'.
     * 
     * @return the value of field 'State'.
     */
    public java.lang.String getState(
    ) {
        return this._state;
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
     * Sets the value of field 'menutext'.
     * 
     * @param menutext the value of field 'menutext'.
     */
    public void setMenutext(
            final java.lang.String menutext) {
        this._menutext = menutext;
    }

    /**
     * Sets the value of field 'state'.
     * 
     * @param state the value of field 'state'.
     */
    public void setState(
            final java.lang.String state) {
        this._state = state;
    }

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("content", _content)
    		.append("state", _state)
    		.append("menutext", _menutext)
    		.toString();
    }
}

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A varbind from the trap
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="parm")
@XmlAccessorType(XmlAccessType.FIELD)
public class Parm {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * parm name
     */
	@XmlElement(name="parmName", required=true)
    private java.lang.String _parmName;

    /**
     * parm value
     */
	@XmlElement(name="value", required=true)
	private org.opennms.netmgt.xml.event.Value _value;


      //----------------/
     //- Constructors -/
    //----------------/

    public Parm() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    public Parm(final String name, final String value) {
    	this();
    	setParmName(name);
    	this._value = new Value(value);
	}


	/**
     * Returns the value of field 'parmName'. The field 'parmName'
     * has the following description: parm name
     * 
     * @return the value of field 'ParmName'.
     */
    public java.lang.String getParmName(
    ) {
        return this._parmName;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has
     * the following description: parm value
     * 
     * @return the value of field 'Value'.
     */
    public org.opennms.netmgt.xml.event.Value getValue(
    ) {
        return this._value;
    }

    /**
     * Sets the value of field 'parmName'. The field 'parmName' has
     * the following description: parm name
     * 
     * @param parmName the value of field 'parmName'.
     */
    public void setParmName(
            final java.lang.String parmName) {
        this._parmName = parmName;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the
     * following description: parm value
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(
            final org.opennms.netmgt.xml.event.Value value) {
        this._value = value;
    }

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("parmName", _parmName)
    		.append("value", _value)
    		.toString();
    }


	public boolean isValid() {
		return getParmName() != null && getValue() != null;
	}
}

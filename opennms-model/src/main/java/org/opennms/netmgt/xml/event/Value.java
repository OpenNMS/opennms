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
 * parm value
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="value")
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * internal content storage
     */
	@XmlValue
    private java.lang.String _content = "";

    /**
     * Field _type.
     */
	@XmlAttribute(name="type")
    private java.lang.String _type = "string";

    /**
     * Field _encoding.
     */
	@XmlAttribute(name="encoding")
    private java.lang.String _encoding = "text";


      //----------------/
     //- Constructors -/
    //----------------/

    public Value() {
        super();
        setContent("");
        setType("string");
        setEncoding("text");
    }


      //-----------/
     //- Methods -/
    //-----------/

    public Value(final String value) {
    	this();
    	setContent(value);
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
     * Returns the value of field 'encoding'.
     * 
     * @return the value of field 'Encoding'.
     */
    public java.lang.String getEncoding(
    ) {
        return this._encoding;
    }

    /**
     * Returns the value of field 'type'.
     * 
     * @return the value of field 'Type'.
     */
    public java.lang.String getType(
    ) {
        return this._type;
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
     * Sets the value of field 'encoding'.
     * 
     * @param encoding the value of field 'encoding'.
     */
    public void setEncoding(
            final java.lang.String encoding) {
        this._encoding = encoding;
    }

    /**
     * Sets the value of field 'type'.
     * 
     * @param type the value of field 'type'.
     */
    public void setType(
            final java.lang.String type) {
        this._type = type;
    }

    public String toString() {
    	return new ToStringBuilder(this)
    		.append("content", _content)
    		.append("type", _type)
    		.append("encoding", _encoding)
    		.toString();
    }
}

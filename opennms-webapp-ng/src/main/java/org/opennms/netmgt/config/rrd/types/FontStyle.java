/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: FontStyle.java,v 1.2 2005/11/03 20:43:59 brozow Exp $
 */

package org.opennms.netmgt.config.rrd.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class FontStyle.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:59 $
 */
public class FontStyle implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The PLAIN type
     */
    public static final int PLAIN_TYPE = 0;

    /**
     * The instance of the PLAIN type
     */
    public static final FontStyle PLAIN = new FontStyle(PLAIN_TYPE, "PLAIN");

    /**
     * The BOLD type
     */
    public static final int BOLD_TYPE = 1;

    /**
     * The instance of the BOLD type
     */
    public static final FontStyle BOLD = new FontStyle(BOLD_TYPE, "BOLD");

    /**
     * The ITALIC type
     */
    public static final int ITALIC_TYPE = 2;

    /**
     * The instance of the ITALIC type
     */
    public static final FontStyle ITALIC = new FontStyle(ITALIC_TYPE, "ITALIC");

    /**
     * The BOLD ITALIC type
     */
    public static final int BOLD_ITALIC_TYPE = 3;

    /**
     * The instance of the BOLD ITALIC type
     */
    public static final FontStyle BOLD_ITALIC = new FontStyle(BOLD_ITALIC_TYPE, "BOLD ITALIC");

    /**
     * Field _memberTable
     */
    private static java.util.Hashtable _memberTable = init();

    /**
     * Field type
     */
    private int type = -1;

    /**
     * Field stringValue
     */
    private java.lang.String stringValue = null;


      //----------------/
     //- Constructors -/
    //----------------/

    private FontStyle(int type, java.lang.String value) 
     {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.opennms.netmgt.config.rrd.types.FontStyle(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * FontStyle
     * 
     * @return Enumeration
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getType
     * 
     * Returns the type of this FontStyle
     * 
     * @return int
     */
    public int getType()
    {
        return this.type;
    } //-- int getType() 

    /**
     * Method init
     * 
     * 
     * 
     * @return Hashtable
     */
    private static java.util.Hashtable init()
    {
        Hashtable members = new Hashtable();
        members.put("PLAIN", PLAIN);
        members.put("BOLD", BOLD);
        members.put("ITALIC", ITALIC);
        members.put("BOLD ITALIC", BOLD_ITALIC);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method readResolve
     * 
     *  will be called during deserialization to replace the
     * deserialized object with the correct constant instance.
     * <br/>
     * 
     * @return Object
     */
    private java.lang.Object readResolve()
    {
        return valueOf(this.stringValue);
    } //-- java.lang.Object readResolve() 

    /**
     * Method toString
     * 
     * Returns the String representation of this FontStyle
     * 
     * @return String
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOf
     * 
     * Returns a new FontStyle based on the given String value.
     * 
     * @param string
     * @return FontStyle
     */
    public static org.opennms.netmgt.config.rrd.types.FontStyle valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid FontStyle";
            throw new IllegalArgumentException(err);
        }
        return (FontStyle) obj;
    } //-- org.opennms.netmgt.config.rrd.types.FontStyle valueOf(java.lang.String) 

}

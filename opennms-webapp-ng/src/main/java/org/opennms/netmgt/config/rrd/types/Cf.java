/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: Cf.java,v 1.2 2005/11/03 20:43:59 brozow Exp $
 */

package org.opennms.netmgt.config.rrd.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class Cf.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:59 $
 */
public class Cf implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The AVERAGE type
     */
    public static final int AVERAGE_TYPE = 0;

    /**
     * The instance of the AVERAGE type
     */
    public static final Cf AVERAGE = new Cf(AVERAGE_TYPE, "AVERAGE");

    /**
     * The MAX type
     */
    public static final int MAX_TYPE = 1;

    /**
     * The instance of the MAX type
     */
    public static final Cf MAX = new Cf(MAX_TYPE, "MAX");

    /**
     * The MIN type
     */
    public static final int MIN_TYPE = 2;

    /**
     * The instance of the MIN type
     */
    public static final Cf MIN = new Cf(MIN_TYPE, "MIN");

    /**
     * The LAST type
     */
    public static final int LAST_TYPE = 3;

    /**
     * The instance of the LAST type
     */
    public static final Cf LAST = new Cf(LAST_TYPE, "LAST");

    /**
     * The TOTAL type
     */
    public static final int TOTAL_TYPE = 4;

    /**
     * The instance of the TOTAL type
     */
    public static final Cf TOTAL = new Cf(TOTAL_TYPE, "TOTAL");

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

    private Cf(int type, java.lang.String value) 
     {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.opennms.netmgt.config.rrd.types.Cf(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of Cf
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
     * Returns the type of this Cf
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
        members.put("AVERAGE", AVERAGE);
        members.put("MAX", MAX);
        members.put("MIN", MIN);
        members.put("LAST", LAST);
        members.put("TOTAL", TOTAL);
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
     * Returns the String representation of this Cf
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
     * Returns a new Cf based on the given String value.
     * 
     * @param string
     * @return Cf
     */
    public static org.opennms.netmgt.config.rrd.types.Cf valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid Cf";
            throw new IllegalArgumentException(err);
        }
        return (Cf) obj;
    } //-- org.opennms.netmgt.config.rrd.types.Cf valueOf(java.lang.String) 

}

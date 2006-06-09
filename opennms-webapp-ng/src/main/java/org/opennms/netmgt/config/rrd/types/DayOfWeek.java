/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: DayOfWeek.java,v 1.2 2005/11/03 20:43:59 brozow Exp $
 */

package org.opennms.netmgt.config.rrd.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class DayOfWeek.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:59 $
 */
public class DayOfWeek implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The MONDAY type
     */
    public static final int MONDAY_TYPE = 0;

    /**
     * The instance of the MONDAY type
     */
    public static final DayOfWeek MONDAY = new DayOfWeek(MONDAY_TYPE, "MONDAY");

    /**
     * The TUESDAY type
     */
    public static final int TUESDAY_TYPE = 1;

    /**
     * The instance of the TUESDAY type
     */
    public static final DayOfWeek TUESDAY = new DayOfWeek(TUESDAY_TYPE, "TUESDAY");

    /**
     * The WEDNESDAY type
     */
    public static final int WEDNESDAY_TYPE = 2;

    /**
     * The instance of the WEDNESDAY type
     */
    public static final DayOfWeek WEDNESDAY = new DayOfWeek(WEDNESDAY_TYPE, "WEDNESDAY");

    /**
     * The THURSDAY type
     */
    public static final int THURSDAY_TYPE = 3;

    /**
     * The instance of the THURSDAY type
     */
    public static final DayOfWeek THURSDAY = new DayOfWeek(THURSDAY_TYPE, "THURSDAY");

    /**
     * The FRIDAY type
     */
    public static final int FRIDAY_TYPE = 4;

    /**
     * The instance of the FRIDAY type
     */
    public static final DayOfWeek FRIDAY = new DayOfWeek(FRIDAY_TYPE, "FRIDAY");

    /**
     * The SATURDAY type
     */
    public static final int SATURDAY_TYPE = 5;

    /**
     * The instance of the SATURDAY type
     */
    public static final DayOfWeek SATURDAY = new DayOfWeek(SATURDAY_TYPE, "SATURDAY");

    /**
     * The SUNDAY type
     */
    public static final int SUNDAY_TYPE = 6;

    /**
     * The instance of the SUNDAY type
     */
    public static final DayOfWeek SUNDAY = new DayOfWeek(SUNDAY_TYPE, "SUNDAY");

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

    private DayOfWeek(int type, java.lang.String value) 
     {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.opennms.netmgt.config.rrd.types.DayOfWeek(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * DayOfWeek
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
     * Returns the type of this DayOfWeek
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
        members.put("MONDAY", MONDAY);
        members.put("TUESDAY", TUESDAY);
        members.put("WEDNESDAY", WEDNESDAY);
        members.put("THURSDAY", THURSDAY);
        members.put("FRIDAY", FRIDAY);
        members.put("SATURDAY", SATURDAY);
        members.put("SUNDAY", SUNDAY);
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
     * Returns the String representation of this DayOfWeek
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
     * Returns a new DayOfWeek based on the given String value.
     * 
     * @param string
     * @return DayOfWeek
     */
    public static org.opennms.netmgt.config.rrd.types.DayOfWeek valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid DayOfWeek";
            throw new IllegalArgumentException(err);
        }
        return (DayOfWeek) obj;
    } //-- org.opennms.netmgt.config.rrd.types.DayOfWeek valueOf(java.lang.String) 

}

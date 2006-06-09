/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: GridTimeUnit.java,v 1.2 2005/11/03 20:43:59 brozow Exp $
 */

package org.opennms.netmgt.config.rrd.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class GridTimeUnit.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:59 $
 */
public class GridTimeUnit implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The SECOND type
     */
    public static final int SECOND_TYPE = 0;

    /**
     * The instance of the SECOND type
     */
    public static final GridTimeUnit SECOND = new GridTimeUnit(SECOND_TYPE, "SECOND");

    /**
     * The MINUTE type
     */
    public static final int MINUTE_TYPE = 1;

    /**
     * The instance of the MINUTE type
     */
    public static final GridTimeUnit MINUTE = new GridTimeUnit(MINUTE_TYPE, "MINUTE");

    /**
     * The HOUR type
     */
    public static final int HOUR_TYPE = 2;

    /**
     * The instance of the HOUR type
     */
    public static final GridTimeUnit HOUR = new GridTimeUnit(HOUR_TYPE, "HOUR");

    /**
     * The DAY type
     */
    public static final int DAY_TYPE = 3;

    /**
     * The instance of the DAY type
     */
    public static final GridTimeUnit DAY = new GridTimeUnit(DAY_TYPE, "DAY");

    /**
     * The WEEK type
     */
    public static final int WEEK_TYPE = 4;

    /**
     * The instance of the WEEK type
     */
    public static final GridTimeUnit WEEK = new GridTimeUnit(WEEK_TYPE, "WEEK");

    /**
     * The MONTH type
     */
    public static final int MONTH_TYPE = 5;

    /**
     * The instance of the MONTH type
     */
    public static final GridTimeUnit MONTH = new GridTimeUnit(MONTH_TYPE, "MONTH");

    /**
     * The YEAR type
     */
    public static final int YEAR_TYPE = 6;

    /**
     * The instance of the YEAR type
     */
    public static final GridTimeUnit YEAR = new GridTimeUnit(YEAR_TYPE, "YEAR");

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

    private GridTimeUnit(int type, java.lang.String value) 
     {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.opennms.netmgt.config.rrd.types.GridTimeUnit(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * GridTimeUnit
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
     * Returns the type of this GridTimeUnit
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
        members.put("SECOND", SECOND);
        members.put("MINUTE", MINUTE);
        members.put("HOUR", HOUR);
        members.put("DAY", DAY);
        members.put("WEEK", WEEK);
        members.put("MONTH", MONTH);
        members.put("YEAR", YEAR);
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
     * Returns the String representation of this GridTimeUnit
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
     * Returns a new GridTimeUnit based on the given String value.
     * 
     * @param string
     * @return GridTimeUnit
     */
    public static org.opennms.netmgt.config.rrd.types.GridTimeUnit valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid GridTimeUnit";
            throw new IllegalArgumentException(err);
        }
        return (GridTimeUnit) obj;
    } //-- org.opennms.netmgt.config.rrd.types.GridTimeUnit valueOf(java.lang.String) 

}

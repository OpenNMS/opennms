/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.7</a>, using an XML
 * Schema.
 * $Id: Backend.java,v 1.2 2005/11/03 20:43:59 brozow Exp $
 */

package org.opennms.netmgt.config.rrd.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * Class Backend.
 * 
 * @version $Revision: 1.2 $ $Date: 2005/11/03 20:43:59 $
 */
public class Backend implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The FILE type
     */
    public static final int FILE_TYPE = 0;

    /**
     * The instance of the FILE type
     */
    public static final Backend FILE = new Backend(FILE_TYPE, "FILE");

    /**
     * The NIO type
     */
    public static final int NIO_TYPE = 1;

    /**
     * The instance of the NIO type
     */
    public static final Backend NIO = new Backend(NIO_TYPE, "NIO");

    /**
     * The MEMORY type
     */
    public static final int MEMORY_TYPE = 2;

    /**
     * The instance of the MEMORY type
     */
    public static final Backend MEMORY = new Backend(MEMORY_TYPE, "MEMORY");

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

    private Backend(int type, java.lang.String value) 
     {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.opennms.netmgt.config.rrd.types.Backend(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of Backend
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
     * Returns the type of this Backend
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
        members.put("FILE", FILE);
        members.put("NIO", NIO);
        members.put("MEMORY", MEMORY);
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
     * Returns the String representation of this Backend
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
     * Returns a new Backend based on the given String value.
     * 
     * @param string
     * @return Backend
     */
    public static org.opennms.netmgt.config.rrd.types.Backend valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid Backend";
            throw new IllegalArgumentException(err);
        }
        return (Backend) obj;
    } //-- org.opennms.netmgt.config.rrd.types.Backend valueOf(java.lang.String) 

}

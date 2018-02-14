/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.client.schema.types;

/**
 * Enumeration InterfaceSnmpPrimaryType.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public enum InterfaceSnmpPrimaryType implements java.io.Serializable {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant P
     */
    P("P"),
    /**
     * Constant S
     */
    S("S"),
    /**
     * Constant C
     */
    C("C"),
    /**
     * Constant N
     */
    N("N");

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field value.
     */
    private final java.lang.String value;


      //----------------/
     //- Constructors -/
    //----------------/

    private InterfaceSnmpPrimaryType(final java.lang.String value) {
        this.value = value;
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method fromValue.
     * 
     * @param value
     * @return the constant for this value
     */
    public static org.opennms.client.schema.types.InterfaceSnmpPrimaryType fromValue(
            final java.lang.String value) {
        for (InterfaceSnmpPrimaryType c: InterfaceSnmpPrimaryType.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * 
     * 
     * @param value
     */
    public void setValue(
            final java.lang.String value) {
    }

    /**
     * Method toString.
     * 
     * @return the value of this constant
     */
    public java.lang.String toString(
    ) {
        return this.value;
    }

    /**
     * Method value.
     * 
     * @return the value of this constant
     */
    public java.lang.String value(
    ) {
        return this.value;
    }

}

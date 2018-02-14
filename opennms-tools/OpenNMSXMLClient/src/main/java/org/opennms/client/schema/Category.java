/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.0.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.client.schema;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This element is used to specify OpenNMS specific categories.
 * Note: currently, these categories are defined in a separate
 * configuration file and
 *  are related directly to monitored services. I have separated
 * out this element so that it can be referenced by other entities
 * (nodes, interfaces, etc.)
 *  however, they will be ignored until the domain model is changed
 * and the service layer is adapted for this behavior. 
 *  
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class Category implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    private java.lang.String _name;


      //----------------/
     //- Constructors -/
    //----------------/

    public Category() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * 
     * 
     * @param out
     * object is an invalid instance according to the schema
     * @throws JAXBException 
     */
    public void marshal(final java.io.Writer out) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(Category.class);
        final Marshaller m = context.createMarshaller();
        m.marshal(this, out);
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled org.opennms.client.schema.Category
     * @throws JAXBException 
     */
    public static org.opennms.client.schema.Category unmarshal(final java.io.Reader reader) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(Category.class);
        final Unmarshaller um = context.createUnmarshaller();
        return (Category) um.unmarshal(reader);
    }

}

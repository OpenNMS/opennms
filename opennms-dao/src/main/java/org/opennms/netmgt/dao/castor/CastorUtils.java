package org.opennms.netmgt.dao.castor;

import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.springframework.dao.DataAccessException;

/**
 * Utility class for Castor configuration files.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class CastorUtils {
    private static final CastorExceptionTranslator CASTOR_EXCEPTION_TRANSLATOR = new CastorExceptionTranslator();

    
    /** Private constructor since this class only has static methods (so far). */
    private CastorUtils() {
    }
    
    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type. 
     * 
     * @param <T> the class representing the marshalled XML configuration
     *      file.  This will be the return time form the method.
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param reader the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException
     * @throws ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a ValidationException
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(Class<T> clazz, Reader reader) throws MarshalException, ValidationException {
        return (T) Unmarshaller.unmarshal(clazz, reader);
    }
    

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     * 
     * @param <T> the class representing the marshalled XML configuration
     *      file.  This will be the return time form the method.
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param reader the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws DataAccessException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using CastorExceptionTranslator.
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Reader reader) throws DataAccessException {
        try {
            return (T) Unmarshaller.unmarshal(clazz, reader);
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("Unmarshalling XML file", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("Unmarshalling XML file", e);
        }
    }
}

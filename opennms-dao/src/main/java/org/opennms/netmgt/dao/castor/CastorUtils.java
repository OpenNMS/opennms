package org.opennms.netmgt.dao.castor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
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
     * Marshal a Castor XML configuration file.
     *
     * @param obj the object representing the objected to be marshalled to XML
     * @param writer where the marshalled XML will be written
     * @throws DataAccessException if the underlying Castor
     *      Marshaller.marshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using CastorExceptionTranslator.
     */
    public static void marshalWithTranslatedExceptions(Object obj, Writer writer) throws DataAccessException {
        try {
            Marshaller.marshal(obj, writer);
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("Marshalling XML file", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("Marshalling XML file", e);
        }
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

    /**
     * Marshall to a string first, then write the string to the file. This
     * way the original config isn't lost if the xml from the marshall is hosed.
     * 
     * FIXME: This could still stand to write to a temporary file and/or make a
     * temporary backup of the production configuration file.
     */
    public static void marshalViaString(Object config, File cfgFile) throws MarshalException, ValidationException, IOException {
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(config, stringWriter);

        FileWriter fileWriter = new FileWriter(cfgFile);
        fileWriter.write(stringWriter.toString());
        fileWriter.flush();
        fileWriter.close();
    }
}

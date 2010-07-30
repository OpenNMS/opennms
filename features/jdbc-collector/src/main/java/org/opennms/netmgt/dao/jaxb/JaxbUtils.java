package org.opennms.netmgt.dao.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.xml.sax.InputSource;

public class JaxbUtils {
    //JaxbExceptionTranslator
    private static final JaxbExceptionTranslator JAXB_EXCEPTION_TRANSLATOR = new JaxbExceptionTranslator();
   
    public JaxbUtils() {
        
    }
    
    private static <T> Unmarshaller createUnmarshaller(Class<T> clazz) throws JAXBException {
        JAXBContext c = JAXBContext.newInstance(clazz);
        Unmarshaller um = c.createUnmarshaller();
        um.setSchema(null);
        
        return um;
    }
    
    private static <T> Marshaller createMarshaller(Class<T> clazz) throws JAXBException {
        JAXBContext c = JAXBContext.newInstance(clazz);
        Marshaller m = c.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        return m;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(Class<T> clazz, Reader reader) throws JAXBException {
        return (T) createUnmarshaller(clazz).unmarshal(reader);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(Class<T> clazz, InputSource source) throws JAXBException {
        return (T) createUnmarshaller(clazz).unmarshal(source);
    }
    
    public static <T> T unmarshal(Class<T> clazz, InputStream in) throws JAXBException {
        return unmarshal(clazz, new InputSource(in));
    }
    
    public static <T> T unmarshal(Class<T> clazz, Resource resource) throws JAXBException, IOException {
        InputStream in;
        try {
            in = resource.getInputStream();
        } catch (IOException e) {
            IOException newE = new IOException("Failed to open XML configuration file for resource '" + resource + "': " + e);
            newE.initCause(e);
            throw newE;
        }
    
        try {
            InputSource source = new InputSource(in);
            try {
                source.setSystemId(resource.getURL().toString());
            } catch (Throwable t) {
                // ignore
            }
            return unmarshal(clazz, source);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
    
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Reader reader) throws DataAccessException {
        try {
            return unmarshal(clazz, reader);
        } catch (JAXBException e) {
            throw JAXB_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file", e);
        }
    }
    
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, InputStream in) throws DataAccessException {
        try {
            return unmarshal(clazz, in);
        } catch (JAXBException e) {
            throw JAXB_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file", e);
        }
    }
    
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Resource resource) {
        // TODO It might be useful to add code to test for readability on real files; the code below is from DefaultManualProvisioningDao - dj@opennms.org 
//        if (!importFile.canRead()) {
//            throw new PermissionDeniedDataAccessException("Unable to read file "+importFile, null);
//        }

        InputStream in;
        try {
            in = resource.getInputStream();
        } catch (IOException e) {
            throw JAXB_EXCEPTION_TRANSLATOR.translate("opening XML configuration file for resource '" + resource + "'", e);
        }
    
        try {
            InputSource source = new InputSource(in);
            try {
                source.setSystemId(resource.getURL().toString());
            } catch (Throwable t) {
                /*
                 * resource.getURL() might throw an IOException
                 * (or maybe a DataAccessException, since it's a
                 * RuntimeException), indicating that the resource can't be
                 * represented as a URL.  We don't really care so much--we'll
                 * only lose the ability for Castor to include the resource URL
                 * in error messages and for it to directly resolve relative
                 * URLs (which we don't currently use), so we just ignore it.
                 */
            }
            return unmarshal(clazz, source);
        } catch (JAXBException e) {
            throw JAXB_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file for resource '" + resource + "'", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
    
    @SuppressWarnings("unused")
    private static <T> void marshal(Class<T> clazz, Object obj, Writer writer) throws IOException, JAXBException {
        createMarshaller(clazz).marshal(obj, writer);
    }
}

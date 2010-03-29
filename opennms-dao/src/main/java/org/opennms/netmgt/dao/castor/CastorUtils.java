/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created March 10, 2007
 * 
 * 2008 Jul 28: Use InputSources and InputStreams wherever we can instead of
 *              readers to avoid character set problems with Readers.  Also
 *              deprecate the Reader methods. - dj@opennms.org
 * 2008 Jul 04: Move resource unmarshalling code here. - dj@opennms.org
 * 2008 Jun 14: Use instances of Marshaller/Unmarshaller to avoid
 *              problematic-looking Castor log messages. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.xml.sax.InputSource;

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
            marshal(obj, writer);
        } catch (IOException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("marshalling XML file", e);
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("marshalling XML file", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("marshalling XML file", e);
        }
    }

    private static void marshal(Object obj, Writer writer) throws IOException, MarshalException, ValidationException {
        Marshaller m = new Marshaller(writer);
        m.setSuppressNamespaces(true);
        m.marshal(obj);
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
    public static void marshalWithTranslatedExceptionsViaString(Object obj, Resource resource) throws DataAccessException {
        Writer fileWriter = null;
        try {
            StringWriter writer = new StringWriter();
            marshal(obj, writer);
            
            fileWriter= new OutputStreamWriter(new FileOutputStream(resource.getFile()), "UTF-8");
            fileWriter.write(writer.toString());
            fileWriter.flush();
            
        } catch (IOException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("marshalling XML file", e);
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("marshalling XML file", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("marshalling XML file", e);
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
    }


    /**
     * Create an Unmarshaller for a specific class and configure it with our
     * default configuration details.  In particular, the Unmarshaller is set
     * to not ignore extra attributes and elements.
     * 
     * @param <T>
     * @param clazz
     * @return
     */
    private static <T> Unmarshaller createUnmarshaller(Class<T> clazz) {
        Unmarshaller u = new Unmarshaller(clazz);
        u.setIgnoreExtraAttributes(false);
        u.setIgnoreExtraElements(false);
        return u;
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
     * @deprecated Use a Resource or InputStream-based method instead to avoid
     *             character set issues.
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(Class<T> clazz, Reader reader) throws MarshalException, ValidationException {
        return (T) createUnmarshaller(clazz).unmarshal(reader);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type. 
     * 
     * @param <T> the class representing the marshalled XML configuration
     *      file.  This will be the return time form the method.
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param in the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException
     * @throws ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a ValidationException
     */
    public static <T> T unmarshal(Class<T> clazz, InputStream in) throws MarshalException, ValidationException {
        return unmarshal(clazz, new InputSource(in));
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(Class<T> clazz, InputSource source) throws MarshalException, ValidationException {
        return (T) createUnmarshaller(clazz).unmarshal(source);
    }
    
    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type. 
     * 
     * @param <T> the class representing the marshalled XML configuration
     *      file.  This will be the return time form the method.
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param resource the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException
     * @throws ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a ValidationException
     * @throws IOException if the resource could not be opened
     */
    public static <T> T unmarshal(Class<T> clazz, Resource resource) throws MarshalException, ValidationException, IOException {
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
     * @deprecated Use a Resource or InputStream-based method instead to avoid
     *             character set issues.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Reader reader) throws DataAccessException {
        try {
            return unmarshal(clazz, reader);
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file", e);
        }
    }
    
    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     * 
     * @param <T> the class representing the marshalled XML configuration
     *      file.  This will be the return time form the method.
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param in the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws DataAccessException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using CastorExceptionTranslator.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, InputStream in) throws DataAccessException {
        try {
            return unmarshal(clazz, in);
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file", e);
        }
    }


    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     * 
     * @param <T> the class representing the marshalled XML configuration
     *      file.  This will be the return time form the method.
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param resource the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws DataAccessException if the resource could not be opened or the
     *      underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using CastorExceptionTranslator and will include information about
     *      the resource from its {@link Resource#toString() toString()} method.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Resource resource) {
        // TODO It might be useful to add code to test for readability on real files; the code below is from DefaultManualProvisioningDao - dj@opennms.org 
//        if (!importFile.canRead()) {
//            throw new PermissionDeniedDataAccessException("Unable to read file "+importFile, null);
//        }

        InputStream in;
        try {
            in = resource.getInputStream();
        } catch (IOException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("opening XML configuration file for resource '" + resource + "'", e);
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
        } catch (MarshalException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file for resource '" + resource + "'", e);
        } catch (ValidationException e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("unmarshalling XML file for resource '" + resource + "'", e);
        } finally {
            IOUtils.closeQuietly(in);
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
        
        marshal(config, stringWriter);

        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
        fileWriter.write(stringWriter.toString());
        fileWriter.flush();
        fileWriter.close();
    }
}

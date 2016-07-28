/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
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
 * @version $Id: $
 */
public abstract class CastorUtils {
    private static final MarshallingExceptionTranslator CASTOR_EXCEPTION_TRANSLATOR = new MarshallingExceptionTranslator();
    private static final boolean DEFAULT_PRESERVATION_BEHAVIOR = false;

    /**
     * Marshal a Castor XML configuration file.
     *
     * @param obj the object representing the objected to be marshalled to XML
     * @param writer where the marshalled XML will be written
     * @throws org.springframework.dao.DataAccessException if the underlying Castor
     *      Marshaller.marshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator.
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

    public static String marshal(final Object obj) throws MarshalException, ValidationException, IOException {
        final StringWriter writer = new StringWriter();
        marshal(obj, writer);
        return writer.toString();
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
     * @throws org.springframework.dao.DataAccessException if the underlying Castor
     *      Marshaller.marshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator.
     * @param resource a {@link org.springframework.core.io.Resource} object.
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

    public static <T> Unmarshaller getUnmarshaller(Class<T> clazz) {
        return createUnmarshaller(clazz, true);
    }

    /**
     * Create an Unmarshaller for a specific class and configure it with our
     * default configuration details.  In particular, the Unmarshaller is set
     * to not ignore extra attributes and elements.
     * 
     * @param clazz the class to unmarshal
     * @param preserveWhitespace whether to preserve whitespace when parsing
     * @return
     */
    private static <T> Unmarshaller createUnmarshaller(Class<T> clazz, boolean preserveWhitespace) {
        Unmarshaller u = new Unmarshaller(clazz);
        u.setIgnoreExtraAttributes(false);
        u.setIgnoreExtraElements(false);
        u.setWhitespacePreserve(preserveWhitespace);

        /*
         * Disable SAX features that allow XXE attacks
         * See:
         *   http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2014-3004
         *   http://issues.opennms.org/browse/NMS-7291
         */
        u.setProperty(
            "org.exolab.castor.sax.features",
            "http://apache.org/xml/features/disallow-doctype-decl"
        );
        u.setProperty(
            "org.exolab.castor.sax.features-to-disable",
            "http://xml.org/sax/features/external-general-entities,http://xml.org/sax/features/external-parameter-entities,http://apache.org/xml/features/nonvalidating/load-external-dtd"
        );

        return u;
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type.
     *
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param reader the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws org.exolab.castor.xml.MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.ValidationException
     * @deprecated Use a Resource or InputStream-based method instead to avoid character set issues.
     */
    public static <T> T unmarshal(Class<T> clazz, Reader reader) throws MarshalException, ValidationException {
        return unmarshal(clazz, reader, DEFAULT_PRESERVATION_BEHAVIOR);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type.
     *
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param reader the marshalled XML configuration file to unmarshal
     * @param preserveWhitespace whether or not to preserve whitespace
     * @return Unmarshalled object representing XML file
     * @throws org.exolab.castor.xml.MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.ValidationException
     * @deprecated Use a Resource or InputStream-based method instead to avoid character set issues.
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(Class<T> clazz, Reader reader, boolean preserveWhitespace) throws MarshalException, ValidationException {
        return (T) createUnmarshaller(clazz, preserveWhitespace).unmarshal(reader);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param in the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws org.exolab.castor.xml.MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.ValidationException
     */
    public static <T> T unmarshal(Class<T> clazz, InputStream in) throws MarshalException, ValidationException {
        return unmarshal(clazz, new InputSource(in), DEFAULT_PRESERVATION_BEHAVIOR);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param in the marshalled XML configuration file to unmarshal
     * @param preserveWhitespace whether or not to preserve whitespace
     * @return Unmarshalled object representing XML file
     * @throws org.exolab.castor.xml.MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.ValidationException
     */
    public static <T> T unmarshal(Class<T> clazz, InputStream in, boolean preserveWhitespace) throws MarshalException, ValidationException {
        return unmarshal(clazz, new InputSource(in), preserveWhitespace);
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(Class<T> clazz, InputSource source, boolean preserveWhitespace) throws MarshalException, ValidationException {
        return (T) createUnmarshaller(clazz, preserveWhitespace).unmarshal(source);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param resource the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws org.exolab.castor.xml.MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.ValidationException
     * @throws java.io.IOException if the resource could not be opened
     */
    public static <T> T unmarshal(Class<T> clazz, Resource resource) throws MarshalException, ValidationException, IOException {
        return unmarshal(clazz, resource, DEFAULT_PRESERVATION_BEHAVIOR);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param resource the marshalled XML configuration file to unmarshal
     * @param preserveWhitespace whether or not to preserve whitespace
     * @return Unmarshalled object representing XML file
     * @throws org.exolab.castor.xml.MarshalException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a org.exolab.castor.xml.ValidationException
     * @throws java.io.IOException if the resource could not be opened
     */
    public static <T> T unmarshal(Class<T> clazz, Resource resource, boolean preserveWhitespace) throws MarshalException, ValidationException, IOException {
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
            return unmarshal(clazz, source, preserveWhitespace);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     *
     * @param clazz the class representing the marshalled XML configuration
     *      file
     * @param reader the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws org.springframework.dao.DataAccessException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator.
     * @deprecated Use a Resource or InputStream-based method instead to avoid character set issues.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Reader reader) throws DataAccessException {
        return unmarshalWithTranslatedExceptions(clazz, reader, DEFAULT_PRESERVATION_BEHAVIOR);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param reader the marshalled XML configuration file to unmarshal
     * @param preserveWhitespace Whether to preserve whitespace when unmarshalling.
     * @return Unmarshalled object representing XML file
     * @throws org.springframework.dao.DataAccessException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator.
     * @deprecated Use a Resource or InputStream-based method instead to avoid character set issues.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Reader reader, boolean preserveWhitespace) throws DataAccessException {
        try {
            return unmarshal(clazz, reader, preserveWhitespace);
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
     * @param clazz the class representing the marshalled XML configuration file
     * @param in the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws org.springframework.dao.DataAccessException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, InputStream in) throws DataAccessException {
        return unmarshalWithTranslatedExceptions(clazz, in, DEFAULT_PRESERVATION_BEHAVIOR);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param in the marshalled XML configuration file to unmarshal
     * @param preserveWhitespace whether to preserve whitespace when unmarshalling.
     * @return Unmarshalled object representing XML file
     * @throws org.springframework.dao.DataAccessException if the underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, InputStream in, boolean preserveWhitespace) throws DataAccessException {
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
     * @param clazz the class representing the marshalled XML configuration file
     * @param resource the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws DataAccessException if the resource could not be opened or the
     *      underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator and will include information about
     *      the resource from its toString() method.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Resource resource) {
        return unmarshalWithTranslatedExceptions(clazz, resource, DEFAULT_PRESERVATION_BEHAVIOR);
    }

    /**
     * Unmarshal a Castor XML configuration file.  Uses Java 5 generics for
     * return type and throws DataAccessExceptions.
     *
     * @param clazz the class representing the marshalled XML configuration file
     * @param resource the marshalled XML configuration file to unmarshal
     * @return Unmarshalled object representing XML file
     * @throws DataAccessException if the resource could not be opened or the
     *      underlying Castor
     *      Unmarshaller.unmarshal() call throws a MarshalException or
     *      ValidationException.  The underlying exception will be translated
     *      using MarshallingExceptionTranslator and will include information about
     *      the resource from its toString() method.
     */
    public static <T> T unmarshalWithTranslatedExceptions(Class<T> clazz, Resource resource, boolean preserveWhitespace) {
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
            return unmarshal(clazz, source, preserveWhitespace);
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
     *
     * @param config a {@link java.lang.Object} object.
     * @param cfgFile a {@link java.io.File} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public static void marshalViaString(Object config, File cfgFile) throws MarshalException, ValidationException, IOException {
        StringWriter stringWriter = new StringWriter();

        marshal(config, stringWriter);

        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
        fileWriter.write(stringWriter.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    // FIXME This is a funky way to duplicate an object - dj@opennms.org
    @SuppressWarnings("unchecked")
    public static <T> T duplicateObject(T object, Class<T> clazz) throws MarshalException, ValidationException {
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(object, stringWriter);
        StringReader stringReader = new StringReader(stringWriter.toString());
        return (T) Unmarshaller.unmarshal(clazz, stringReader);
    }
}

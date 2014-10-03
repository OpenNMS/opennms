/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

@Provider
public class ValidatingMessageBodyReader<T> implements MessageBodyReader<T> {
	private static final Logger LOG = LoggerFactory.getLogger(ValidatingMessageBodyReader.class);


	@Context
	protected Providers providers;

	/**
	 * @return true if the class is a JAXB-marshallable class that has 
	 * an {@link javax.xml.bind.annotation.XmlRootElement} annotation.
	 */
        @Override
	public boolean isReadable(final Class<?> clazz, final Type type, final Annotation[] annotations, final MediaType mediaType) {
		return (clazz.getAnnotation(XmlRootElement.class) != null);
	}

        @Override
	public T readFrom(final Class<T> clazz, final Type type, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> parameters, final InputStream stream) throws IOException, WebApplicationException {
		LOG.debug("readFrom: {}/{}/{}", clazz.getSimpleName(), type, mediaType);

		JAXBContext jaxbContext = null;
		final ContextResolver<JAXBContext> resolver = providers.getContextResolver(JAXBContext.class, mediaType);
		try {

			if (resolver != null) {
				jaxbContext = resolver.getContext(clazz);
			}

			if (jaxbContext == null) {
				jaxbContext = JAXBContext.newInstance(clazz);

			}
			
			return JaxbUtils.unmarshal(clazz, new InputSource(stream), jaxbContext);

		} catch (final JAXBException e) {
			LOG.warn("An error occurred while unmarshaling a {} object", clazz.getSimpleName(), e);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}

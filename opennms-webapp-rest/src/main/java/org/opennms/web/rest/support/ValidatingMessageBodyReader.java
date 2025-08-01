/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.support;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
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
@Consumes({ MediaType.APPLICATION_XML, "application/*+xml", MediaType.TEXT_XML })
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

			return JaxbUtils.unmarshal(clazz, new InputSource(stream), jaxbContext, true);

		} catch (final JAXBException e) {
			LOG.warn("An error occurred while unmarshaling a {} object", clazz.getSimpleName(), e);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}

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

import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.JaxbUtils;
import org.xml.sax.InputSource;

@Provider
public class ValidatingMessageBodyReader<T> implements MessageBodyReader<T> {

	@Context
	protected Providers providers;

	@Override
	public boolean isReadable(final Class<?> clazz, final Type type, final Annotation[] annotations, final MediaType mediaType) {
		
		LogUtils.debugf(this, "isReadable");
		return true;
	}

	@Override
	public T readFrom(final Class<T> clazz, final Type type, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> parameters, final InputStream stream) throws IOException, WebApplicationException {
		LogUtils.debugf(this, "readFrom: %s/%s/%s", clazz.getSimpleName(), type, mediaType);

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
			LogUtils.warnf(this, e, "An error occurred while unmarshaling a %s object", clazz.getSimpleName());
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}

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
package org.opennms.web.rest.v1;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.opennms.web.rest.support.MultivaluedMapImpl;

@Provider
@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM })
public class FormPropertiesReader implements MessageBodyReader<MultivaluedMapImpl> {

    private HttpServletRequest m_httpServletRequest;

    @Context
    public void setHttpServletRequest(HttpServletRequest request) {
        m_httpServletRequest = request;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaTypes) {
        return type.isAssignableFrom(MultivaluedMapImpl.class);
    }

    /** {@inheritDoc} */
    @Override
    public MultivaluedMapImpl readFrom(final Class<MultivaluedMapImpl> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) throws IOException, WebApplicationException {

        final MultivaluedMapImpl result = new MultivaluedMapImpl();

        final Enumeration<String> en = m_httpServletRequest.getParameterNames();
        while (en.hasMoreElements()) {
            final String parmName = en.nextElement();
            final String[] parmValue = m_httpServletRequest.getParameterValues(parmName);
            result.put(parmName, Arrays.asList(parmValue));
        }

        return result;
    }

}

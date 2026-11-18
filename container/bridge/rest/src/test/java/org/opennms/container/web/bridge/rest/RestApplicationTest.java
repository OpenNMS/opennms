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
package org.opennms.container.web.bridge.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.http.HttpService;

public class RestApplicationTest {

    private static final String APPLICATION_PATH = "/rest";

    private HttpService httpService;
    private RestApplication application;

    @Before
    public void setUp() throws Exception {
        final ServletConfig servletConfig = mock(ServletConfig.class);
        when(servletConfig.getServletContext()).thenReturn(mock(ServletContext.class));
        when(servletConfig.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        httpService = mock(HttpService.class);
        doAnswer(invocation -> {
            invocation.<Servlet>getArgument(1).init(servletConfig);
            return null;
        }).when(httpService).registerServlet(eq(APPLICATION_PATH), any(Servlet.class), any(), any());
        application = new RestApplication(APPLICATION_PATH, httpService);
    }

    @Test
    public void publishesResources() throws Exception {
        application.addResource(new FirstResource());
        application.addResource(new SecondResource());
        application.publish(List.of());

        assertThat(application.getEndpoints(), containsInAnyOrder("/rest/first", "/rest/second"));
    }

    @Test
    public void registersOneServletForAllPublishOperations() throws Exception {
        application.addResource(new FirstResource());
        application.publish(List.of());
        application.addResource(new SecondResource());
        application.publish(List.of());

        verify(httpService, times(1)).registerServlet(eq(APPLICATION_PATH), any(Servlet.class), any(), any());
    }

    @Test
    public void keepsCurrentResourcesWhenNewResourceIsInvalid() throws Exception {
        application.addResource(new FirstResource());
        application.publish(List.of());

        application.addResource(new InvalidResource());
        application.publish(List.of());

        assertThat(application.getEndpoints(), contains("/rest/first"));
    }

    @Test
    public void publishesValidResourcesWhenOneResourceIsInvalid() throws Exception {
        application.addResource(new FirstResource());
        application.addResource(new InvalidResource());
        application.addResource(new SecondResource());
        application.publish(List.of());

        assertThat(application.getEndpoints(), containsInAnyOrder("/rest/first", "/rest/second"));
    }

    @Test
    public void unregistersServletWhenOnlyResourceIsInvalid() throws Exception {
        application.addResource(new InvalidResource());
        application.publish(List.of());

        assertThat(application.getEndpoints(), is(empty()));
        assertThat(application.isEmpty(), is(true));
        verify(httpService).unregister(APPLICATION_PATH);
    }

    @Test
    public void unregistersServletWhenLastResourceIsRemoved() throws Exception {
        final FirstResource resource = new FirstResource();
        application.addResource(resource);
        application.publish(List.of());

        application.removeResource(resource);
        application.publish(List.of());

        assertThat(application.getEndpoints(), is(empty()));
        verify(httpService).unregister(APPLICATION_PATH);
    }

    @Path("/first")
    public static class FirstResource {
        @GET
        public String get() {
            return "first";
        }
    }

    @Path("/second")
    public static class SecondResource {
        @GET
        public String get() {
            return "second";
        }
    }

    @Path("/invalid/{id: [}")
    public static class InvalidResource {
        @GET
        public String get() {
            return "invalid";
        }
    }
}

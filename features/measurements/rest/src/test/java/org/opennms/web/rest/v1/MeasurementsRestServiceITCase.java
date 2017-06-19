/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.exec.launcher.CommandLauncher;
import org.apache.commons.exec.launcher.CommandLauncherFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * Contains tests that can run irrespective of the fetch strategy.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public abstract class MeasurementsRestServiceITCase {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementsRestServiceITCase.class);

    @Autowired
    protected MeasurementsRestService m_svc;

    @Autowired
    protected MonitoringLocationDao m_locationDao;

    @Autowired
    protected NodeDao m_nodeDao;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    protected FilesystemResourceStorageDao m_resourceStorageDao;

    public void setUp() {
        BeanUtils.assertAutowiring(this);

        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);
        m_nodeDao.flush();
    }

    @Test
    public void notFoundOnMissingResource() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setResourceId("node[99].interfaceSnmp[eth0-04013f75f101]");

        exception.expect(exceptionWithResponseCode(404));
        m_svc.query(request);
    }

    @Test
    public void notFoundOnMissingAttribute() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setAttribute("n0tIfInOctets");

        exception.expect(exceptionWithResponseCode(404));
        m_svc.query(request);
    }

    @Test
    public void badRequestOnMissingLabel() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setLabel(null);

        exception.expect(exceptionWithResponseCode(400));
        m_svc.query(request);
    }

    @Test
    public void badRequestOnMissingAttribute() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setResourceId(null);

        exception.expect(exceptionWithResponseCode(400));
        m_svc.query(request);
    }

    @Test
    public void badRequestOnMissingResourceId() {
        final QueryRequest request = buildRequest();
        request.getSources().get(0).setResourceId(null);

        exception.expect(exceptionWithResponseCode(400));
        m_svc.query(request);
    }

    private static Matcher<?> exceptionWithResponseCode(final int status) {
        return new BaseMatcher<WebApplicationException>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof WebApplicationException)) {
                    return false;
                }
                WebApplicationException e = (WebApplicationException)o;
                return e.getResponse().getStatus() == status;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("invalid status code");
            }
        };
    }

    private static QueryRequest buildRequest() {
        final QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);

        final Source source = new Source();
        source.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        source.setAttribute("ifInOctets");
        source.setAggregation("AVERAGE");
        source.setLabel("octetsIn");
        request.setSources(Lists.newArrayList(source));

        return request;
    }

    @Test
    public void canRetrieveFallbackAttributeWhenAttributeNotFound() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        // Average
        Source ifInOctetsAvg = new Source();
        ifInOctetsAvg.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctetsAvg.setAttribute("willNotBeFound");
        ifInOctetsAvg.setFallbackAttribute("ifInOctets");
        ifInOctetsAvg.setAggregation("AVERAGE");
        ifInOctetsAvg.setLabel("ifInOctetsAvg");

        request.setSources(Lists.newArrayList(
                ifInOctetsAvg
        ));

        // Perform the query
        QueryResponse response = m_svc.query(request);

        // Validate the results
        long timestamps[] = response.getTimestamps();
        final Map<String, double[]> columns = response.columnsWithLabels();

        assertEquals(3600000L, response.getStep());
        assertEquals(680, timestamps.length);

        // Verify the values at an arbitrary index
        final int idx = 8;
        assertEquals(1414630800000L, timestamps[idx]);
        assertEquals(270.66140826873385, columns.get("ifInOctetsAvg")[idx], 0.0001);
    }

    @Test
    public void canRetrieveAttributeWhenFallbackAttributeIsSet() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        // Average
        Source ifInOctetsAvg = new Source();
        ifInOctetsAvg.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctetsAvg.setAttribute("ifInOctets");
        ifInOctetsAvg.setFallbackAttribute("willNotBeFound");
        ifInOctetsAvg.setAggregation("AVERAGE");
        ifInOctetsAvg.setLabel("ifInOctetsAvg");

        request.setSources(Lists.newArrayList(
                ifInOctetsAvg
        ));

        // Perform the query
        QueryResponse response = m_svc.query(request);

        // Validate the results
        long timestamps[] = response.getTimestamps();
        final Map<String, double[]> columns = response.columnsWithLabels();

        assertEquals(3600000L, response.getStep());
        assertEquals(680, timestamps.length);

        // Verify the values at an arbitrary index
        final int idx = 8;
        assertEquals(1414630800000L, timestamps[idx]);
        assertEquals(270.66140826873385, columns.get("ifInOctetsAvg")[idx], 0.0001);
    }

    @Test(expected = javax.ws.rs.WebApplicationException.class)
    public void cannotRetrieveUnknownAttributeAndUnknownFallbackAttribute() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        // Average
        Source ifInOctetsAvg = new Source();
        ifInOctetsAvg.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctetsAvg.setAttribute("willNotBeFound");
        ifInOctetsAvg.setFallbackAttribute("willNotBeFoundToo");
        ifInOctetsAvg.setAggregation("AVERAGE");
        ifInOctetsAvg.setLabel("ifInOctetsAvg");

        request.setSources(Lists.newArrayList(
                ifInOctetsAvg
        ));

        // Perform the query - this must fail
        m_svc.query(request);
    }

    @Test
    public void canRetrieveNoMeasurementsInRelaxedMode() throws Exception {
        // Enable relaxed mode
        QueryRequest request = new QueryRequest();
        request.setRelaxed(true);
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        // Query for some attribute that doesn't exist, on an existing resource
        Source notIfInOctetsAvg = new Source();
        notIfInOctetsAvg.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        notIfInOctetsAvg.setAttribute("notIfInOctets");
        notIfInOctetsAvg.setAggregation("AVERAGE");
        notIfInOctetsAvg.setLabel("notIfInOctets");
        request.setSources(Lists.newArrayList(notIfInOctetsAvg));

        // Perform the query
        try {
            m_svc.query(request);
            fail("HTTP 204 exception expected.");
        } catch (WebApplicationException ex) {
            assertEquals(204, ex.getResponse().getStatus());
        }
    }

    protected static String findRrdtool() {
        try {
            @SuppressWarnings("unchecked")
            final Map<String,String> env = new HashMap<String,String>(EnvironmentUtils.getProcEnvironment());
            if (env.get("PATH") != null) {
                final String pathVar = env.get("PATH");
                if (!OS.isFamilyWindows()) {
                    final List<String> paths = new ArrayList<>(Arrays.asList(pathVar.split(":")));
                    paths.add("/usr/local/bin");
                    paths.add("/usr/local/sbin");
                    for (final String path : paths) {
                        final String tryme = path + File.separator + "rrdtool";
                        if (new File(tryme).exists()) {
                            return tryme;
                        }
                    }
                }
            }
            return "rrdtool";
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void assumeRrdtoolExists(final String libraryName) {
        final String libraryPath = System.getProperty("java.library.path", "");
        if (!libraryPath.contains(":/usr/local/lib")) {
            System.setProperty("java.library.path", libraryPath + ":/usr/local/lib");
        }
        boolean rrdtoolExists = false;
        try {
            final CommandLauncher cl = CommandLauncherFactory.createVMLauncher();
            final Process p = cl.exec(new CommandLine(findRrdtool()), EnvironmentUtils.getProcEnvironment());
            final int returnCode = p.waitFor();
            LOG.debug("Loading library from java.library.path={}", System.getProperty("java.library.path"));
            System.loadLibrary(libraryName);
            rrdtoolExists = returnCode == 0;
        } catch (final Exception e) {
            LOG.warn("Failed to run 'rrdtool' or libjrrd(2)? is missing.", e);
        }
        Assume.assumeTrue(rrdtoolExists);
    }
}

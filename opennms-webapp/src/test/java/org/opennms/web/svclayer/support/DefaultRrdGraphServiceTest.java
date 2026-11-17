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
package org.opennms.web.svclayer.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultRrdGraphServiceTest extends TestCase {
    private FileAnticipator m_fileAnticipator;
    
    private DefaultRrdGraphService m_service;

    private ResourceDao m_resourceDao;

    private GraphDao m_graphDao;

    private RrdDao m_rrdDao;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_fileAnticipator = new FileAnticipator(false);
        m_service = new DefaultRrdGraphService();
    }
    
    @Override
    protected void tearDown() throws Exception {
        if (m_resourceDao != null) verifyNoMoreInteractions(m_resourceDao);
        if (m_graphDao != null)    verifyNoMoreInteractions(m_graphDao);
        m_fileAnticipator.tearDown();
    }
    
    public void testAfterPropertiesSet() {
        setUpAll();
    }

    public void testNoResourceDao() throws Exception {
        setUpGraphDao();
        setUpRrdDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("resourceDao property has not been set"));
        
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testNoGraphDao() {
        setUpResourceDao();
        setUpRrdDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("graphDao property has not been set"));
        
        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testNoRrdDao() {
        setUpResourceDao();
        setUpGraphDao();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("rrdDao property has not been set"));

        try {
            m_service.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    public void testAdhocCommandKeepsEachValueInOneArgument() {
        AdhocGraphType type = new AdhocGraphType();
        type.setCommandPrefix("rrdtool graph - --imgformat PNG --start {1} --end {2}");
        type.setTitleTemplate("--title=\"{3}\"");
        type.setDataSourceTemplate("DEF:{4}={0}:{5}:{6}");
        type.setGraphLineTemplate("{7}:{4}#{8}:\"{9}\"");

        OnmsResource resource = mock(OnmsResource.class);
        when(resource.getRrdGraphAttributes()).thenReturn(Collections.singletonMap("ifInOctets",
                new RrdGraphAttribute("ifInOctets", "snmp/1/eth0", "ifInOctets.rrd")));

        String title = "Traffic on eth0";
        String[] dataSources = { "ifInOctets" };
        String[] aggregateFunctions = { "AVERAGE" };
        String[] colors = { "ff0000" };
        String[] dataSourceTitles = { "In Octets" };
        String[] styles = { "LINE2" };
        DefaultRrdGraphService.validateAdhocParameters(title, dataSources, aggregateFunctions, colors, dataSourceTitles, styles);

        String command = m_service.createAdHocCommand(type, resource, 1000000L, 2000000L,
                title, dataSources, aggregateFunctions, colors, dataSourceTitles, styles);

        List<String> expected = Arrays.asList("rrdtool", "graph", "-", "--imgformat", "PNG",
                "--start", "1000", "--end", "2000",
                "--title=Traffic on eth0",
                "DEF:ds0=snmp/1/eth0/ifInOctets.rrd:ifInOctets:AVERAGE",
                "LINE2:ds0#ff0000:In Octets");
        assertEquals(expected, Arrays.asList(StringUtils.createCommandArray(command)));
    }

    public void testAdhocCommandKeepsQuotesAndBracesInValues() {
        AdhocGraphType type = new AdhocGraphType();
        type.setCommandPrefix("rrdtool graph - --imgformat PNG --start {1} --end {2}");
        type.setTitleTemplate("--title=\"{3}\"");
        type.setDataSourceTemplate("DEF:{4}={0}:{5}:{6}");
        type.setGraphLineTemplate("{7}:{4}#{8}:\"{9}\"");

        // A foreign source or foreign ID can contain ' and { characters.
        OnmsResource resource = mock(OnmsResource.class);
        when(resource.getRrdGraphAttributes()).thenReturn(Collections.singletonMap("ifInOctets",
                new RrdGraphAttribute("ifInOctets", "snmp/fs/It's{0}/eth0", "ifInOctets.rrd")));

        String title = "It's a {graph} {3}";
        String[] dataSources = { "ifInOctets" };
        String[] aggregateFunctions = { "AVERAGE" };
        String[] colors = { "ff0000" };
        String[] dataSourceTitles = { "It's {1} {graph}" };
        String[] styles = { "LINE2" };
        DefaultRrdGraphService.validateAdhocParameters(title, dataSources, aggregateFunctions, colors, dataSourceTitles, styles);

        String command = m_service.createAdHocCommand(type, resource, 1000000L, 2000000L,
                title, dataSources, aggregateFunctions, colors, dataSourceTitles, styles);

        List<String> expected = Arrays.asList("rrdtool", "graph", "-", "--imgformat", "PNG",
                "--start", "1000", "--end", "2000",
                "--title=It's a {graph} {3}",
                "DEF:ds0=snmp/fs/It's{0}/eth0/ifInOctets.rrd:ifInOctets:AVERAGE",
                "LINE2:ds0#ff0000:It's {1} {graph}");
        assertEquals(expected, Arrays.asList(StringUtils.createCommandArray(command)));
    }

    public void testAdhocAcceptsValuesFromTheUserInterface() {
        for (String aggregateFunction : new String[] { "AVERAGE", "MIN", "MAX", "LAST" }) {
            for (String style : new String[] { "LINE1", "LINE2", "LINE3", "AREA", "STACK" }) {
                DefaultRrdGraphService.validateAdhocParameters("It's a {graph} \\ title",
                        new String[] { "ds" }, new String[] { aggregateFunction }, new String[] { "00FF00cc" },
                        new String[] { "Data Source 1" }, new String[] { style });
            }
        }
    }

    public void testAdhocRejectsArgumentInjection() {
        String[][] badValues = {
                // title, aggregate function, color, data source title, style
                { "x\" --daemon \"127.0.0.1:4242", "AVERAGE", "ff0000", "In", "LINE2" },
                { "x\n--daemon 127.0.0.1:4242", "AVERAGE", "ff0000", "In", "LINE2" },
                { "Title", "AVERAGE --daemon 127.0.0.1:4242", "ff0000", "In", "LINE2" },
                { "Title", "AVERAGE", "ff0000 --daemon 127.0.0.1:4242", "In", "LINE2" },
                { "Title", "AVERAGE", "ff0000", "In\" --daemon \"127.0.0.1:4242", "LINE2" },
                { "Title", "AVERAGE", "ff0000", "In\t--daemon", "LINE2" },
                { "Title", "AVERAGE", "ff0000", "In", "--daemon 127.0.0.1:4242 LINE2" },
                { "Title", "AVERAGE", "#ff0000", "In", "LINE2" },
                { "Title", null, "ff0000", "In", "LINE2" },
                { null, "AVERAGE", "ff0000", "In", "LINE2" },
        };
        for (String[] bad : badValues) {
            try {
                DefaultRrdGraphService.validateAdhocParameters(bad[0], new String[] { "ds" },
                        new String[] { bad[1] }, new String[] { bad[2] }, new String[] { bad[3] }, new String[] { bad[4] });
                fail("Expected IllegalArgumentException for " + Arrays.toString(bad));
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    public void testAdhocAcceptsDefaultWizardSubmission() {
        // The wizard selects one data source and sends four values for each other parameter.
        DefaultRrdGraphService.validateAdhocParameters("Graph Title", new String[] { "ifInOctets" },
                new String[] { "AVERAGE", "AVERAGE", "AVERAGE", "AVERAGE" },
                new String[] { "ff0000", "00ff00", "0000ff", "ffff00" },
                new String[] { "Data Source 1", "Data Source 2", "Data Source 3", "Data Source 4" },
                new String[] { "LINE2", "LINE2", "LINE2", "LINE2" });
    }

    public void testAdhocRejectsMismatchedArrays() {
        try {
            DefaultRrdGraphService.validateAdhocParameters("Title", new String[] { "ds0", "ds1" },
                    new String[] { "AVERAGE" }, new String[] { "ff0000", "00ff00" },
                    new String[] { "In", "Out" }, new String[] { "LINE2", "LINE2" });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void setUpAll() {
        setUpResourceDao();
        setUpGraphDao();
        setUpRrdDao();
        m_service.afterPropertiesSet();
    }
    
    private void setUpResourceDao() {
        m_resourceDao = mock(ResourceDao.class);
        m_service.setResourceDao(m_resourceDao);
    }
    
    private void setUpGraphDao() {
        m_graphDao = mock(GraphDao.class);
        m_service.setGraphDao(m_graphDao);
    }
    
    private void setUpRrdDao() {
        m_rrdDao = mock(RrdDao.class);
        m_service.setRrdDao(m_rrdDao);
    }
    
}

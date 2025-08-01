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
package org.opennms.netmgt.config.pagesequence;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class PageSequenceTest extends XmlTestNoCastor<PageSequence> {
    public PageSequenceTest(final PageSequence sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Before
    public void setUp() {
        super.setUp();
        final Properties props = new Properties();
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.JaxbUtils", "TRACE");
        props.put(MockLogger.LOG_KEY_PREFIX + "org.opennms.core.xml.JaxbClassObjectAdapter", "TRACE");
        MockLogAppender.setupLogging(true, props);
    }

    @Override
    protected boolean ignoreNamespace(final String namespace) {
        return "http://xmlns.opennms.org/xsd/page-sequence".equals(namespace);
    }
    
    protected String getSchemaFile() {
        return "target/classes/xsds/page-sequence.xsd";
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getPageSequence(),
                    new File("target/test-classes/org/opennms/netmgt/config/pagesequence/page-sequence.xml")
                }
        });
    }

    protected static PageSequence getPageSequence() {
        final PageSequence ps = new PageSequence();

        Page page = new Page();
        page.setMethod("GET");
        page.setHttpVersion("1.1");
        page.setScheme("http");
        page.setHost("${ipaddr}");
        page.setDisableSslVerification("true");
        page.setPort(7080);
        page.setPath("/Login.do");
        page.setSuccessMatch("(HQ Login)|(Sign in to Hyperic HQ)");
        page.setResponseRange("100-399");
        ps.addPage(page);

        page = new Page();
        page.setMethod("POST");
        page.setHttpVersion("1.1");
        page.setScheme("http");
        page.setHost("${ipaddr}");
        page.setDisableSslVerification("true");
        page.setPort(7080);
        page.setPath("/j_security_check.do");
        page.setFailureMatch("(?s)(The username or password provided does not match our records)|(You are not signed in)");
        page.setFailureMessage("HQ Login in Failed");
        page.setSuccessMatch("HQ Dashboard");
        page.setResponseRange("100-399");
        org.opennms.netmgt.config.pagesequence.Parameter parameter = new org.opennms.netmgt.config.pagesequence.Parameter();
        parameter.setKey("j_username");
        parameter.setValue("hqadmin");
        page.addParameter(parameter);
        parameter = new org.opennms.netmgt.config.pagesequence.Parameter();
        parameter.setKey("j_password");
        parameter.setValue("hqadmin");
        page.addParameter(parameter);
        ps.addPage(page);

        page = new Page();
        page.setPath("/Logout.do");
        page.setPort(7080);
        page.setSuccessMatch("HQ Login");
        page.addHeader(new Header("foo", "bar"));
        ps.addPage(page);

        return ps;
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
        ps.addPage(page);

        return ps;
    }
}

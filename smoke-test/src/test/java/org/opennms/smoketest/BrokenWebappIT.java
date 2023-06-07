/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

public class BrokenWebappIT {
    @ClassRule
    public static final OpenNMSStack BROKEN_WEBAPP = OpenNMSStack.minimal(
            b -> b.withFile(getBrokenLdapXml(), "jetty-webapps/opennms/WEB-INF/spring-security.d/ldap.xml"),
            b -> b.withWaitStrategy(c -> new AbstractWaitStrategy() {
                        @Override
                        protected void waitUntilReady() {
                        }
                    }));

    public static URL getBrokenLdapXml() {
        try {
            var source = Path.of("../opennms-webapp/src/main/webapp/WEB-INF/spring-security.d/ldap.xml.disabled");
            var xml = Files.readString(source);

            // Remove an uncommented <beans:entry> element to create an XML syntax error
            var brokenXml = xml.replaceFirst("(?m)<beans:entry>(\\s*<!-- Name of the LDAP group for OpenNMS administrators -->)", "$1");
            if (xml.equals(brokenXml)) {
                fail("No substitutions were done in ldap.xml content\n" + xml);
            }

            var tmpDir = Files.createTempDirectory(BrokenWebappIT.class.getSimpleName());
            var ldapXml = Path.of(tmpDir + "/" + "ldap.xml");
            Files.writeString(ldapXml, brokenXml);

            return ldapXml.toUri().toURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testOpenNMSStartupFailsAsExpected() throws InterruptedException {
        var waitStrategy = new OpenNMSContainer.WaitForOpenNMS(BROKEN_WEBAPP.opennms());
        try {
            waitStrategy.waitUntilReady(BROKEN_WEBAPP.opennms());
        } catch (Exception e) {
            assertThat("Exception message", e.getMessage(), containsString("container is no longer running"));
            assertThat("Container logs after container exits",
                    BROKEN_WEBAPP.opennms().getLogs(),
                    containsString("An error occurred invoking operation start on MBean OpenNMS:Name=JettyServer"));
            return;
        }

        fail("Should have received an exception waiting for OpenNMS startup");
    }
}

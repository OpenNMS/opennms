/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.protocols.sftp;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ThreadCategory;

/**
 * The Class Sftp3gppUrlConnectionTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppUrlConnectionTest {

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        try {
            new URL("sftp://admin:admin@localhost/");
            new URL("sftp+3gpp://admin:admin@localhost/");
        } catch (MalformedURLException e) {
            log().info("Registering supported protocols");
            URL.setURLStreamHandlerFactory(new SftpUrlFactory());
        }
    }

    /**
     * Test path for 3GPP (NE Mode ~ A).
     *
     * @throws Exception the exception
     */
    @Test
    public void testPathFor3GPPA() throws Exception {
        URL url = new URL("sftp+3gpp://admin:admin@192.168.1.1/opt/hitachi/cnp/data/pm/reports/3gpp/5/___CURRENT_3GPP_A_FORMAT?step=300&tz-offset=GMT-5&neId=MME00001");
        Sftp3gppUrlConnection c = (Sftp3gppUrlConnection) url.openConnection();
        String path = c.getPath();
        log().debug(path);
    }

    /**
     * Log.
     *
     * @return the thread category
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}

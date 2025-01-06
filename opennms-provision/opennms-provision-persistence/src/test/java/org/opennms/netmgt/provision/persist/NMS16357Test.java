/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class NMS16357Test {

    private void checkUrl(final String url) throws MalformedURLException {
        final String modifiedUrl = AbstractForeignSourceRepository.stripCredentials(new URL(url));
        System.out.println(modifiedUrl);
        Assert.assertFalse(modifiedUrl.contains("secretuser"));
        Assert.assertFalse(modifiedUrl.contains("secretpass"));
    }

    @Test
    public void testStrip() throws MalformedURLException {
        checkUrl("http://foo.bar.com?password=secretpass&username=secretuser");
        checkUrl("http://foo.bar.com?username=secretuser&password=secretpass");
        checkUrl("http://foo.bar.com?username=secretuser1&username=secretuser2&password=secretpass1&password=secretpass2");
        checkUrl("http://foo.bar.com?username=secretuser&password=secretpass&something=else");
        checkUrl("http://foo.bar.com?username=secretuser&password=secretpass&something=else");
        checkUrl("http://foo.bar.com?something=before&username=secretuser&password=secretpass&something=else");
    }
}

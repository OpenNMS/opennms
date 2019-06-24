/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.opennms.smoketest.utils.DevDebugUtils.CONTAINER_HOST_M2_SYS_PROP;

import java.util.concurrent.Callable;

import org.junit.Test;

public class DevDebugUtilsTest {

    @Test
    public void canConvertToContainerAccessibleUrl() {
        withClearedContainerHost(() -> {
            assertThat(DevDebugUtils.convertToContainerAccessibleUrl("http://127.0.0.1:39995/path?query=x", "opennms", 8980),
                    equalTo("http://opennms:8980/path?query=x"));
            return null;
        });

        withClearedContainerHost(() -> {
            System.setProperty(CONTAINER_HOST_M2_SYS_PROP, "beer");
            assertThat(DevDebugUtils.convertToContainerAccessibleUrl("http://127.0.0.1:39995/path?query=x", "opennms", 8980),
                    equalTo("http://beer:39995/path?query=x"));
            return null;
        });
    }

    private static void withClearedContainerHost(Callable<Void> callable) {
        final String existingContainerHost = System.getProperty(CONTAINER_HOST_M2_SYS_PROP);
        try {
            System.clearProperty(CONTAINER_HOST_M2_SYS_PROP);
            callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (existingContainerHost != null) {
                System.setProperty(CONTAINER_HOST_M2_SYS_PROP, existingContainerHost);
            }
        }
    }
}

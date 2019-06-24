/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.web.HttpClientWrapperConfigHelper.PARAMETER_KEYS.useSystemProxy;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opennms.netmgt.model.notifd.Argument;

public class HttpNotificationStrategyTest {

    @Test
    public void shouldExtractUrlFromArgument() {
        String url = "myUrl";
        HttpNotificationStrategy strategy = createNotificationStrategyWithSingleArgument("url", url);
        assertEquals(url, strategy.getUrl());
    }

    @Test
    public void shouldExtractUrlFromArgumentAsPrefix() {
        String url = "myUrl";
        HttpNotificationStrategy strategy = createNotificationStrategyWithSingleArgument("urlWithSuffix", url);
        assertEquals(url, strategy.getUrl());
    }

    @Test
    public void shouldExtractUseSystemProperty() {
        HttpNotificationStrategy strategy = createNotificationStrategyWithSingleArgument(useSystemProxy.name(), "true");
        assertEquals(true, strategy.getUseSystemProxy());
    }

    @Test
    public void shouldExtractUseSystemPropertyAsPrefix() {
        HttpNotificationStrategy strategy = createNotificationStrategyWithSingleArgument(useSystemProxy.name()+"WithSuffix", "true");
        assertEquals(true, strategy.getUseSystemProxy());
    }

    private HttpNotificationStrategy createNotificationStrategyWithSingleArgument(String name, String value){
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument(name, null, value, false));
        HttpNotificationStrategy strategy = new HttpNotificationStrategy();
        strategy.setArguments(arguments);
        return strategy;
    }
}


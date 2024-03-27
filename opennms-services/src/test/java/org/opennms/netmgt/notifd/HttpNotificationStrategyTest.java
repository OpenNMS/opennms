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


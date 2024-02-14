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
package org.opennms.osgi;

import org.opennms.osgi.locator.OnmsServiceManagerLocator;
import org.osgi.framework.BundleContext;

public class VaadinApplicationContextImpl implements VaadinApplicationContext {
    private String sessionId;
    private String userName;
    private int uiId;

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    @Override
    public EventProxy getEventProxy(OnmsServiceManager serviceManager) {
        if (serviceManager == null) throw new IllegalArgumentException("OnmsServiceManager must not be null");
        EventRegistry eventRegistry = serviceManager.getEventRegistry();
        if (eventRegistry == null) throw new IllegalArgumentException("EventRegistry must not be null");
        return eventRegistry.getScope(this);
    }

    @Override
    public EventProxy getEventProxy(BundleContext bundleContext) {
        if (bundleContext == null) throw new IllegalArgumentException("BundleContext must not be null");
        return getEventProxy(new OnmsServiceManagerLocator().lookup(bundleContext));
    }

    public void setUiId(int uiId) {
        this.uiId = uiId;
    }

    @Override
    public int getUiId() {
        return uiId;
    }
}

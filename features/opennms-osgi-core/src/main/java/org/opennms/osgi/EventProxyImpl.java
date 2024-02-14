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

import java.util.List;

class EventProxyImpl implements EventProxy {

    private final BundleContext bundleContext;
    private final VaadinApplicationContext applicationContext;

    protected EventProxyImpl(BundleContext bundleContext, VaadinApplicationContext applicationContext) {
        this.bundleContext = bundleContext;
        this.applicationContext = applicationContext;
    }

    /**
     * Fires an event and notifies all {@link EventListener} registered in the EventRegistry.
     * Be aware that only {@linkplain EventListener}s within session-scope and those listeners who 
     * listens to events of type T gets notified.
     * 
     * @see {@link EventListener}
     */
    @Override
    public <T> void fireEvent(T eventObject) {
        if (eventObject == null) return;
        List<EventListener> eventListeners = getServiceManager().getServices(EventListener.class, applicationContext, EventListener.getProperties(eventObject.getClass()));
        for (EventListener eachListener : eventListeners) {
            eachListener.invoke(eventObject);
        }
    }

    @Override
    public <T> void addPossibleEventConsumer(T possibleEventConsumer) {
        getServiceManager().getEventRegistry().addPossibleEventConsumer(possibleEventConsumer, applicationContext);
    }

    private OnmsServiceManager getServiceManager() {
        return new OnmsServiceManagerLocator().lookup(bundleContext);
    }
}

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
package org.opennms.vaadin.extender;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinSession;

public class SessionListenerRepository implements SessionInitListener, SessionDestroyListener {

    private final static Logger LOG = LoggerFactory.getLogger(SessionListenerRepository.class);

    private final BundleContext context;
    private Map<VaadinSession, String> vaadinSessionIdMap = new HashMap<VaadinSession, String>();

    public static SessionListenerRepository getRepository(BundleContext context) {
        ServiceReference reference = context.getServiceReference(SessionListenerRepository.class.getName());
        return (SessionListenerRepository)context.getService(reference);
    }

    public SessionListenerRepository(BundleContext context) {
        this.context = context;
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        final String sessionId = vaadinSessionIdMap.get(event.getSession());
        if (sessionId == null) throw new IllegalArgumentException("Unknown session : " + event.getSession());
        for (SessionListener eachListener : getSessionListeners()) {
            eachListener.sessionDestroyed(sessionId);
        }
        vaadinSessionIdMap.remove(event.getSession());
    }

    @Override
    public void sessionInit(SessionInitEvent event) throws ServiceException {
        vaadinSessionIdMap.put(event.getSession(), event.getSession().getSession().getId());
        for (SessionListener eachListener : getSessionListeners()) {
            eachListener.sessionInitialized(vaadinSessionIdMap.get(event.getSession()));
        }
    }

    private List<SessionListener> getSessionListeners() {
        final List<SessionListener> sessionListeners = new ArrayList<>();
        try {
            final ServiceReference[] references = context.getAllServiceReferences(SessionListener.class.getName(), null);
            if (references != null) {
                for (ServiceReference eachReference : references) {
                    Object service = context.getService(eachReference);
                    if (service == null) continue;
                    sessionListeners.add((SessionListener)service);
                }
            }
        } catch (InvalidSyntaxException e) {
            LOG.error("Error retrieving SessionListeners", e);
        }
        return sessionListeners;
    }
}

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
package org.opennms.netmgt.dao.support;

import java.io.Closeable;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.dao.api.FilterWatcher;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.filter.api.FilterDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

@EventListener(name = "FilterWatcher")
public class DefaultFilterWatcher implements FilterWatcher, InitializingBean, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFilterWatcher.class);

    private static final String MATCH_ANY_RULE = "IPADDR != '0.0.0.0'";

    private static final String REFRESH_RATE_LIMIT_MS_SYS_PROP = "org.opennms.netmgt.dao.support.filterServiceRefreshRateLimitMs";
    private static final long DEFAULT_REFRESH_RATE_LIMIT_MS = TimeUnit.SECONDS.toMillis(30);
    private static final long REFRESH_RATE_LIMIT_MS = SystemProperties.getLong(REFRESH_RATE_LIMIT_MS_SYS_PROP, DEFAULT_REFRESH_RATE_LIMIT_MS);

    @Autowired
    private FilterDao filterDao;
    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SessionUtils sessionUtils;

    private long refreshRateLimitMs = REFRESH_RATE_LIMIT_MS;

    private final Timer timer = new Timer("FilterService-Timer");

    private final Map<String, FilterSession> sessionByRule = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        long timerIntevalMs = Math.min(refreshRateLimitMs, TimeUnit.SECONDS.toMillis(5));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sessionByRule.values().forEach(s -> {
                        try {
                            s.refreshIfNeeded();
                        } catch (Exception e) {
                            LOG.warn("Error refreshing filter for rule: {}. Will retry again in {}ms.", s.rule, timerIntevalMs, e);
                        }
                    });
                } catch (Exception e) {
                    LOG.warn("Error refreshing filter results. Will retry again in {}ms.", timerIntevalMs, e);
                }
            }
        }, timerIntevalMs, timerIntevalMs);
    }

    @Override
    public void destroy() {
        timer.cancel();
    }

    @Override
    public Closeable watch(String filterRule, Consumer<FilterResults> callback) {
        String effectiveFilterRule;
        if (StringUtils.isEmpty(filterRule)) {
            effectiveFilterRule = MATCH_ANY_RULE;
        } else {
            effectiveFilterRule = filterRule.trim();
        }

        final FilterSession session;
        synchronized (sessionByRule) {
            // Create a new session if necessary
            session = sessionByRule.computeIfAbsent(effectiveFilterRule, FilterSession::new);
            // Register the callback with the session
            session.addCallback(callback);
        }

        // Remove the callback and close any sessions we no longer need
        return () -> {
            synchronized (sessionByRule) {
                session.removeCallback(callback);
                garbageCollectSessions();
            }
        };
    }

    private void garbageCollectSessions() {
        List<FilterSession> sessionsToRemove = sessionByRule.values().stream()
                .filter(s -> s.callbacks.isEmpty())
                .collect(Collectors.toList());
        for (FilterSession session : sessionsToRemove) {
            sessionByRule.remove(session.rule);
        }
    }

    @EventHandler(ueis = {
            EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
            EventConstants.SERVICE_DELETED_EVENT_UEI,
            EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI,
            EventConstants.NODE_LOCATION_CHANGED_EVENT_UEI,
            EventConstants.NODE_ADDED_EVENT_UEI,
            EventConstants.NODE_DELETED_EVENT_UEI,
            EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI,
            EventConstants.INTERFACE_DELETED_EVENT_UEI,
            EventConstants.INTERFACE_REPARENTED_EVENT_UEI,
            // Issued when the status changes on a service
            EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI,
            EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI
    })
    public void inventoryChangeEventHandler(final IEvent event) {
        // Filters can depend on arbitrary node fields & relationships so we need to refresh these periodically
        sessionByRule.values().forEach(FilterSession::requestRefresh);
    }
    
    private static class FilterResultsImpl implements FilterResults {
        private final Map<Integer, Map<InetAddress, Set<String>>> nodeIpServiceMap;
        private final NodeDao nodeDao;
        public FilterResultsImpl(Map<Integer, Map<InetAddress, Set<String>>> nodeIpServiceMap, NodeDao nodeDao) {
            this.nodeIpServiceMap = Objects.requireNonNull(nodeIpServiceMap);
            this.nodeDao = Objects.requireNonNull(nodeDao);
        }

        @Override
        public Map<Integer, Map<InetAddress, Set<String>>> getNodeIpServiceMap() {
            return nodeIpServiceMap;
        }

        @Override
        public Set<ServiceRef> getServicesNamed(String serviceName) {
            Set<ServiceRef> serviceRefs = new LinkedHashSet<>();
            for (Map.Entry<Integer, Map<InetAddress, Set<String>>> nodeEntry : nodeIpServiceMap.entrySet()) {
                int nodeId = nodeEntry.getKey();
                for (Map.Entry<InetAddress, Set<String>> interfaceEntry : nodeEntry.getValue().entrySet()) {
                    InetAddress interfaceAddress = interfaceEntry.getKey();
                    if (interfaceEntry.getValue().contains(serviceName)) {
                        serviceRefs.add(new ServiceRef(nodeId, interfaceAddress, serviceName,nodeDao.getLocationForId(nodeId)));
                    }
                }
            }
            return serviceRefs;
        }
    }

    private class FilterSession {
        private final String rule;
        private final List<Consumer<FilterResults>> callbacks = new LinkedList<>();
        private final AtomicReference<FilterResults> lastFilterResultsRef = new AtomicReference<>();
        private long lastRefreshedMs;
        private long lastRefreshRequestMs;

        public FilterSession(String rule) {
            this.rule = Objects.requireNonNull(rule);
            filterDao.validateRule(rule);
        }

        public synchronized void addCallback(Consumer<FilterResults> callback) {
            callbacks.add(callback);

            // Request a refresh whenever callback are added
            if(requestRefresh()) {
                // Our request actually triggered a refresh, so the callback was already made
                return;
            }

            final FilterResults lastFilterResults = lastFilterResultsRef.get();
            if (lastFilterResults != null) {
                callback.accept(lastFilterResults);
            }
        }

        public synchronized void removeCallback(Consumer<FilterResults> callback) {
            callbacks.remove(callback);
        }

        public synchronized void refreshNow() {
            lastRefreshedMs = System.currentTimeMillis();
            LOG.debug("Refreshing results for filter rule: {}", rule);
            FilterResults newFilterResults = sessionUtils.withReadOnlyTransaction(() ->
                    new FilterResultsImpl(filterDao.getNodeIPAddressServiceMap(rule),nodeDao));
            LOG.debug("Done refreshing results for rule.");

            final FilterResults lastFilterResults = lastFilterResultsRef.get();
            if (Objects.equals(lastFilterResults, newFilterResults)) {
                // nothing has changed, noop
                return;
            }

            lastFilterResultsRef.set(newFilterResults);
            notifyCallbacks(newFilterResults);
        }

        public synchronized boolean refreshIfNeeded() {
            if (lastRefreshRequestMs > 0
                    && lastRefreshRequestMs >= lastRefreshedMs
                    && System.currentTimeMillis() - lastRefreshedMs >= refreshRateLimitMs) {
                lastRefreshRequestMs = 0;
                refreshNow();
                return true;
            }
            return false;
        }

        public synchronized boolean requestRefresh() {
            lastRefreshRequestMs = System.currentTimeMillis();
            return refreshIfNeeded();
        }

        private void notifyCallbacks(FilterResults results) {
            callbacks.forEach(c -> {
                try {
                    c.accept(results);
                } catch (Exception e) {
                    LOG.warn("Error notifying callback: {} for results of filter rule: {}.", c, rule, e);
                }
            });
        }
    }

    public void setRefreshRateLimitMs(long refreshRateLimitMs) {
        this.refreshRateLimitMs = refreshRateLimitMs;
    }

    public long getRefreshRateLimitMs() {
        return refreshRateLimitMs;
    }

    public void setFilterDao(FilterDao filterDao) {
        this.filterDao = filterDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }
}

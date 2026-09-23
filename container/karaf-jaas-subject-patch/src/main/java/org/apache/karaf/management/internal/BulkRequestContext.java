/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.management.internal;

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.apache.karaf.util.jaas.JaasHelper;

/**
 * <p>Class to optimize ConfigAdmin access with the lifecycle of single
 * {@link org.apache.karaf.management.JMXSecurityMBean#canInvoke(Map) bulk query invocation}. This prevents countless
 * {@link org.osgi.service.cm.ConfigurationAdmin#listConfigurations(String) listings of ConfigAdmin configurations}
 * for each checked MBean/method.</p>
 * <p>Access to this object doesn't have to be synchronized, as it is passed down the <code>canInvoke</code> chain.</p>
 */
public class BulkRequestContext {

    private List<String> allPids = new ArrayList<>();
    private List<Dictionary<String, Object>> whiteListProperties = new ArrayList<>();

    private ConfigurationAdmin configAdmin;

    // if there's AccessControlContext or subject, we can fail fast
    private boolean anonymous = false;
    // otherwise we can cache current subject's principals for faster access
    private Set<Principal> principals = new HashSet<>();

    // cache with lifecycle bound to BulkRequestContext instance
    private Map<String, Dictionary<String, Object>> cachedConfigurations = new HashMap<>();

    private BulkRequestContext() {}

    public static BulkRequestContext newContext(ConfigurationAdmin configAdmin) throws IOException {
        BulkRequestContext context = new BulkRequestContext();
        context.configAdmin = configAdmin;
        try {
            // check JAAS subject here
            // OPENNMS: Subject.getSubject() always throws on JDK 23+
            Subject subject = JaasHelper.currentSubject();
            if (subject == null) {
                context.anonymous = true;
            } else {
                context.principals.addAll(subject.getPrincipals());
            }
            // list available ACL configs - valid for this instance only
            for (Configuration config : configAdmin.listConfigurations("(service.pid=jmx.acl*)")) {
                context.allPids.add(config.getPid());
            }
            // list available ACT whitelist configs
            Configuration[] configs = configAdmin.listConfigurations("(service.pid=jmx.acl.whitelist)");
            if (configs != null) {
                for (Configuration config : configs) {
                    context.whiteListProperties.add(config.getProperties());
                }
            }
        } catch (InvalidSyntaxException ise) {
            throw new RuntimeException(ise);
        }

        return context;
    }

    /**
     * Return list of PIDs related to RBAC/ACL.
     *
     * @return The list of PIDs.
     */
    public List<String> getAllPids() {
        return allPids;
    }

    /**
     * Return list of configurations from the whitelist.
     *
     * @return The list of configurations.
     */
    public List<Dictionary<String,Object>> getWhitelistProperties() {
        return whiteListProperties;
    }

    /**
     * Return {@link Configuration ConfigAdmin configuration} - may be cached in this instance of
     * {@link BulkRequestContext context}
     *
     * @param generalPid The configuration PID.
     * @return The configuration.
     * @throws IOException If an error ocurrs while retrieving the configuration.
     */
    public Dictionary<String, Object> getConfiguration(String generalPid) throws IOException {
        if (!cachedConfigurations.containsKey(generalPid)) {
            cachedConfigurations.put(generalPid, configAdmin.getConfiguration(generalPid, null).getProperties());
        }
        return cachedConfigurations.get(generalPid);
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public Set<Principal> getPrincipals() {
        return principals;
    }

}

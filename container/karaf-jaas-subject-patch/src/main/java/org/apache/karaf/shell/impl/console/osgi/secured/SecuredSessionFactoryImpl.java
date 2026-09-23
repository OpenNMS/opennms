/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.shell.impl.console.osgi.secured;

import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.security.auth.Subject;

import org.apache.felix.gogo.runtime.CommandNotFoundException;
import org.apache.felix.gogo.runtime.CommandSessionImpl;
import org.apache.felix.service.command.Function;
import org.apache.felix.service.threadio.ThreadIO;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.service.guard.tools.ACLConfigurationParser;
import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.impl.console.SessionFactoryImpl;
import org.apache.karaf.util.tracker.SingleServiceTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.karaf.util.jaas.JaasHelper;

public class SecuredSessionFactoryImpl extends SessionFactoryImpl implements ConfigurationListener {

    private static final String PROXY_COMMAND_ACL_PID_PREFIX = "org.apache.karaf.command.acl.";
    private static final String CONFIGURATION_FILTER = "(" + Constants.SERVICE_PID + "=" + PROXY_COMMAND_ACL_PID_PREFIX + "*)";

    private static final String SHELL_SCOPE = "shell";
    private static final String SHELL_INVOKE = ".invoke";
    private static final String SHELL_REDIRECT = ".redirect";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuredSessionFactoryImpl.class);

    private BundleContext bundleContext;
    private Map<String, Dictionary<String, Object>> scopes = new HashMap<>();
    private SingleServiceTracker<ConfigurationAdmin> configAdminTracker;
    private ServiceRegistration<ConfigurationListener> registration;
    private ThreadLocal<Map<Object, Boolean>> serviceVisibleMap = new ThreadLocal<>();
    private Map<Thread, Map<Object, Boolean>> serviceVisibleMapForAllThreads = new WeakHashMap<>();

    public SecuredSessionFactoryImpl(BundleContext bundleContext, ThreadIO threadIO) throws InvalidSyntaxException {
        super(threadIO);
        this.bundleContext = bundleContext;
        this.registration = bundleContext.registerService(ConfigurationListener.class, this, null);
        this.configAdminTracker = new SingleServiceTracker<>(bundleContext, ConfigurationAdmin.class, this::update);
        this.configAdminTracker.open();
    }

    public void stop() {
        this.registration.unregister();
        this.configAdminTracker.close();
        super.stop();
    }

    @Override
    protected Object invoke(CommandSessionImpl session, Object target, String name, List<Object> args) throws Exception {
        checkSecurity(SHELL_SCOPE, SHELL_INVOKE, Arrays.asList(target, name, args));
        return super.invoke(session, target, name, args);
    }

    @Override
    protected Path redirect(CommandSessionImpl session, Path path, int mode) {
        checkSecurity(SHELL_SCOPE, SHELL_REDIRECT, Arrays.asList(path, mode));
        return super.redirect(session, path, mode);
    }

    @Override
    protected Function wrap(Command command) {
        return new SecuredCommand(this, command);
    }

    @Override
    protected boolean isVisible(Object service) {
        if (this.serviceVisibleMap.get() == null) {
            this.serviceVisibleMap.set(new HashMap<>());
            Thread cur = Thread.currentThread();
            this.serviceVisibleMapForAllThreads.put(cur, this.serviceVisibleMap.get());
        }
        if (this.serviceVisibleMap.get().get(service) != null) {
            return this.serviceVisibleMap.get().get(service);
        }
        if (service instanceof Command) {
            Command cmd = (Command) service;
            boolean ret = isVisible(cmd.getScope(), cmd.getName());
            this.serviceVisibleMap.get().put(service, ret);
            return ret;
        } else {
            boolean ret = super.isVisible(service);
            this.serviceVisibleMap.get().put(service, ret);
            return ret;
        }
    }

    public boolean isVisible(String scope, String name) {
        boolean visible = true;
        Dictionary<String, Object> config = getScopeConfig(scope);
        if (config != null) {
            visible = false;
            List<String> roles = new ArrayList<>();
            ACLConfigurationParser.getRolesForInvocation(name, null, null, config, roles);
            if (roles.isEmpty()) {
                visible = true;
            } else {
                for (String role : roles) {
                    if (currentUserHasRole(role)) {
                        visible = true;
                    }
                }
            }
        } 
        AliasCommand aliasCommand = findAlias(scope, name);
        if (aliasCommand != null) {
            visible = visible && isAliasVisible(aliasCommand.getScope(), aliasCommand.getName());
        }
        return visible;
    }

    public boolean isAliasVisible(String scope, String name) {
        Dictionary<String, Object> config = getScopeConfig(scope);
        if (config != null) {
            List<String> roles = new ArrayList<>();
            ACLConfigurationParser.getRolesForInvocationForAlias(name, null, null, config, roles);
            if (roles.isEmpty()) {
                return true;
            } else {
                for (String role : roles) {
                    if (currentUserHasRole(role)) {
                        return true;
                    }
                }
                return false;
            }
        } 
        return true;
    }
       
    private AliasCommand findAlias(String scope, String name) {
        if (session != null) {
            Set<String> vars = ((Set<String>) session.get(null));
            String aliasScope = null;
            String aliasName = null;
            for (String var : vars) {
                Object content = session.get(var);
                if (content != null && "org.apache.felix.gogo.runtime.Closure".equals(content.getClass().getName())) {

                    int index = var.indexOf(":");
                    if (index > 0) {
                        aliasScope = var.substring(0, index);
                        aliasName = var.substring(index + 1);
                        String originalCmd = content.toString();
                        index = originalCmd.indexOf(" ");
                        Object securityCmd = null;
                        if (index > 0) {
                            securityCmd = ((org.apache.felix.gogo.runtime.Closure)content)
                                .get(originalCmd.substring(0, index));
                        }
                        if (securityCmd instanceof SecuredCommand) {
                            if (((SecuredCommand)securityCmd).getScope().equals(scope)
                               && ((SecuredCommand)securityCmd).getName().equals(name)) {
                                return new AliasCommand(aliasScope, aliasName);
                            }
                        }
                    }
                }
                
            }
        }
        return null;
    }
    
    
    void checkSecurity(String scope, String name, List<Object> arguments) {
       
        Dictionary<String, Object> config = getScopeConfig(scope);
        boolean passCheck = false;
        if (config != null) {
            if (!isVisible(scope, name)) {
                throw new CommandNotFoundException(scope + ":" + name);
            }
            List<String> roles = new ArrayList<>();
            ACLConfigurationParser.Specificity s = ACLConfigurationParser.getRolesForInvocation(name, new Object[] { arguments.toString() }, null, config, roles);
            if (s == ACLConfigurationParser.Specificity.NO_MATCH) {
                passCheck = true;
            }
            for (String role : roles) {
                if (currentUserHasRole(role)) {
                    passCheck = true;
                }
            }
            if (!passCheck) {
                throw new SecurityException("Insufficient credentials.");
            }
        } else {
            List<String> roles = new ArrayList<>();
            ACLConfigurationParser.getCompulsoryRoles(roles);
            if (roles.size() == 0) {
                passCheck = true;
            }
            for (String role : roles) {
                if (currentUserHasRole(role)) {
                    passCheck = true;
                }
            }
            if (!passCheck) {
                throw new SecurityException("Insufficient credentials.");
            }
        }
        AliasCommand aliasCommand = findAlias(scope, name); 
        if (aliasCommand != null) {
            //this is the alias
            if (config != null) {
                if (!isAliasVisible(aliasCommand.getScope(), aliasCommand.getName())) {
                    throw new CommandNotFoundException(aliasCommand.getScope() + ":" + aliasCommand.getName());
                }
                List<String> roles = new ArrayList<>();
                ACLConfigurationParser.Specificity s = ACLConfigurationParser.getRolesForInvocationForAlias(aliasCommand.getName(), new Object[] { arguments.toString() }, null, config, roles);
                if (s == ACLConfigurationParser.Specificity.NO_MATCH) {
                    return;
                }
                for (String role : roles) {
                    if (currentUserHasRole(role)) {
                        return;
                    }
                }
                throw new SecurityException("Insufficient credentials.");
            }
        }
               
    }


    static boolean currentUserHasRole(String requestedRole) {
        String clazz;
        String role;
        boolean customClazz = false;

        int index = requestedRole.indexOf(':');
        if (index > 0) {
            clazz = requestedRole.substring(0, index);
            customClazz = true;
            role = requestedRole.substring(index + 1);
        } else {
            clazz = RolePrincipal.class.getName();
            role = requestedRole;
        }

        // OPENNMS: Subject.getSubject() always throws on JDK 23+
        Subject subject = JaasHelper.currentSubject();

        if (subject == null) {
            return false;
        }

        for (Principal p : subject.getPrincipals()) {
            if (customClazz) {
            	if(clazz.equals(p.getClass().getName()) && role.equals(p.getName())) {
            		return true;
            	}
            } else {
            	if(RolePrincipal.class.isAssignableFrom(p.getClass()) && role.equals(p.getName())) {
            		return true;
            	}
            }
        }

        return false;
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        if (!event.getPid().startsWith(PROXY_COMMAND_ACL_PID_PREFIX))
            return;

        try {
            synchronized(this.serviceVisibleMap) {
                if (this.serviceVisibleMap.get() != null) {
                    this.serviceVisibleMap.get().clear();
                }
            }
            switch (event.getType()) {
                case ConfigurationEvent.CM_DELETED:
                    removeScopeConfig(event.getPid().substring(PROXY_COMMAND_ACL_PID_PREFIX.length()));
                    break;
                case ConfigurationEvent.CM_UPDATED:
                    ConfigurationAdmin configAdmin = bundleContext.getService(event.getReference());
                    try {
                        addScopeConfig(configAdmin.getConfiguration(event.getPid(), null));
                    } finally {
                        bundleContext.ungetService(event.getReference());
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("Problem processing Configuration Event {}", event, e);
        }
    }
    
    @Override
    public void unregister(Object service) {
        synchronized (services) {
            super.unregister(service);
            removeUnregisteredSeriveForAllShell(service);
        }
    }

    private void removeUnregisteredSeriveForAllShell(Object service) {
        synchronized (this.serviceVisibleMapForAllThreads) {
            for (final Iterator<Map<Object, Boolean>> iterator = this.serviceVisibleMapForAllThreads.values().iterator();
                iterator.hasNext();) {
                Map<Object, Boolean> serviceMap = iterator.next();
                serviceMap.remove(service);
            }
        }
    }

    private void addScopeConfig(Configuration config) {
        if (!config.getPid().startsWith(PROXY_COMMAND_ACL_PID_PREFIX)) {
            // not a command scope configuration file
            return;
        }
        String scope = config.getPid().substring(PROXY_COMMAND_ACL_PID_PREFIX.length());
        if (scope.indexOf('.') >= 0) {
            // scopes don't contains dots, not a command scope
            return;
        }
        scope = scope.trim();
        synchronized (scopes) {
            if (scope.endsWith("*")) {
                scope = "star";
            }
            scopes.put(scope, config.getProcessedProperties(null));
        }
    }

    private void removeScopeConfig(String scope) {
        synchronized (scopes) {
            scopes.remove(scope);
        }
    }

    private Dictionary<String, Object> getScopeConfig(String scope) {
        synchronized (scopes) {
            if (scope.equals("*")) {
                scope = "star";
            }
            return scopes.get(scope);
        }
    }

    protected void update(ConfigurationAdmin prev, ConfigurationAdmin configAdmin) {
        try {
            Configuration[] configs = configAdmin.listConfigurations(CONFIGURATION_FILTER);
            if (configs != null) {
                for (Configuration config : configs) {
                    addScopeConfig(config);
                }
            }
        } catch (Exception e) {
            // Ignore, should never happen
        }
    }
}

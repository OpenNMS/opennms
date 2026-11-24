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
package org.apache.karaf.shell.security.impl;

import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.gogo.runtime.CommandProxy;
import org.apache.felix.gogo.runtime.activator.Activator;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSessionListener;
import org.apache.felix.service.command.Converter;
import org.apache.felix.service.command.Function;
import org.apache.felix.service.threadio.ThreadIO;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.shell.util.ShellUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import javax.security.auth.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.karaf.util.jaas.JaasHelper;

@Deprecated
public class SecuredCommandProcessorImpl extends CommandProcessorImpl {

    private final BundleContext bundleContext;
    private final ServiceReference<ThreadIO> threadIOServiceReference;
    private final ServiceTracker<Object, Object> commandTracker;
    private final ServiceTracker<Converter, Converter> converterTracker;
    private final ServiceTracker<CommandSessionListener, CommandSessionListener> listenerTracker;

    public SecuredCommandProcessorImpl(BundleContext bc) {
        this(bc, bc.getServiceReference(ThreadIO.class));
    }

    private SecuredCommandProcessorImpl(BundleContext bc, ServiceReference<ThreadIO> sr) {
        super(bc.getService(sr));
        bundleContext = bc;
        threadIOServiceReference = sr;

        // OPENNMS: Subject.getSubject() always throws on JDK 23+
        Subject sub = JaasHelper.currentSubject();
        if (sub == null)
            throw new SecurityException("No current Subject in the Access Control Context");

        Set<RolePrincipal> rolePrincipals = sub.getPrincipals(RolePrincipal.class);
        if (rolePrincipals.size() == 0)
            throw new SecurityException("Current user " + ShellUtil.getCurrentUserName() + " has no associated roles.");

        // TODO cater for custom roles
        StringBuilder sb = new StringBuilder();
        sb.append("(|");
        for (RolePrincipal rp : rolePrincipals) {
            sb.append('(');
            sb.append("org.apache.karaf.service.guard.roles");
            sb.append('=');
            sb.append(escapeforFilterString(rp.getName()));
            sb.append(')');
        }
        sb.append("(!(org.apache.karaf.service.guard.roles=*))"); // Or no roles specified at all
        sb.append(')');
        String roleClause = sb.toString();

        addConstant(Activator.CONTEXT, bc);
        addCommand("osgi", this, "addCommand");
        addCommand("osgi", this, "removeCommand");
        addCommand("osgi", this, "eval");

        try {
            // The role clause is used to only display commands that the current user can invoke.
            commandTracker = trackCommands(bc, roleClause);
            commandTracker.open();

            converterTracker = trackConverters(bc);
            converterTracker.open();
            listenerTracker = trackListeners(bc);
            listenerTracker.open();
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        commandTracker.close();
        converterTracker.close();
        listenerTracker.close();
        bundleContext.ungetService(threadIOServiceReference);
    }

    private ServiceTracker<Object, Object> trackCommands(final BundleContext context, String roleClause) throws InvalidSyntaxException {
        Filter filter = context.createFilter(String.format("(&(%s=*)(%s=*)%s)",
                CommandProcessor.COMMAND_SCOPE, CommandProcessor.COMMAND_FUNCTION, roleClause));

        return new ServiceTracker<Object, Object>(context, filter, null) {
            @Override
            public Object addingService(ServiceReference<Object> reference) {
                Object scope = reference.getProperty(CommandProcessor.COMMAND_SCOPE);
                Object function = reference.getProperty(CommandProcessor.COMMAND_FUNCTION);
                List<Object> commands = new ArrayList<>();

                if (scope != null && function != null) {
                    if (function.getClass().isArray()) {
                        for (Object f : ((Object[]) function)) {
                            Function target = new CommandProxy(context, reference,
                                    f.toString());
                            addCommand(scope.toString(), target, f.toString());
                            commands.add(target);
                        }
                    } else {
                        Function target = new CommandProxy(context, reference,
                                function.toString());
                        addCommand(scope.toString(), target, function.toString());
                        commands.add(target);
                    }
                    return commands;
                }
                return null;
            }

            @Override
            public void removedService(ServiceReference<Object> reference, Object service) {
                Object scope = reference.getProperty(CommandProcessor.COMMAND_SCOPE);
                Object function = reference.getProperty(CommandProcessor.COMMAND_FUNCTION);

                if (scope != null && function != null) {
                    if (!function.getClass().isArray()) {
                        removeCommand(scope.toString(), function.toString());
                    } else {
                        for (Object func : (Object[]) function) {
                            removeCommand(scope.toString(), func.toString());
                        }
                    }
                }
                super.removedService(reference, service);
            }
        };
    }

    private ServiceTracker<Converter, Converter> trackConverters(BundleContext context) {
        return new ServiceTracker<Converter, Converter>(context, Converter.class.getName(), null) {
            @Override
            public Converter addingService(ServiceReference<Converter> reference) {
                Converter converter = super.addingService(reference);
                addConverter(converter);
                return converter;
            }

            @Override
            public void removedService(ServiceReference<Converter> reference, Converter service) {
                removeConverter(service);
                super.removedService(reference, service);
            }
        };
    }

    private ServiceTracker<CommandSessionListener, CommandSessionListener> trackListeners(BundleContext context) {
        return new ServiceTracker<CommandSessionListener, CommandSessionListener>(context, CommandSessionListener.class, null) {
            @Override
            public CommandSessionListener addingService(ServiceReference<CommandSessionListener> reference) {
                CommandSessionListener listener = context.getService(reference);
                addListener(listener);
                return listener;
            }

            @Override
            public void removedService(ServiceReference<CommandSessionListener> reference, CommandSessionListener service) {
                removeListener(service);
                context.ungetService(reference);
            }
        };
    }

    private String escapeforFilterString(String original) {
        //the filter string follow the LDAP rule
        //where we need escape the special char
        String ret = original;
        ret = ret.replace("\\", "\\\\");
        ret = ret.replace("*", "\\*");
        ret = ret.replace("(", "\\(");
        ret = ret.replace(")", "\\)");
        return ret;
    }

}

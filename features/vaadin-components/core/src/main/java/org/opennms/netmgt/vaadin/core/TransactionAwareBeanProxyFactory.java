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
package org.opennms.netmgt.vaadin.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;


/**
 * This factory class is needed, because when we use DAOs or other classes requiring a transaction scope when not running in the Jetty container, but in the Apache Karaf Container.
 * We either have to ensure that the method call is run within a transaction scope, or we create an new scope.
 * We already have setup this with Spring and do not want to set it up again in the blueprint container.
 *
 * The factory creates a proxy of an already existing object and ensures that each method of a bean is invoked within a transaction scope.
 * This is achieved by wrapping each method call around a {@link TransactionOperations#execute(TransactionCallback)} call.
 */
public class TransactionAwareBeanProxyFactory {

    private static class TransactionInvocationHandlerException extends RuntimeException {

        public TransactionInvocationHandlerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final TransactionOperations transactionOperations;

    public TransactionAwareBeanProxyFactory(TransactionOperations transactionOperations) {
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    /**
     * Creates a proxy for the given object. The object must already been instantiated and must implement at least one interface..
     * The proxy created wraps only methods of all defined interfaces of the provided class.
     * All wrapped methods are executed within a transaction scope.
     * @param createProxyFor The object to create the proxy for. Must not be null.
     * @param <T> The type of the proxy object.
     * @return The proxy of the object to proxy where each (interface) method call is enforced to run within a transaction scope.
     */
    public <T> T createProxy(final T createProxyFor) {
        Objects.requireNonNull(createProxyFor);

        // if no interfaces, there is nothing to do
        if (createProxyFor.getClass().getInterfaces().length == 0) {
            throw new IllegalArgumentException("No interface defined for class " + createProxyFor.getClass());
        }

        // This is the "magic" to wrap any method call of the defined interfaces in a transaction aware call
        final InvocationHandler transactionInvocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxyObject, Method method, Object[] objects) throws Throwable {
                try {
                    Object result = transactionOperations.execute(new TransactionCallback<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus status) {
                            try {
                                return method.invoke(createProxyFor, objects);
                            } catch (InvocationTargetException e) {
                                // we wrap the InvocationTargetException into a RuntimeException as the method signature
                                // of doInTransaction does not allow throwing exceptions.
                                throw new TransactionInvocationHandlerException("Error while invoking transaction aware method", e);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    return result;
                // We catch TransactionInvocationHandlerException in order to deal with exceptions thrown by the "transactionInvocationHandler" above.
                // We grab the target Exception of a possible InvocationTargetException to ensure the right exception is propagated.
                } catch (TransactionInvocationHandlerException ex) {
                    if (ex.getCause() instanceof InvocationTargetException) {
                        throw ((InvocationTargetException)ex.getCause()).getTargetException();
                    }
                    throw ex.getCause(); //otherwise throw the cause, as we do not know what to do
                }
            }
        };

        // This creates the actual Transaction-Aware Proxy
        T transactionAwareProxy = (T) Proxy.newProxyInstance(
                createProxyFor.getClass().getClassLoader(),
                createProxyFor.getClass().getInterfaces(),
                transactionInvocationHandler);
        return transactionAwareProxy;
    }
}

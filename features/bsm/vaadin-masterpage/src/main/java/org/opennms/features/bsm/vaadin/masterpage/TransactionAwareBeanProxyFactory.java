/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.bsm.vaadin.masterpage;


import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/**
 * This factory class is needed, because when we use DAOs or other classes requiring a transaction scope, we either have 
 * to ensure that the method call is run within a transaction scope, or we create an new scope.
 * We already have setup this with Spring and do not want to set it up again in the blueprint container.
 * 
 * The factory creates a proxy of an already existing object and ensures that each method of a bean is invoked within a transaction scope.
 * This is achieved by wrapping each method call around a {@link TransactionOperations#execute(TransactionCallback)} call.
 */
public class TransactionAwareBeanProxyFactory {

    /**
     * Is used to execute the object to proxy's executed method within a transaction scope.
     */
    private final TransactionOperations transactionOperations;
    
    public TransactionAwareBeanProxyFactory(TransactionOperations transactionOperations) {
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    /**
     * Creates a proxy for the given object. The object must already been instantiated.
     * The proxy creation is quite different from the creation of the standard java Proxy pattern.
     * The returned proxy wraps ALL method calls around a transaction scope.
     * @param createProxyFor The object to create the proxy for. Must not be null.
     * @param <T> The type of the proxy object.
     * @return The proxy of the object to proxy where each method call is enforced to run within a transaction scope.
     */
    public <T> T createProxy(final T createProxyFor) {
        Objects.requireNonNull(createProxyFor);
        final ProxyFactory proxyFactory = new ProxyFactory(createProxyFor);
        proxyFactory.addAdvice(new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                Object result = transactionOperations.execute(new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction(TransactionStatus status) {
                        try {
                            return invocation.getMethod().invoke(invocation.getThis(), invocation.getArguments());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return result;
            }
        });
        T transactionAwareProxy = (T) proxyFactory.getProxy();
        return transactionAwareProxy;
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.dao.hibernate;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.opennms.core.spring.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.constraints.NotNull;

public class TracingTransactionInterceptor /*extends TransactionInterceptor */ implements ApplicationContextAware {
    private Tracer tracer = GlobalOpenTelemetry.get().getTracer(TracingTransactionInterceptor.class.getName());

    //
    // @Autowired
    private static ApplicationContext applicationContext;

    @Autowired
    //@NotNull
    private BeanFactoryTransactionAttributeSourceAdvisor advisor;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void doStuff() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : allBeanNames) {
            System.out.println(beanName);
        }
        //BeanFactoryReference foo = BeanUtils.getBeanFactory("shared");
        //foo.
        //BeanFactoryTransactionAttributeSourceAdvisor advisor = BeanUtils.getBean("dao", TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME, BeanFactoryTransactionAttributeSourceAdvisor.class);
        advisor.setAdvice(new Foo((MethodInterceptor) advisor.getAdvice()));
    }

    public class Foo implements MethodInterceptor, Advice {
        private final MethodInterceptor orig;

        public Foo(MethodInterceptor orig) {
            this.orig = orig;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
                Span span = tracer.spanBuilder("invoke transaction").startSpan();
                span.setAttribute("stacktrace", ExceptionUtils.getStackTrace(new Exception()));
                try (Scope scope = span.makeCurrent()) {
                    return this.orig.invoke(invocation);
                } catch (Throwable throwable) {
                    span.setStatus(StatusCode.ERROR, "Received unexpected Throwable");
                    span.recordException(throwable);
                    throw throwable;
                } finally {
                    span.end();
                }
        }
    }

    /*
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Span span = tracer.spanBuilder("invoke transaction").startSpan();
        span.setAttribute("stacktrace", ExceptionUtils.getStackTrace(new Exception()));
        try (Scope scope = span.makeCurrent()) {
            return super.invoke(invocation);
        } catch (Throwable throwable) {
            span.setStatus(StatusCode.ERROR, "Received unexpected Throwable");
            span.recordException(throwable);
            throw throwable;
        } finally {
            span.end();
        }
    }

     */

/*
    @Override
    public void afterTransactionBegin(Transaction tx) {
        Span span = tracer.spanBuilder("afterTransactionBegin").startSpan();
        span.setAttribute("stacktrace", ExceptionUtils.getStackTrace(new Exception()));
        try (Scope scope = span.makeCurrent()) {
            super.afterTransactionBegin(tx);
        } catch (Throwable throwable) {
            span.setStatus(StatusCode.ERROR, "Received unexpected Throwable");
            span.recordException(throwable);
            throw throwable;
        } finally {
            span.end();
        }
    }
    */
}

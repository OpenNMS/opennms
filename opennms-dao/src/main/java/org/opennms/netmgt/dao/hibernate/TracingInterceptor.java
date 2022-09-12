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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;

public class TracingInterceptor extends EmptyInterceptor {
    private Tracer tracer = GlobalOpenTelemetry.get().getTracer(TracingInterceptor.class.getName());


    @Override
    public void afterTransactionBegin(Transaction tx) {
        Span span;
        Span ourSpan = null;
        if (Span.current().getSpanContext().isValid()) {
            span = Span.current();
        } else {
            ourSpan = tracer.spanBuilder("afterTransactionBegin").startSpan();
            ourSpan.setAttribute("stacktrace", ExceptionUtils.getStackTrace(new Exception()));
            span = ourSpan;
        }
        try (Scope scope = span.makeCurrent()) {
            super.afterTransactionBegin(tx);
        } catch (Throwable throwable) {
            span.setStatus(StatusCode.ERROR, "Received unexpected Throwable");
            span.recordException(throwable);
            throw throwable;
        } finally {
            if (ourSpan != null) {
                span.end();
            }
        }
    }
}

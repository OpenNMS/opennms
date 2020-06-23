/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.Striped;

/**
 * Here we extend {@link com.google.common.util.concurrent.Striped}
 * to support creating fair reentrant locks.
 *
 * We use reflection to invoke {@link com.google.common.util.concurrent.Striped.CompactStriped#CompactStriped(int, Supplier)}
 * and provide a supplier that creates a fair lock.
 *
 * This is clearly a hack, but seems better than the alternative of A) trying to find another library that supports this
 * or B) re-implementing striped locking ourselves.
 *
 * Guava Feature Request:
 *   https://github.com/google/guava/issues/2514
 *
 * @author jwhite
 */
public class StripedExt {

    /**
     * Creates a {@code Striped<Lock>} with eagerly initialized, strongly referenced locks.
     * Every lock is fair and reentrant.
     *
     * @param stripes the minimum number of stripes (locks) required
     * @return a new {@code Striped<Lock>}
     */
    public static Striped<Lock> fairLock(int stripes) {
        final Striped<Lock> lockStripes = Striped.lock(1);
        try {
            final Constructor<?> ctor = lockStripes.getClass().getDeclaredConstructor(int.class, Supplier.class);
            ctor.setAccessible(true);
            try {
                return (Striped<Lock>)ctor.newInstance(stripes, (Supplier<Lock>) () -> new FairPaddedLock());
            } catch (InstantiationException|IllegalAccessException|InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Modified version com.google.common.util.concurrent.Striped.PaddedLock that
     * creates a fair ReentrantLock
     */
    private static class FairPaddedLock extends ReentrantLock {
        /*
         * Padding from 40 into 64 bytes, same size as cache line. Might be beneficial to add
         * a fourth long here, to minimize chance of interference between consecutive locks,
         * but I couldn't observe any benefit from that.
         */
        @SuppressWarnings("unused")
        long q1, q2, q3;

        FairPaddedLock() {
            super(true);
        }
    }

}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.eventd;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * <p>UndeclaredEventException class.</p>
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public final class UndeclaredEventException extends UndeclaredThrowableException {

    private static final long serialVersionUID = -3138934063775363857L;

    /**
     * <p>Constructor for UndeclaredEventException.</p>
     *
     * @param t a {@link java.lang.Throwable} object.
     */
    public UndeclaredEventException(Throwable t) {
        super(t);
    }

    /**
     * <p>Constructor for UndeclaredEventException.</p>
     *
     * @param t a {@link java.lang.Throwable} object.
     * @param s a {@link java.lang.String} object.
     */
    public UndeclaredEventException(Throwable t, String s) {
        super(t, s);
    }
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.jexl;

import org.apache.commons.jexl2.JexlInfo;
import org.apache.commons.jexl2.introspection.JexlMethod;
import org.apache.commons.jexl2.introspection.JexlPropertyGet;
import org.apache.commons.jexl2.introspection.JexlPropertySet;
import org.apache.commons.jexl2.introspection.UberspectImpl;
import org.apache.commons.logging.Log;

/**
 * A modified implementation of the jexl2 Uberspect class to use the OnmsJexlSandbox implementation.
 */
public class OnmsJexlUberspect extends UberspectImpl {
    protected final OnmsJexlSandbox sandbox;

    public OnmsJexlUberspect(final Log runtimeLogger, final OnmsJexlSandbox theSandbox) {
        super(runtimeLogger);
        if (theSandbox == null) {
            throw new NullPointerException("sandbox can not be null");
        } else {
            this.sandbox = theSandbox;
        }
    }

    public void setLoader(final ClassLoader cloader) {
        this.base().setLoader(cloader);
    }

    public JexlMethod getConstructorMethod(final Object ctorHandle, final Object[] args, final JexlInfo info) {
        final String className;
        if (ctorHandle instanceof Class) {
            Class<?> clazz = (Class) ctorHandle;
            className = clazz.getName();
        } else {
            if (ctorHandle == null) {
                return null;
            }

            className = ctorHandle.toString();
        }

        return this.sandbox.execute(className, "") != null ? super.getConstructorMethod(className, args, info) : null;
    }

    public JexlMethod getMethod(final Object obj, final String method, final Object[] args, final JexlInfo info) {
        if (obj != null && method != null) {
            final String className;
            if (obj instanceof Class) {
                Class<?> clazz = (Class) obj;
                className = clazz.getName();
            } else {
                className = obj.getClass().getName();
            }
            String actual = this.sandbox.execute(className, method);
            if (actual != null) {
                return this.getMethodExecutor(obj, actual, args);
            }
        }

        return null;
    }

    public JexlPropertyGet getPropertyGet(final Object obj, final Object identifier, final JexlInfo info) {
        if (obj != null && identifier != null) {
            String actual = this.sandbox.read(obj.getClass().getName(), identifier.toString());
            if (actual != null) {
                return super.getPropertyGet(obj, actual, info);
            }
        }

        return null;
    }

    public JexlPropertySet getPropertySet(final Object obj, final Object identifier, final Object arg, final JexlInfo info) {
        if (obj != null && identifier != null) {
            String actual = this.sandbox.write(obj.getClass().getName(), identifier.toString());
            if (actual != null) {
                return super.getPropertySet(obj, actual, arg, info);
            }
        }

        return null;
    }
}

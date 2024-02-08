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

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
package org.opennms.web.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Minimal drop-in replacement for Spring's {@code org.springframework.web.servlet.mvc.multiaction.MultiActionController},
 * which was removed in Spring 5.0.
 *
 * <p>It reproduces only the slice of behaviour OpenNMS relies on:</p>
 * <ul>
 *   <li>the request is dispatched to a handler method whose name is the last path segment of the
 *       request URI with any file extension removed (the behaviour of Spring's default
 *       {@code InternalPathMethodNameResolver}); e.g. {@code /alarm/saveStickyMemo.htm} &rarr; {@code saveStickyMemo};</li>
 *   <li>handler methods have the signature {@code ModelAndView name(HttpServletRequest, HttpServletResponse)}
 *       or {@code ModelAndView name(HttpServletRequest, HttpServletResponse, Command)}, where {@code Command}
 *       is a bean whose properties are bound from the request parameters.</li>
 * </ul>
 *
 * <p>As with the original, all HTTP methods are accepted, and subclasses may override
 * {@link #handleRequest} or {@link #handleRequestInternal} (e.g. to wrap dispatch in a transaction).</p>
 */
public abstract class OnmsMultiActionController extends AbstractController {

    /**
     * Framework lifecycle methods that must never be treated as dispatch targets. Both are public
     * {@code ModelAndView (HttpServletRequest, HttpServletResponse)} methods (inherited from
     * {@link AbstractController}, or overridden by subclasses), so without this guard a request such as
     * {@code /event/handleRequest.htm} would resolve to one of them and re-enter this dispatcher, recursing
     * until a {@link StackOverflowError} (a trivially reachable DoS). They are not valid actions.
     */
    private static final Set<String> RESERVED_METHOD_NAMES = Set.of("handleRequest", "handleRequestInternal");

    public OnmsMultiActionController() {
        // Accept all HTTP methods, matching MultiActionController (do not restrict to GET/HEAD/POST).
        super(false);
    }

    /** {@inheritDoc} Dispatches to the handler method named by the request path. */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String methodName = resolveMethodName(request);
        final Method method = findHandlerMethod(methodName);
        if (method == null) {
            throw new ServletException("No handler method named '" + methodName + "' found on " + getClass().getName());
        }
        try {
            final Object[] args;
            if (method.getParameterCount() == 3) {
                args = new Object[] { request, response, bindCommand(request, method.getParameterTypes()[2], methodName) };
            } else {
                args = new Object[] { request, response };
            }
            return (ModelAndView) method.invoke(this, args);
        } catch (final InvocationTargetException ite) {
            final Throwable cause = ite.getTargetException();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new ServletException(cause);
        }
    }

    /** Method name = last path segment of the request URI, with any extension (and matrix params) stripped. */
    private static String resolveMethodName(final HttpServletRequest request) {
        String path = request.getRequestURI();
        final int slash = path.lastIndexOf('/');
        if (slash != -1) {
            path = path.substring(slash + 1);
        }
        final int semi = path.indexOf(';');
        if (semi != -1) {
            path = path.substring(0, semi);
        }
        final int dot = path.lastIndexOf('.');
        if (dot != -1) {
            path = path.substring(0, dot);
        }
        return path;
    }

    /** Finds a public handler method by name returning a ModelAndView and taking (request, response[, command]). */
    private Method findHandlerMethod(final String methodName) {
        if (RESERVED_METHOD_NAMES.contains(methodName)) {
            // Never dispatch to the framework lifecycle methods; doing so re-enters this dispatcher (recursion).
            return null;
        }
        for (final Method method : getClass().getMethods()) {
            if (!method.getName().equals(methodName) || !ModelAndView.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            final Class<?>[] params = method.getParameterTypes();
            if (params.length >= 2
                    && HttpServletRequest.class.isAssignableFrom(params[0])
                    && HttpServletResponse.class.isAssignableFrom(params[1])
                    && (params.length == 2 || params.length == 3)) {
                return method;
            }
        }
        return null;
    }

    /** Instantiates the command object and binds request parameters onto it. */
    private static Object bindCommand(final HttpServletRequest request, final Class<?> commandClass, final String objectName) throws Exception {
        final Constructor<?> ctor = commandClass.getDeclaredConstructor();
        ReflectionUtils.makeAccessible(ctor);
        final Object command = ctor.newInstance();
        final ServletRequestDataBinder binder = new ServletRequestDataBinder(command, objectName);
        binder.bind(request);
        return command;
    }
}

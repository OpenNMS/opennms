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
package org.opennms.container.web.felix.base.internal.service;

import org.osgi.service.http.HttpContext;
import org.osgi.framework.Bundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

public final class DefaultHttpContext 
    implements HttpContext
{
    private Bundle bundle;

    public DefaultHttpContext(Bundle bundle)
    {
        this.bundle = bundle;
    }

    @Override
    public String getMimeType(String name)
    {
        return null;
    }

    @Override
    public URL getResource(String name)
    {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        return this.bundle.getResource(name);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res)
    {
        return true;
    }
}

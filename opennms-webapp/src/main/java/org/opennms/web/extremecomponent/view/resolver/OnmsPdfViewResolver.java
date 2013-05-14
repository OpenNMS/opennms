/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.extremecomponent.view.resolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.extremecomponents.table.core.Preferences;
import org.extremecomponents.table.filter.ViewResolver;

public class OnmsPdfViewResolver implements ViewResolver {

    @Override
    public void resolveView(ServletRequest request, ServletResponse response, Preferences preferences, Object viewData) throws Exception {
        InputStream is = new ByteArrayInputStream(((String) viewData).getBytes("UTF-8"));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        FopFactory fopFactory = FopFactory.newInstance();
        fopFactory.setStrictValidation(false);
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
        
        TransformerFactory tfact = TransformerFactory.newInstance();
        Transformer transformer = tfact.newTransformer();
        Source src = new StreamSource(is);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        
        byte[] contents = out.toByteArray();
        response.setContentLength(contents.length);
        response.getOutputStream().write(contents);

    }

}

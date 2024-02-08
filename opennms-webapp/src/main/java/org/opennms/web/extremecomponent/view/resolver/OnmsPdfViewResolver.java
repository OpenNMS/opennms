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
package org.opennms.web.extremecomponent.view.resolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.extremecomponents.table.core.Preferences;
import org.extremecomponents.table.filter.ViewResolver;
import org.springframework.core.io.ClassPathResource;

public class OnmsPdfViewResolver implements ViewResolver {

    @Override
    public void resolveView(ServletRequest request, ServletResponse response, Preferences preferences, Object viewData) throws Exception {
        InputStream is = new ByteArrayInputStream(((String) viewData).getBytes(StandardCharsets.UTF_8));
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        final FopFactory fopFactory = new FopFactoryBuilder(new ClassPathResource("/fop.xconf").getURI()).build();
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

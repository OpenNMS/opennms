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

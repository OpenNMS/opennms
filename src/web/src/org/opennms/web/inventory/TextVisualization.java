/*
 * Created on 13-ott-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.web.inventory;
import java.io.*;
import java.util.*;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.opennms.netmgt.inventory.UnparsableConfigurationException;


/**
 * @author maurizio
 */
public class TextVisualization implements Visualization {

    public String getVisualization(String filePath, Map parameters) throws IOException, UnparsableConfigurationException{

		String fileXslt = (String) parameters.get("xslt-file");
		if(fileXslt==null){
			throw new IOException("Parameter xslt-file not found.");
		}
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		Writer w = new StringWriter();
		try {
			transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(fileXslt));
			Reader r = new FileReader(filePath);
			transformer.transform(new StreamSource(r),new javax.xml.transform.stream.StreamResult(w));
		}catch (TransformerException t) {
			throw new IOException(t.toString());
		}
		
		return w.toString();
	}
	
	
}

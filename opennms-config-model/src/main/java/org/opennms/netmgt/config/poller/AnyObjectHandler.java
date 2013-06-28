package org.opennms.netmgt.config.poller;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class AnyObjectHandler implements DomHandler<XmlContent, StreamResult> {
	private StringWriter xmlWriter = new StringWriter(); 

	public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
		return new StreamResult(xmlWriter);
	}

	public XmlContent getElement(StreamResult rt) {
		String xml = rt.getWriter().toString();
		return new XmlContent(xml.replaceFirst("\\<\\?xml[^>]+\\>", ""));
	}

	public Source marshal(XmlContent n, ValidationEventHandler errorHandler) {
		try {
			StringReader xmlReader = new StringReader(n.getContent().trim());
			return new StreamSource(xmlReader);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}

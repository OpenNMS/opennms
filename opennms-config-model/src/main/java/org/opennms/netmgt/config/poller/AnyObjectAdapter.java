package org.opennms.netmgt.config.poller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnyObjectAdapter extends XmlAdapter<Element, Object> {

	@Override
	public Element marshal(Object v) throws Exception {
		if (v == null)
			return null;
		byte[] utf8Bytes = ((String) v).getBytes("UTF-8");
		InputStream sbis = new ByteArrayInputStream(utf8Bytes);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sbis);
		return doc.getDocumentElement();
	}

	@Override
	public Object unmarshal(Element v) throws Exception {
		StringBuilder builder = new StringBuilder();
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(v), new StreamResult(writer));
		builder.append(writer.toString());
		return builder.toString();
	}

}
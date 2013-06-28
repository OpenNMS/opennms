package org.opennms.netmgt.config.poller;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class AnyObjectAdapter extends XmlAdapter<Element, XmlContent> {

	@Override
	public Element marshal(XmlContent v) throws Exception {
		if (v == null)
			return null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(v.getContent()));
		Document doc = builder.parse(is);
		return doc.getDocumentElement();
	}

	@Override
	public XmlContent unmarshal(Element v) throws Exception {
		StringBuilder builder = new StringBuilder();
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(v), new StreamResult(writer));
		builder.append(writer.toString());
		return new XmlContent(builder.toString());
	}

}
package org.opennms.core.test.xml;

import org.junit.Ignore;
import org.junit.Test;

public abstract class XmlTestNoCastor<T> extends XmlTest<T> {
	public XmlTestNoCastor(final T sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Test
	@Override
	@Ignore
	public void marshalCastorAndCompareToXml() throws Exception {
	}

	@Test
	@Override
	@Ignore
	public void unmarshalXmlAndCompareToCastor() throws Exception {
	}

	@Test
	@Override
	@Ignore
	public void marshalCastorUnmarshalJaxb() throws Exception {
	}

	@Test
	@Override
	@Ignore
	public void marshalJaxbUnmarshalCastor() throws Exception {
	}
	
	@Test
	@Override
	@Ignore
	public void validateCastorObjectAgainstSchema() throws Exception {
	}

}

package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ScriptTest extends XmlTestNoCastor<Script> {

	public ScriptTest(final Script sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Script script0 = new Script();
		script0.setLanguage("erlang");
		Script script1 = new Script();
		script1.setLanguage("erlang");
		script1.setContent("This is a test");
		return Arrays.asList(new Object[][] {
				{script0,
				"<script language=\"erlang\"/>",
				"target/classes/xsds/eventconf.xsd" }, 
				{script1,
					"<script language=\"erlang\">This is a test</script>",
					"target/classes/xsds/eventconf.xsd" }, 
		});
	}

}

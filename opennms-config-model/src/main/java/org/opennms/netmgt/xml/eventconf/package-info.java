@XmlSchema(namespace = "http://xmlns.opennms.org/xsd/eventconf", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@XmlJavaTypeAdapter(value=StringTrimAdapter.class,type=String.class)
package org.opennms.netmgt.xml.eventconf;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opennms.core.xml.StringTrimAdapter;

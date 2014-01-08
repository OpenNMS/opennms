@XmlSchema(
    namespace="http://xmlns.opennms.org/xsd/mail-transport-test",
    elementFormDefault=javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
    xmlns={
        @XmlNs(prefix="", namespaceURI="http://xmlns.opennms.org/xsd/mail-transport-test")
    }
)
@XmlPrefix("mt")
package org.opennms.netmgt.config.mailtransporttest;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;

import org.opennms.core.xml.XmlPrefix;


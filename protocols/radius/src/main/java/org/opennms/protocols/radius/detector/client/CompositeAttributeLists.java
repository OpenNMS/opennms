package org.opennms.protocols.radius.detector.client;

import net.jradius.client.auth.EAPTTLSAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;

public class CompositeAttributeLists {

	private AttributeList m_outerAttributes;
	private AttributeList m_innerAttributes;
	private String m_innerAuthType;
	private Boolean m_trustAll = true;

	public CompositeAttributeLists(AttributeList attributes) {
		m_outerAttributes = attributes;
	}

	public void addToInner(RadiusAttribute attribute) {
		if (hasNoInnerAttributes()){
			setInnerAttributes(new AttributeList());
		}
		m_innerAttributes.add(attribute);
	}

	private void setInnerAttributes(AttributeList attributeList) {
		m_innerAttributes = attributeList;
		
	}

	public boolean hasNoInnerAttributes() {
		return (m_innerAttributes == null);
	}

	public void setTunneledAuthType(String ttlsInnerAuthType) {
		m_innerAuthType = ttlsInnerAuthType;
	}

	public AccessRequest createRadiusRequest(RadiusAuthenticator radiusAuthenticator) {
		AccessRequest request = new AccessRequest();
    	request.addAttributes(m_outerAttributes);
    	if (radiusAuthenticator instanceof EAPTTLSAuthenticator){
    		((EAPTTLSAuthenticator) radiusAuthenticator).setTunneledAttributes(m_innerAttributes);
    		((EAPTTLSAuthenticator) radiusAuthenticator).setInnerProtocol(m_innerAuthType);
    		((EAPTTLSAuthenticator) radiusAuthenticator).setTrustAll(m_trustAll);
    	}
		
		return request;
	}

}

package org.opennms.sms.monitor.internal.config;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

@XmlRootElement(name="matches")
public class TextResponseMatcher extends SequenceResponseMatcher {

	public TextResponseMatcher() {
	}

	public TextResponseMatcher(String text) {
		this();
		setText(text);
	}

	@Override
    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        LogUtils.tracef(this, "textMatches(%s, %s, %s)", session.substitute(getText()), request, response);
        String responseText = response.getText() == null ? "" : response.getText();
        return session.matches(getText(), responseText);
    }

    @Override
    public String toString() {
        return "textMatches(\"" + getText() + "\")";
    }

}

/**
 * 
 */
package org.opennms.netmgt.poller.monitors;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.mailtransporttest.ReadmailTest;
import org.opennms.netmgt.config.mailtransporttest.SendmailTest;
import org.opennms.netmgt.config.mailtransporttest.MailTransportTest;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * This is a wrapper class for handling JavaMail configurations.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class MailTransportParameters {
    
    public static final String KEY = MailTransportParameters.class.getName();
	private static final int DEFAULT_RETRY = 1;
	private static final int DEFAULT_TIMEOUT = 3000;
    private Map<String, String> m_parameterMap;
    private MailTransportTest m_transportTest;
	private String m_testSubjectSuffix;
    private boolean m_end2EndTestInProgress = false;
    private Properties m_javamailProperties = new Properties();

    MailTransportParameters(Map<String, String> parameterMap) {
        m_parameterMap = parameterMap;
        String test = getStringParm("mail-transport-test", null);
        if (test == null) {
            throw new IllegalArgumentException("mail-transport-test must be set in monitor parameters");
        }
        m_transportTest = parseMailTransportTest(test);
    }
    
    @SuppressWarnings("unchecked")
    static synchronized MailTransportParameters get(Map paramterMap) {
        MailTransportParameters parms = (MailTransportParameters)paramterMap.get(KEY);
        if (parms == null) {
            parms = new MailTransportParameters(paramterMap);
            paramterMap.put(KEY, parms);
        }
        return parms;
    }
            
    Map<String, String> getParameterMap() {
        return m_parameterMap;
    }

    MailTransportTest getTransportTest() {
        return m_transportTest;
    }

    MailTransportTest parseMailTransportTest(String test) {
        try {
            return (MailTransportTest) Unmarshaller.unmarshal(MailTransportTest.class, new StringReader(test));
        } catch (MarshalException e) {
            throw new IllegalArgumentException("Unable to parse mail-test-sequence for MailTransportMonitor: "+test, e);
        } catch (ValidationException e) {
            throw new IllegalArgumentException("Unable to parse page-sequence for HttpMonitor: "+test, e);
        }
    
    }

    private String getStringParm(String key, String deflt) {
        return ParameterMap.getKeyedString(this.getParameterMap(), key, deflt);
    }
    
    private int getIntParm(String key, int defValue) {
        return ParameterMap.getKeyedInteger(getParameterMap()  , key, defValue);
    }

	public int getRetries() {
		return getIntParm("retry", MailTransportParameters.DEFAULT_RETRY);
	}

	public int getTimeout() {
		return getIntParm("timeout", MailTransportParameters.DEFAULT_TIMEOUT);
	}
	
    public String getReadTestPassword() {
        return getReadTest().getUserAuth().getPassword();
    }

    ReadmailTest getReadTest() {
        return getTransportTest().getMailTest().getReadmailTest();
    }

	public String getTestSubjectSuffix() {
		return m_testSubjectSuffix;
	}

	public void setTestSubjectSuffix(String suffix) {
		m_testSubjectSuffix = suffix;
	}

	public String getComputedTestSubject() {
	    try {
	        if (getSendTestSubject() != null) {
	            return new StringBuilder(getSendTestSubject()).append(':').append(m_testSubjectSuffix == null ? null : m_testSubjectSuffix).toString();
	        } else {
	            return null;
	        }
	    } catch (IllegalStateException e) {
	        return null;
	    }
	}
	
    String getSendTestFrom() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getFrom();
    }

    public boolean isSendTestUseAuth() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().isUseAuthentication();
    }

    public String getSendTestCharSet() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getCharSet();        }

    public String getSendTestMessageContentType() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getMessageContentType();
    }

    public boolean isSendTestDebug() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().isDebug();
    }

    public String getSendTestMessageEncoding() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getMessageEncoding();
    }

    public String getSendTestMailer() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getMailer();
    }

    public String getSendTestHost() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailHost().getHost();        }

    public String getSendTestMessageBody() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getBody();
    }

    public String getSendTestPassword() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getUserAuth().getPassword();
    }

    public boolean isSendTestIsQuitWait() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().isQuitWait();
    }

    public int getSendTestPort() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return (int)getSendTest().getSendmailHost().getPort();
    }

    public boolean isSendTestIsSslEnable() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().isSslEnable();
    }

    public boolean isSendTestStartTls() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().isStartTls();
    }

    public String getSendTestSubject() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getSubject();
    }

    public String getSendTestRecipeint() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getTo();
    }

    public String getSendTestTransport() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getTransport();
    }

    public boolean isSendTestUseJmta() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().isUseJmta();
    }

    public String getSendTestUserName() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getUserAuth().getUserName();
    }

    SendmailTest getSendTest() {
        return getTransportTest().getMailTest().getSendmailTest();
    }

    public String getReadTestHost() {
        return getReadTest().getReadmailHost().getHost();
    }

    public int getReadTestPort() {
        return (int)getReadTest().getReadmailHost().getPort();
    }

    public String getReadTestUserName() {
        return getReadTest().getUserAuth().getUserName();
    }

    public String getReadTestFolder() {
        return getReadTest().getMailFolder();
    }

    public String getReadTestProtocol() {
        return getReadTest().getReadmailHost().getReadmailProtocol().getTransport();
    }

    public boolean isReadTestStartTlsEnabled() {
        return getReadTest().getReadmailHost().getReadmailProtocol().isStartTls();
    }
    
    public boolean isReadTestSslEnabled() {
        return getReadTest().getReadmailHost().getReadmailProtocol().isSslEnable();
    }

    public void setEnd2EndTestInProgress(boolean b) {
        m_end2EndTestInProgress  = b;
    }
    
    public boolean isEnd2EndTestInProgress() {
        return m_end2EndTestInProgress;
    }
    
    public long getReadTestAttemptInterval() {
        return getReadTest().getAttemptInterval();
    }
    
    public long getSendTestAttemptInterval() {
        return getSendTest().getAttemptInterval();
    }

    public Properties getJavamailProperties() {
        return m_javamailProperties;
    }

    public void setJavamailProperties(Properties props) {
        m_javamailProperties = props;
    }

    public void setReadTestHost(String host) {
        getReadTest().getReadmailHost().setHost(host);
    }

    public void setSendTestHost(String host) {
        getSendTest().getSendmailHost().setHost(host);
    }
}
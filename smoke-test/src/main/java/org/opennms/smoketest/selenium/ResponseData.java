package org.opennms.smoketest.selenium;

public class ResponseData {
    private final int m_status;
    private final String m_responseText;

    public ResponseData(final int status, final String responseText) {
        m_status = status;
        m_responseText = responseText;
    }

    public int getStatus() {
        return m_status;
    }
    public String getResponseText() {
        return m_responseText;
    }

    @Override
    public String toString() {
        return "ResponseData [status=" + m_status + ", responseText=" + m_responseText + "]";
    }
}

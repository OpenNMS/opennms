package org.opennms.netmgt.jasper.measurement;

import java.io.InputStream;

/**
 * Created by mvrueden on 26/08/15.
 */
class Result {
    private InputStream errorStream;
    private InputStream inputStream;
    private String responseMessage;
    private int responseCode;

    public void setErrorStream(InputStream errorStream) {
        this.errorStream = errorStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean wasSuccessful() {
        return responseCode >= 200 && responseCode <= 299;
    }

    public boolean wasRedirection() {
        return responseCode >= 300 && responseCode <= 399;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public InputStream getErrorStream() {
        return errorStream;
    }
}

package org.opennms.netmgt.provision.conversation;

import java.io.IOException;

import org.opennms.netmgt.provision.exchange.ResponseHandler;

public class SimpleConversationEndPoint {

    protected Conversation m_conversation;
    private int m_timeout;
    
    public void init() throws IOException {
        m_conversation = new Conversation();
    };
    
    public void onInit() {};
    
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getTimeout() {
        return m_timeout;
    }
    
    /**
     * 
     * @param prefix
     * @return ResponseHandler
     */
    protected ResponseHandler startsWith(final String prefix) {
        return new ResponseHandler() {

            public boolean matches(String input) {
                return input.startsWith(prefix);
            }
            
        };
    }
    
    /**
     * 
     * @param phrase
     * @return ResponseHandler
     */
    protected ResponseHandler contains(final String phrase) {
        return new ResponseHandler() {

            public boolean matches(String input) {
                return input.contains(phrase);
            }
            
        };
    }
    
    /**
     * 
     * @param regex
     * @return ResponseHandler
     */
    protected ResponseHandler regexMatches(final String regex) {
        return new ResponseHandler() {
            
            public boolean matches(String input) {
                return input.matches(regex);
            }
            
        };
    }
}

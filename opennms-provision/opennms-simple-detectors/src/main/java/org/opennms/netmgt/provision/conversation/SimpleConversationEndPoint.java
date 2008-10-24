package org.opennms.netmgt.provision.conversation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import org.opennms.netmgt.provision.exchange.Exchange;
import org.opennms.netmgt.provision.exchange.RequestHandler;
import org.opennms.netmgt.provision.exchange.ResponseHandler;

public class SimpleConversationEndPoint {
    
    public static class SimpleExchange implements Exchange{
        private ResponseHandler m_responseHandler;
        private RequestHandler m_requestHandler;
        
        public SimpleExchange(ResponseHandler responseHandler, RequestHandler requestHandler) {
            m_responseHandler = responseHandler;
            m_requestHandler = requestHandler;
        }

        public boolean matchResponseByString(String response) {
            return m_responseHandler.matches(response);
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            String input = in.readLine();
            if(input != null) { return false;}
            return matchResponseByString(input);
        }

        public boolean sendRequest(OutputStream out) throws IOException {
            if(m_requestHandler != null) {
                m_requestHandler.doRequest(out);
            }
            return true;
        }
        
    }
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
    
    /**
     * Add a ResponseHandler by calling one of the three utility methods:
     * 
     * startsWith(String prefix);
     * contains(String phrase);
     * regexMatches(String regex);
     * 
     * Within the extending class's overriding onInit method
     */
    protected void addResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        m_conversation.addExchange(new SimpleExchange(responseHandler, requestHandler));
    }
    
    protected RequestHandler singleLineReply(final String reply) {
        return new RequestHandler() {

            public void doRequest(OutputStream out) throws IOException {
                out.write(String.format("%s\r\n", reply).getBytes());
            }
            
        };
    }
    
    protected RequestHandler singleLineRequest(final String request) {
      return new RequestHandler() {

          public void doRequest(OutputStream out) throws IOException {
              out.write(String.format("%s\r\n", request).getBytes());
          }
          
      };
  }
}

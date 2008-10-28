package org.opennms.netmgt.provision.detector;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.exchange.Exchange;
import org.opennms.netmgt.provision.exchange.RequestHandler;
import org.opennms.netmgt.provision.exchange.ResponseHandler;
import org.springframework.util.StringUtils;

public class FtpDetector extends SimpleDetector implements ServiceDetector {
    
    public static class FtpResponse extends SimpleExchange implements Exchange{
        
        public FtpResponse(ResponseHandler responseHandler, RequestHandler requestHandler) {
            super(responseHandler, requestHandler);
            
        }

        public boolean processResponse(BufferedReader in) throws IOException {
            int code;
            List<String> response = new ArrayList<String>();

            String firstResponseLine = in.readLine();
            System.out.println("FtpResponse from server: " + firstResponseLine);
            if (firstResponseLine == null) {
                throw new IOException("End of stream was reached before a response could be read");
                
            }
            
            // XXX this could use better error checking!
            String codeString = firstResponseLine.substring(0, 3);
            response.add(firstResponseLine.substring(4));

            try {
                code = Integer.parseInt(codeString);
            } catch (NumberFormatException e) {
                IOException newE = new IOException("First response line returned a non-numeric result code \"" + codeString + "\": " + firstResponseLine);
                newE.initCause(e);
                throw newE;
            }

            // Is the fourth character a hyphen (if so, it's a continuation)?
            if ("-".equals(firstResponseLine.substring(3, 4))) {
                // The multi-line response ends with a line that begins with this:
                String endMultiLine = code + " ";

                while (true) {
                    String subsequentResponse = in.readLine();
                    System.out.println("FtpResponse subsequentResponse: " + subsequentResponse);
                    if (subsequentResponse == null) {
                        throw new IOException("End of stream was reached before the complete multi-line response could be read.  What was read: " + StringUtils.collectionToDelimitedString(response, "\n"));
                    }
                    
                    if (subsequentResponse.startsWith(endMultiLine)) {
                        response.add(subsequentResponse.substring(4));
                        break;
                    }
                    
                    response.add(subsequentResponse);
                }
            }
            
            return m_responseHandler.matches(codeString);
        }

        public boolean matchResponseByString(String input) {
            return false;
        }
        
    }
    
    protected FtpDetector() {
        super(21, 500, 3);
    }
    
    public void onInit() {
        expectBanner(expectedCodeRange(100, 600), singleLineRequest("quit"));
        addFtpResponseHandler(expectedCodeRange(100,600), null);
    }
    
     /**
      * Overriding expectBanner for Ftp, the FtpResponse is a multiline exchange.
      * 
      *  @param ResponseHandler
      *  @param ResquestHandler
      */
    protected void expectBanner(ResponseHandler responseHandler, RequestHandler requestHandler) {
        getConversation().addExchange(new FtpResponse(responseHandler, requestHandler));
    }
    
    protected void addFtpResponseHandler(ResponseHandler responseHandler, RequestHandler requestHandler) {
        getConversation().addExchange(new FtpResponse(responseHandler, requestHandler));
    }
    
    protected ResponseHandler expectedCodeRange(final int begin, final int end) {
        return new ResponseHandler() {

            public boolean matches(String input) {
                int code = Integer.parseInt(input);
                return code >= begin && code < end;
            }
            
        };
    }

}

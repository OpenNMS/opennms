package org.opennms.netmgt.provision.detector.simple.response;

import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;



public class HttpStatusResponse extends LineOrientedResponse {
    
    
    /**
     * @param response
     */
    public HttpStatusResponse(String response) {
        super(response);
        
    }

    public boolean validateResponse(String pattern, String url, boolean isCheckCode, int maxRetCode) throws Exception {
        String codeStr = Integer.toString(maxRetCode);
        String[] codeArray = codeStr.split("");
        if(codeArray.length < 3) {
            throw new IllegalArgumentException("Maximum HTTP return code is too short, must be at least 3 digits");
        }
        String REGEX = String.format("([H][T][T][P+]/[1].[0-1]) ([0-%s][0-2][0-%s]) ([a-zA-Z ]+)", codeArray[1], codeArray[3]);
        
        if(!isCheckCode) {
            REGEX = "([H][T][T][P+]/[1].[0-1]) ([0-6]+) ([a-zA-Z ]+)";
        }
        
        LogUtils.infof(this, "HTTP status regex: %s\n", REGEX);
        return Pattern.matches(REGEX, getResponse().trim());
    }

}

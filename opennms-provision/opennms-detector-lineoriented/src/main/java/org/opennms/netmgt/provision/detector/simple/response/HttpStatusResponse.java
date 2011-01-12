package org.opennms.netmgt.provision.detector.simple.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;



/**
 * <p>HttpStatusResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class HttpStatusResponse extends LineOrientedResponse {
    
    
    private static final Pattern DEFAULT_REGEX = Pattern.compile("([H][T][T][P+]/[1].[0-1]) ([0-6]+) ([a-zA-Z ]+)");

    /**
     * <p>Constructor for HttpStatusResponse.</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public HttpStatusResponse(final String response) {
        super(response);
        
    }

    /**
     * <p>validateResponse</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     * @param isCheckCode a boolean.
     * @param maxRetCode a int.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    public boolean validateResponse(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode) throws Exception {
        String[] codeArray = Integer.toString(maxRetCode).split("");
        if(codeArray.length < 3) {
            throw new IllegalArgumentException("Maximum HTTP return code is too short, must be at least 3 digits");
        }
        
        final Pattern p;
        
        if (isCheckCode) {
            p = Pattern.compile(String.format("([H][T][T][P+]/[1].[0-1]) ([0-%s][0-2][0-%s]) ([a-zA-Z ]+)", codeArray[1], codeArray[3]));
        } else {
            p = DEFAULT_REGEX;
        }

        final Matcher m = p.matcher(getResponse().trim());
        LogUtils.infof(this, "HTTP status regex: %s\n", p.pattern());
        return m.matches();
    }

}

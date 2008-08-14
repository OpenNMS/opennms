package org.opennms.web;

// see: http://mc4j.org/confluence/display/stripes/XSS+filter

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.text.Normalizer;


public class SafeHtmlUtil
{
    public static String sanitize(String raw)
    {
        if (raw==null || raw.length()==0)
            return raw;

        return HTMLEntityEncode(canonicalize(raw));
    }

    private static Pattern scriptPattern = Pattern.compile("script", Pattern.CASE_INSENSITIVE);
    private static Matcher scriptMatcher = scriptPattern.matcher("&#x73;cript");

    public static String HTMLEntityEncode(String input)
    {
        String next = scriptMatcher.replaceAll(input);

        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < next.length(); ++i )
        {
            char ch = next.charAt( i );

            if (ch=='<')
                sb.append("&lt;");
            else if (ch=='>')
                sb.append("&gt;");
            else
                sb.append(ch);
        }

        return sb.toString();
    }


    // "Simplifies input to its simplest form to make encoding tricks more difficult"
    // though it didn't do seem to do anything to hex or HTML encoded characters... *shrug* maybe for unicode?
    public static String canonicalize( String input )
    {
        return Normalizer.normalize( input, Normalizer.DECOMP, 0 );
    }
}
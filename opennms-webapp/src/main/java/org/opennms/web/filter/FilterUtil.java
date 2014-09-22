package org.opennms.web.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FilterUtil {

    public static String toFilterURL(String[] filters) {
        return toFilterURL(filters != null ? Arrays.asList(filters) : new ArrayList<String>());
    }

    public static String toFilterURL(List<String> filters) {
        StringBuffer buffer = new StringBuffer();
        if( filters != null ) {
            for( int i=0; i < filters.size(); i++ ) {
                if (i == 0) buffer.append("filter=");
                else buffer.append( "&amp;filter=" );
                String filterString = filters.get(i);
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }
        return( buffer.toString() );
    }
}

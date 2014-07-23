package org.opennms.netmgt.jmx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO mvr document and add license text
public class JmxUtils {

    // converts, so that we only have Strings, all non String value parameters will be removed
    public static Map<String, String> convertToStringMap(Map<String, Object> map) {
        Map<String, String> convertedProperties = new HashMap<>();
        for (Map.Entry<String, Object> eachEntry : map.entrySet()) {
            if (eachEntry.getValue() instanceof String) {
                convertedProperties.put(eachEntry.getKey(), (String)eachEntry.getValue());
            }
        }
        return Collections.unmodifiableMap(convertedProperties);
    }

}

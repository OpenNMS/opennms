package org.opennms.netmgt.jmx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO mvr document and add license text
public class JmxUtils {

    /**
     * Converts, so that we only have Strings, all non String value parameters will be removed.
     * <p/>
     * The returned map is not modifiable.
     * <p/>
     * If the input map is null, null is also returned.
     *
     * @param map The map to be converted. May be null.
     * @return An unmodifiable map containing only String values from the input map, or null if input map was null.
     */
    public static Map<String, String> convertToStringMap(Map<String, Object> map) {
        if (map != null) {
            Map<String, String> convertedProperties = new HashMap<>();
            for (Map.Entry<String, Object> eachEntry : map.entrySet()) {
                if (eachEntry.getValue() instanceof String) {
                    convertedProperties.put(eachEntry.getKey(), (String) eachEntry.getValue());
                }
            }
            return Collections.unmodifiableMap(convertedProperties);
        }
        return null;
    }

}

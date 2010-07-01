
/**
 * <p>StringUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
public class StringUtils {
    /**
     * <p>join</p>
     *
     * @param s a {@link java.util.Collection} object.
     * @param delimiter a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(final Collection<?> s, final String delimiter) {
        if (s == null) {
            return "null";
        }
        final StringBuilder builder = new StringBuilder();
        final Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /**
     * <p>join</p>
     *
     * @param s a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(final List<String> s) {
        return join(s, ", ");
    }
}

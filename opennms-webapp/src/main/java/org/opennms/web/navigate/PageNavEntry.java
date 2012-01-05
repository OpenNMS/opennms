package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

public interface PageNavEntry {
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getName();

    /**
     * <p>getUrl</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getUrl();

    /**
     * <p>evaluate</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.navigate.DisplayStatus} object.
     */
    public abstract DisplayStatus evaluate(HttpServletRequest request);
}

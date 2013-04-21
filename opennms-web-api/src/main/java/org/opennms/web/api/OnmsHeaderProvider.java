package org.opennms.web.api;

import javax.servlet.http.HttpServletRequest;

public interface OnmsHeaderProvider {

    String getHeaderHtml(HttpServletRequest request);
}

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;

import javax.net.ssl.X509ExtendedTrustManager;

/**
 * This class is used to change the behavior of the X509ExtendedTrustManager
 * that is used to validate certificates, and algorithms for SSL/TLS sockets.
 * With this class all certificates will be approved.
 *
 * @author <a href="mailto:roskens@opennms.org">Ronald Roskens</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class RelaxedX509ExtendedTrustManager extends X509ExtendedTrustManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    /**
     * <p>
     * getAcceptedIssuers</p>
     *
     * @return an array of {@link java.security.cert.X509Certificate} objects.
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine ssle) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine ssle) throws CertificateException {
    }
}

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.provision.support.trustmanager;

import java.security.cert.CertificateException;

import javax.net.ssl.X509TrustManager;

/**
 * This class is used to change the behaviour of the X509TrustManager that is
 * used to validate certificates from an HTTPS server. With this class all
 * certificates will be approved
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 */
public class RelaxedX509TrustManager implements X509TrustManager {
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
    }

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}

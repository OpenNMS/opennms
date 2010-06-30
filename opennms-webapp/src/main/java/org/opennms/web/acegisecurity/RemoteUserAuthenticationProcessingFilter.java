//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/

package org.opennms.web.acegisecurity;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.AbstractProcessingFilter;

/**
 * <p>RemoteUserAuthenticationProcessingFilter class.</p>
 *
 * @author Timothy Nowaczyk, tan7f@virginia.edu
 * @version $Id: $
 * @since 1.6.12
 */
public class RemoteUserAuthenticationProcessingFilter extends AbstractProcessingFilter {

  /** {@inheritDoc} */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
    String username = (String)request.getAttribute("REMOTE_USER");
    String password = "";

    if (username == null) {
      username = "";
    }

    username = username.trim();

    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
    authRequest.setDetails(authenticationDetailsSource.buildDetails((HttpServletRequest) request));

    return this.getAuthenticationManager().authenticate(authRequest);
  }

  /** {@inheritDoc} */
  @Override
  public String getDefaultFilterProcessesUrl() {
    return "/j_acegi_security_check";
  }
}

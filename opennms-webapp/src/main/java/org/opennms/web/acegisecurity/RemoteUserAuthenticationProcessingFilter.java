package org.opennms.web.acegisecurity;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.AbstractProcessingFilter;

/**
 * @author Timothy Nowaczyk, tan7f@virginia.edu
 */
public class RemoteUserAuthenticationProcessingFilter extends AbstractProcessingFilter {

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

  @Override
  public String getDefaultFilterProcessesUrl() {
    return "/j_acegi_security_check";
  }
}

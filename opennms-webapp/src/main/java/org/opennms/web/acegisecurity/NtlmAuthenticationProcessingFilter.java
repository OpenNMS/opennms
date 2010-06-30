/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opennms.web.acegisecurity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jcifs.Config;
import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.smb.NtlmChallenge;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;
import jcifs.util.LogStream;

import org.acegisecurity.AcegiMessageSource;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator;
import org.acegisecurity.ui.AbstractProcessingFilter;
import org.acegisecurity.ui.AuthenticationDetailsSource;
import org.acegisecurity.ui.AuthenticationDetailsSourceImpl;
import org.acegisecurity.ui.rememberme.NullRememberMeServices;
import org.acegisecurity.ui.rememberme.RememberMeServices;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.Assert;

/**
 * Processes an authentication form.<p>Login forms must present two parameters to this filter: a username and
 * password. The parameter names to use are contained in the static fields {@link #ACEGI_SECURITY_FORM_USERNAME_KEY}
 * and {@link #ACEGI_SECURITY_FORM_PASSWORD_KEY}.</p>
 *  <P><B>Do not use this class directly.</B> Instead configure <code>web.xml</code> to use the {@link
 * org.acegisecurity.util.FilterToBeanProxy}.</p>
 *
 * @author Ben Alex
 * @author Colin Sampaleanu
 * @version $Id: AuthenticationProcessingFilter.java 1496 2006-05-23 13:38:33 +0000 (Tue, 23 May 2006) benalex $
 * @since 1.6.12
 */
public class NtlmAuthenticationProcessingFilter extends BindAuthenticator implements Filter, InitializingBean, ApplicationEventPublisherAware, MessageSourceAware {
    //~ Static fields/initializers =====================================================================================

    //public static final String ACEGI_SECURITY_LAST_USERNAME_KEY = "ACEGI_SECURITY_LAST_USERNAME";
        //~ Instance fields ================================================================================================

        protected ApplicationEventPublisher eventPublisher;
        protected AuthenticationDetailsSource authenticationDetailsSource = new AuthenticationDetailsSourceImpl();
        protected MessageSourceAccessor messages = AcegiMessageSource.getAccessor();
        private Properties exceptionMappings = new Properties();
        private RememberMeServices rememberMeServices = new NullRememberMeServices();
        private LdapAuthoritiesPopulator ldapAuthoritiesPopulator;
        protected List<String> skipNtlmAuthUrls = new ArrayList<String>();
        /**
         * Where to redirect the browser to if authentication is successful but ACEGI_SAVED_REQUEST_KEY is
         * <code>null</code>
         */
        private String defaultTargetUrl;

        /**
         * If <code>true</code>, will always redirect to the value of {@link #getDefaultTargetUrl} upon successful
         * authentication, irrespective of the page that caused the authentication request (defaults to <code>false</code>).
         */
        private boolean alwaysUseDefaultTargetUrl = false;

        private String defaultDomain;
        private String domainController;
        private boolean loadBalance;
        private boolean enableBasic;
        private boolean insecureBasic;
        private String realm;
        private Properties properties;
        private boolean initialized = false;

        //~ constructor
        
        
        /**
         * <p>Constructor for NtlmAuthenticationProcessingFilter.</p>
         *
         * @param initialDirContextFactory a {@link org.acegisecurity.ldap.InitialDirContextFactory} object.
         */
        public NtlmAuthenticationProcessingFilter(
        	    InitialDirContextFactory initialDirContextFactory) {
			super(initialDirContextFactory);

		}
        
        //~ Methods ========================================================================================================

        /**
         * <p>afterPropertiesSet</p>
         *
         * @throws java.lang.Exception if any.
         */
        public void afterPropertiesSet() throws Exception {
            Assert.hasLength(defaultTargetUrl, "defaultTargetUrl must be specified");
            Assert.notNull(properties, "properties must be specified");
            if (domainController == null) 
            	Assert.notNull(properties.getProperty("jcifs.http.domainController"), "jcifs.http.domainController must be specified");
            Assert.notNull(properties.getProperty("jcifs.smb.client.username"), "jcifs.smb.client.username must be specified");
            Assert.notNull(properties.getProperty("jcifs.smb.client.password"), "jcifs.smb.client.password must be specified");
            Assert.notNull(this.rememberMeServices);
        }

        /**
         * Does nothing. We use IoC container lifecycle services instead.
         */
        public void destroy() {}

        /** {@inheritDoc} */
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


    		if (!(request instanceof HttpServletRequest)) {
                throw new ServletException("Can only process HttpServletRequest");
            }

            if (!(response instanceof HttpServletResponse)) {
                throw new ServletException("Can only process HttpServletResponse");
            }
        	
            HttpServletRequest httpRequest = (HttpServletRequest) request;
        	HttpServletResponse httpResponse = (HttpServletResponse) response;

    		log().debug(
    				"passAuthFilter: parsing request: "
    						+ httpRequest.getRequestURL());
    		log().debug("passAuthFilter: AUTH Type: " + httpRequest.getAuthType());
            
        	Authentication authResult = SecurityContextHolder.getContext().getAuthentication();
        	
        	
        	if (authResult == null && !skipAuthUrl(httpRequest)) {
        		if (log().isDebugEnabled()) {
                    log().debug("Request is to process NTLM authentication");
                }
        		boolean authenticated;
                try {
                    onPreAuthentication(httpRequest, httpResponse);
                	
                    authResult= attemptAuthentication(request, response, chain);
                	
                    authenticated = true;
                } catch (AuthenticationException failed) {
                    // Authentication failed
                    unsuccessfulAuthentication(httpRequest, httpResponse, failed);
                    authenticated = false;
                    return;
                }
                
                if (authenticated)
                	successfulAuthentication( httpRequest, httpResponse, authResult);
            } 

        	chain.doFilter(request, response);
            
            // allora se si ottiene successo nell'autenticazione bisogna effettuare il chaindofilter
            // se mi autentico con NTLM non ho bisogno di effettuare altro.
            // allora vediamo di ragionare:
            
            // 1) provo l'autenticazione utilizzando NTLM
            // 2) se fallisce vado avanti nella catena
            // 3) se mi autentico vado avanti nella catena
            // 4) se comincio con /hhhhh.xyz vado avanti nella catena
            // 5) se e' autenticato vado avanti nella catena.
        }

        private boolean skipAuthUrl(HttpServletRequest request) {
			boolean skipAuth = false;	
        	String uri = request.getRequestURI();
        	log().debug("skipAuthUrl: " + uri);
        	int pathParamIndex = uri.indexOf(';');

			if (pathParamIndex > 0) {
					// strip everything after the first semi-colon
					uri = uri.substring(0, pathParamIndex);
			}

			String skipProcessesUrl = null;
			Iterator<String> ite = skipNtlmAuthUrls.iterator();
			while (ite.hasNext()) {
				skipProcessesUrl = ite.next();
	        	log().debug("skipAuthUrl: analizing skipping url" + skipProcessesUrl);				

	        	log().debug("skipAuthurl: contextPath: " + request.getContextPath());
	        	
	        	if ("".equals(request.getContextPath())) {
					skipAuth = uri.startsWith(skipProcessesUrl);
				} else {
					skipAuth = uri.startsWith(request.getContextPath() + skipProcessesUrl);
				}

				if (skipAuth) {
					log().debug("skipping NTLM Authentication");
					return true;
				}
			}
			log().debug("Url request should pass NTLM Authentication");
			return false;

		}

        /**
         * Supplies the default target Url that will be used if no saved request is found or the
         * <tt>alwaysUseDefaultTargetUrl</tt> propert is set to true.
         * Override this method of you want to provide a customized default Url (for example if you want different Urls
         * depending on the authorities of the user who has just logged in).
         *
         * @return the defaultTargetUrl property
         */
        public String getDefaultTargetUrl() {
            return defaultTargetUrl;
        }

        /**
         * <p>Getter for the field <code>exceptionMappings</code>.</p>
         *
         * @return a {@link java.util.Properties} object.
         */
        public Properties getExceptionMappings() {
            return new Properties(exceptionMappings);
        }

        /**
         * <p>Getter for the field <code>rememberMeServices</code>.</p>
         *
         * @return a {@link org.acegisecurity.ui.rememberme.RememberMeServices} object.
         */
        public RememberMeServices getRememberMeServices() {
            return rememberMeServices;
        }

        /**
         * {@inheritDoc}
         *
         * Does JCIFS Initialization. We use IoC container lifecycle services instead.
         */
        public void init(FilterConfig arg0) throws ServletException {
        	if (properties ==null || properties.isEmpty() ) return;
        	
        	log().debug("init");
            String name;
            int level;

            /* Set jcifs properties we know we want; soTimeout and cachePolicy to 10min.
             */
            Config.setProperty( "jcifs.smb.client.soTimeout", "300000" );
            Config.setProperty( "jcifs.netbios.cachePolicy", "1200" );

            Iterator e = properties.keySet().iterator();
            while( e.hasNext() ) {
                name = (String) e.next();
                if( name.startsWith( "jcifs." )) {
                    Config.setProperty( name, (String)properties.get( name ));
                }
            }
            defaultDomain = Config.getProperty("jcifs.smb.client.domain");
            domainController = Config.getProperty( "jcifs.http.domainController" );
            if( domainController == null ) {
                domainController = defaultDomain;
                loadBalance = Config.getBoolean( "jcifs.http.loadBalance", true );
            }
            enableBasic = Boolean.valueOf(
                    Config.getProperty("jcifs.http.enableBasic")).booleanValue();
            insecureBasic = Boolean.valueOf(
                    Config.getProperty("jcifs.http.insecureBasic")).booleanValue();
            realm = Config.getProperty("jcifs.http.basicRealm");
            if (realm == null) realm = "jCIFS";

            if(( level = Config.getInt( "jcifs.util.loglevel", -1 )) != -1 ) {
                LogStream.setLevel( level );
            }
    		initialized = true;
        }
        
        /**
         * This method simply calls <tt>negotiate( req, resp, false )</tt>
         * and then <tt>chain.doFilter</tt>. You can override and call
         * negotiate manually to achive a variety of different behavior.
         *
         * @param request a {@link javax.servlet.ServletRequest} object.
         * @param response a {@link javax.servlet.ServletResponse} object.
         * @param chain a {@link javax.servlet.FilterChain} object.
         * @return a {@link org.acegisecurity.Authentication} object.
         * @throws java.io.IOException if any.
         * @throws javax.servlet.ServletException if any.
         * @throws org.acegisecurity.AuthenticationException if any.
         */
        public Authentication attemptAuthentication( ServletRequest request,
                    ServletResponse response,
                    FilterChain chain ) throws IOException, ServletException,AuthenticationException {
        	if (!initialized) {
        		init(null);
        	}

        	HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse)response;
            NtlmPasswordAuthentication ntlm;

            log().info("initializing negotiation");
            if ((ntlm = negotiate( req, resp, false )) == null) {
                log().info("ntlm negotiation ended with failure");
                throw new BadCredentialsException("NTLM authentication failed");
            }

            //GrantedAuthorityImpl[] gas = {new GrantedAuthorityImpl("ROLE_USER"),new GrantedAuthorityImpl("ROLE_ADMIN")};
            log().debug("ntlm username: " + ntlm.getUsername());
            
            LdapUserDetails userFromSearch = getUserSearch().searchForUser(ntlm.getUsername());
            log().debug("User Detail Found Dn: " + userFromSearch.getDn());

            GrantedAuthority[] gas = ldapAuthoritiesPopulator.getGrantedAuthorities(userFromSearch);


            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ntlm.getUsername(),"",gas);
            
            log().debug("authentication credentials: " + auth.getCredentials());
            

            // Authentication success
            // Place the last username attempted into HttpSession for views
            req.getSession().setAttribute(org.acegisecurity.ui.webapp.AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY, ntlm.getUsername());

            // Allow subclasses to set the "details" property
            setDetails(req, auth);
            return auth;
        }
        
        /**
         * Negotiate password hashes with MSIE clients using NTLM SSP
         *
         * @param req The servlet request
         * @param resp The servlet response
         * @param skipAuthentication If true the negotiation is only done if it is
         * initiated by the client (MSIE post requests after successful NTLM SSP
         * authentication). If false and the user has not been authenticated yet
         * the client will be forced to send an authentication (server sends
         * HttpServletResponse.SC_UNAUTHORIZED).
         * @return True if the negotiation is complete, otherwise false
         * @throws java.io.IOException if any.
         * @throws javax.servlet.ServletException if any.
         */
        protected NtlmPasswordAuthentication negotiate( HttpServletRequest req,
                    HttpServletResponse resp,
                    boolean skipAuthentication ) throws IOException, ServletException {
            UniAddress dc;
            String msg;
            NtlmPasswordAuthentication ntlm = null;
            msg = req.getHeader( "Authorization" );
            log().debug("negotiate: msg: " + msg);
            boolean offerBasic = enableBasic && (insecureBasic || req.isSecure());
            log().debug("negotiate: offerBasic: " + offerBasic);

            if( msg != null && (msg.startsWith( "NTLM " ) ||
                        (offerBasic && msg.startsWith("Basic ")))) {
                if (msg.startsWith("NTLM ")) {
                    HttpSession ssn = req.getSession();
                    byte[] challenge;
                    log().debug("negotiate: loadbalance: " + loadBalance);

                    if( loadBalance ) {
                        NtlmChallenge chal = (NtlmChallenge)ssn.getAttribute( "NtlmHttpChal" );
                        if( chal == null ) {
                            chal = SmbSession.getChallengeForDomain();
                            ssn.setAttribute( "NtlmHttpChal", chal );
                        }
                        dc = chal.dc;
                        challenge = chal.challenge;
                    } else {
                        dc = UniAddress.getByName( domainController, true );
                        challenge = SmbSession.getChallenge( dc );
                    }
                    
                    log().debug("negotiate: challenge: " + challenge.toString());


                    if(( ntlm = NtlmSsp.authenticate( req, resp, challenge )) == null ) {
                        log().debug("negotiate: Auth Failed: ");

                    	return null;
                    }
                    /* negotiation complete, remove the challenge object */
                    log().debug("negotiate: ntlm: " + ntlm.getName());

                    ssn.removeAttribute( "NtlmHttpChal" );
                } else {
                    String auth = new String(Base64.decode(msg.substring(6)),
                            "US-ASCII");
                    int index = auth.indexOf(':');
                    String user = (index != -1) ? auth.substring(0, index) : auth;
                    String password = (index != -1) ? auth.substring(index + 1) :
                            "";
                    index = user.indexOf('\\');
                    if (index == -1) index = user.indexOf('/');
                    String domain = (index != -1) ? user.substring(0, index) :
                            defaultDomain;
                    user = (index != -1) ? user.substring(index + 1) : user;
                    ntlm = new NtlmPasswordAuthentication(domain, user, password);
                    dc = UniAddress.getByName( domainController, true );
                }
                
                try {

                    log().debug("negotiate: logon dc: " + dc);
                    log().debug("negotiate: logon ntlm username: " + ntlm.getName());
                    SmbSession.logon( dc, ntlm );

                    if( log().isDebugEnabled() ) {
                        log().debug( "NtlmHttpFilter: " + ntlm +
                                " successfully authenticated against " + dc );
                    }
                } catch( SmbAuthException sae ) {
                    if( log().isInfoEnabled() ) {
                        log().info( "NtlmHttpFilter: " + ntlm.getName() +
                                ": 0x" + jcifs.util.Hexdump.toHexString( sae.getNtStatus(), 8 ) +
                                ": " + sae );
                    }
                    if( sae.getNtStatus() == SmbAuthException.NT_STATUS_ACCESS_VIOLATION ) {
                        /* Server challenge no longer valid for
                         * externally supplied password hashes.
                         */
                        HttpSession ssn = req.getSession(false);
                        if (ssn != null) {
                            ssn.removeAttribute( "NtlmHttpAuth" );
                        }
                    }
                    resp.setHeader( "WWW-Authenticate", "NTLM" );
                    if (offerBasic) {
                        resp.addHeader( "WWW-Authenticate", "Basic realm=\"" +
                                realm + "\"");
                    }
                    resp.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                    resp.setContentLength(0); /* Marcel Feb-15-2005 */
                    resp.flushBuffer();
                    return null;
                }
                req.getSession().setAttribute( "NtlmHttpAuth", ntlm );
            } else {
                if (!skipAuthentication) {
                    HttpSession ssn = req.getSession(false);
                    if (ssn == null || (ntlm = (NtlmPasswordAuthentication)
                                ssn.getAttribute("NtlmHttpAuth")) == null) {
                        resp.setHeader( "WWW-Authenticate", "NTLM" );
                        if (offerBasic) {
                            resp.addHeader( "WWW-Authenticate", "Basic realm=\"" +
                                    realm + "\"");
                        }
                        resp.setStatus( HttpServletResponse.SC_UNAUTHORIZED );
                        resp.setContentLength(0);
                        resp.flushBuffer();
                        return null;
                    }
                }
            }

            return ntlm;
        }

    	/**
    	 * <p>Getter for the field <code>defaultDomain</code>.</p>
    	 *
    	 * @return a {@link java.lang.String} object.
    	 */
    	public String getDefaultDomain() {
    		return defaultDomain;
    	}

    	/**
    	 * <p>Setter for the field <code>defaultDomain</code>.</p>
    	 *
    	 * @param defaultDomain a {@link java.lang.String} object.
    	 */
    	public void setDefaultDomain(String defaultDomain) {
    		this.defaultDomain = defaultDomain;
    	}

    	/**
    	 * <p>Getter for the field <code>domainController</code>.</p>
    	 *
    	 * @return a {@link java.lang.String} object.
    	 */
    	public String getDomainController() {
    		return domainController;
    	}

    	/**
    	 * <p>Setter for the field <code>domainController</code>.</p>
    	 *
    	 * @param domainController a {@link java.lang.String} object.
    	 */
    	public void setDomainController(String domainController) {
    		this.domainController = domainController;
    	}

    	/**
    	 * <p>isLoadBalance</p>
    	 *
    	 * @return a boolean.
    	 */
    	public boolean isLoadBalance() {
    		return loadBalance;
    	}

    	/**
    	 * <p>Setter for the field <code>loadBalance</code>.</p>
    	 *
    	 * @param loadBalance a boolean.
    	 */
    	public void setLoadBalance(boolean loadBalance) {
    		this.loadBalance = loadBalance;
    	}

    	/**
    	 * <p>isEnableBasic</p>
    	 *
    	 * @return a boolean.
    	 */
    	public boolean isEnableBasic() {
    		return enableBasic;
    	}

    	/**
    	 * <p>Setter for the field <code>enableBasic</code>.</p>
    	 *
    	 * @param enableBasic a boolean.
    	 */
    	public void setEnableBasic(boolean enableBasic) {
    		this.enableBasic = enableBasic;
    	}

    	/**
    	 * <p>isInsecureBasic</p>
    	 *
    	 * @return a boolean.
    	 */
    	public boolean isInsecureBasic() {
    		return insecureBasic;
    	}

    	/**
    	 * <p>Setter for the field <code>insecureBasic</code>.</p>
    	 *
    	 * @param insecureBasic a boolean.
    	 */
    	public void setInsecureBasic(boolean insecureBasic) {
    		this.insecureBasic = insecureBasic;
    	}

    	/**
    	 * <p>Getter for the field <code>realm</code>.</p>
    	 *
    	 * @return a {@link java.lang.String} object.
    	 */
    	public String getRealm() {
    		return realm;
    	}

    	/**
    	 * <p>Setter for the field <code>realm</code>.</p>
    	 *
    	 * @param realm a {@link java.lang.String} object.
    	 */
    	public void setRealm(String realm) {
    		this.realm = realm;
    	}

    	/**
    	 * <p>Getter for the field <code>properties</code>.</p>
    	 *
    	 * @return a {@link java.util.Properties} object.
    	 */
    	public Properties getProperties() {
    		return properties;
    	}

    	/**
    	 * <p>Setter for the field <code>properties</code>.</p>
    	 *
    	 * @param properties a {@link java.util.Properties} object.
    	 */
    	public void setProperties(Properties properties) {
    		this.properties = properties;
    	}


        /**
         * <p>isAlwaysUseDefaultTargetUrl</p>
         *
         * @return a boolean.
         */
        public boolean isAlwaysUseDefaultTargetUrl() {
            return alwaysUseDefaultTargetUrl;
        }

        /**
         * <p>onPreAuthentication</p>
         *
         * @param request a {@link javax.servlet.http.HttpServletRequest} object.
         * @param response a {@link javax.servlet.http.HttpServletResponse} object.
         * @throws org.acegisecurity.AuthenticationException if any.
         * @throws java.io.IOException if any.
         */
        protected void onPreAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {}

        /**
         * <p>onSuccessfulAuthentication</p>
         *
         * @param request a {@link javax.servlet.http.HttpServletRequest} object.
         * @param response a {@link javax.servlet.http.HttpServletResponse} object.
         * @param authResult a {@link org.acegisecurity.Authentication} object.
         * @throws java.io.IOException if any.
         */
        protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) throws IOException {
 
        }

        /**
         * <p>onUnsuccessfulAuthentication</p>
         *
         * @param request a {@link javax.servlet.http.HttpServletRequest} object.
         * @param response a {@link javax.servlet.http.HttpServletResponse} object.
         * @param failed a {@link org.acegisecurity.AuthenticationException} object.
         * @throws java.io.IOException if any.
         */
        protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException {
        }

         /**
          * <p>Setter for the field <code>alwaysUseDefaultTargetUrl</code>.</p>
          *
          * @param alwaysUseDefaultTargetUrl a boolean.
          */
         public void setAlwaysUseDefaultTargetUrl(boolean alwaysUseDefaultTargetUrl) {
            this.alwaysUseDefaultTargetUrl = alwaysUseDefaultTargetUrl;
        }

        /** {@inheritDoc} */
        public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        /**
         * <p>Setter for the field <code>authenticationDetailsSource</code>.</p>
         *
         * @param authenticationDetailsSource a {@link org.acegisecurity.ui.AuthenticationDetailsSource} object.
         */
        public void setAuthenticationDetailsSource(AuthenticationDetailsSource authenticationDetailsSource) {
            Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
            this.authenticationDetailsSource = authenticationDetailsSource;
        }

        /**
         * <p>Setter for the field <code>defaultTargetUrl</code>.</p>
         *
         * @param defaultTargetUrl a {@link java.lang.String} object.
         */
        public void setDefaultTargetUrl(String defaultTargetUrl) {
            Assert.isTrue(defaultTargetUrl.startsWith("/") | defaultTargetUrl.startsWith("http"),
                    "defaultTarget must start with '/' or with 'http(s)'");
            this.defaultTargetUrl = defaultTargetUrl;
        }

        /**
         * <p>Setter for the field <code>exceptionMappings</code>.</p>
         *
         * @param exceptionMappings a {@link java.util.Properties} object.
         */
        public void setExceptionMappings(Properties exceptionMappings) {
            this.exceptionMappings = exceptionMappings;
        }

        /** {@inheritDoc} */
        public void setMessageSource(MessageSource messageSource) {
            this.messages = new MessageSourceAccessor(messageSource);
        }

        /**
         * <p>Setter for the field <code>rememberMeServices</code>.</p>
         *
         * @param rememberMeServices a {@link org.acegisecurity.ui.rememberme.RememberMeServices} object.
         */
        public void setRememberMeServices(RememberMeServices rememberMeServices) {
            this.rememberMeServices = rememberMeServices;
        }

        /**
         * <p>successfulAuthentication</p>
         *
         * @param request a {@link javax.servlet.http.HttpServletRequest} object.
         * @param response a {@link javax.servlet.http.HttpServletResponse} object.
         * @param authResult a {@link org.acegisecurity.Authentication} object.
         * @throws java.io.IOException if any.
         */
        protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) throws IOException {
            if (log().isDebugEnabled()) {
                log().debug("Authentication success: " + authResult.toString());
            }

            SecurityContextHolder.getContext().setAuthentication(authResult);

            if (log().isDebugEnabled()) {
                log().debug("Updated SecurityContextHolder to contain the following Authentication: '" + authResult + "'");
            }

            onSuccessfulAuthentication(request, response, authResult);

            rememberMeServices.loginSuccess(request, response, authResult);

            // Fire event
            if (this.eventPublisher != null) {
                eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
            }

        }
        
        /**
         * <p>determineTargetUrl</p>
         *
         * @param request a {@link javax.servlet.http.HttpServletRequest} object.
         * @return a {@link java.lang.String} object.
         */
        protected String determineTargetUrl(HttpServletRequest request) {
            // Don't attempt to obtain the url from the saved request if alwaysUsedefaultTargetUrl is set
            String targetUrl = alwaysUseDefaultTargetUrl ? null : AbstractProcessingFilter.obtainFullRequestUrl(request);

            if (targetUrl == null) {
                targetUrl = getDefaultTargetUrl();
            }

            return targetUrl;
        }

        /**
         * <p>unsuccessfulAuthentication</p>
         *
         * @param request a {@link javax.servlet.http.HttpServletRequest} object.
         * @param response a {@link javax.servlet.http.HttpServletResponse} object.
         * @param failed a {@link org.acegisecurity.AuthenticationException} object.
         * @throws java.io.IOException if any.
         */
        protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException {
            SecurityContextHolder.getContext().setAuthentication(null);

            if (log().isDebugEnabled()) {
                log().debug("Updated SecurityContextHolder to contain null Authentication");
            }

            if (log().isDebugEnabled()) {
                log().debug("Authentication request failed: " + failed.toString());
            }

            try {
                request.getSession().setAttribute(AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY, failed);
            } catch (Exception ignored) {}

            onUnsuccessfulAuthentication(request, response, failed);

            rememberMeServices.loginFail(request, response);

        }

        /**
         * <p>Getter for the field <code>authenticationDetailsSource</code>.</p>
         *
         * @return a {@link org.acegisecurity.ui.AuthenticationDetailsSource} object.
         */
        public AuthenticationDetailsSource getAuthenticationDetailsSource() {
            // Required due to SEC-310
            return authenticationDetailsSource;
        }

    	Category log() {
        	ThreadCategory.setPrefix("OpenNMS.WEB.AUTH");
        	return ThreadCategory.getInstance(this.getClass());

    	}
        /**
         * Provided so that subclasses may configure what is put into the authentication request's details
         * property.
         *
         * @param request that an authentication request is being created for
         * @param authRequest the authentication request object that should have its details set
         */
        protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
            authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        }

		/**
		 * <p>Getter for the field <code>skipNtlmAuthUrls</code>.</p>
		 *
		 * @return a {@link java.util.List} object.
		 */
		public List<String> getSkipNtlmAuthUrls() {
			return skipNtlmAuthUrls;
		}

		/**
		 * <p>Setter for the field <code>skipNtlmAuthUrls</code>.</p>
		 *
		 * @param skipNtlmAuthUrls a {@link java.util.List} object.
		 */
		public void setSkipNtlmAuthUrls(List<String> skipNtlmAuthUrls) {
			this.skipNtlmAuthUrls = skipNtlmAuthUrls;
		}

		/**
		 * <p>Getter for the field <code>ldapAuthoritiesPopulator</code>.</p>
		 *
		 * @return a {@link org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator} object.
		 */
		public LdapAuthoritiesPopulator getLdapAuthoritiesPopulator() {
			return ldapAuthoritiesPopulator;
		}

		/**
		 * <p>Setter for the field <code>ldapAuthoritiesPopulator</code>.</p>
		 *
		 * @param ldapAuthoritiesPopulator a {@link org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator} object.
		 */
		public void setLdapAuthoritiesPopulator(
				LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
			this.ldapAuthoritiesPopulator = ldapAuthoritiesPopulator;
		}
		
}

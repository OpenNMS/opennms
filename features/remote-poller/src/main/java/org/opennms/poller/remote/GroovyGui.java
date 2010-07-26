package org.opennms.poller.remote;

/**
 * <p>GroovyGui interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface GroovyGui {

	/**
	 * <p>getAuthenticationBean</p>
	 *
	 * @return a {@link org.opennms.poller.remote.AuthenticationBean} object.
	 */
	public AuthenticationBean getAuthenticationBean();
	/**
	 * <p>createAndShowGui</p>
	 */
	public void createAndShowGui();

}

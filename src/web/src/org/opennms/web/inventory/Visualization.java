/*
 * Creato il 27-ago-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.inventory;

import java.io.IOException;
import java.util.Map;
import org.opennms.netmgt.inventory.UnparsableConfigurationException;

/**
 * @author maurizio
 */
public interface Visualization {
	public String getVisualization(String filePath, Map parameters) throws IOException,UnparsableConfigurationException;
}

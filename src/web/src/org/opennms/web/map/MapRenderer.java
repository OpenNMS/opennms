/*
 * Creato il 24-ago-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.map;

import org.opennms.web.map.view.*;

/**
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
public interface MapRenderer {
	public abstract byte[] getRenderedMap(VMap map, String displayType);
}

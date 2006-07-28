/*
 * Creato il 27-ott-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.map;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opennms.web.map.view.*;


/**
 * @author micmas
 * 
 * Per modificare il modello associato al commento di questo tipo generato,
 * aprire Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e
 * commenti
 */
public class MapsServlet extends HttpServlet {
    /*
     * (non Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */

    private final static String key = "MapsServlet";

    private final static int DISPLAY = 0;

    private final static int DELETE = 1;

    private final static int SAVE = 2;

/*    private final static int SAVEAS = 3;

    private final static int UPDATEELEMENT = 4;

    private final static int UPDATEMAP = 5;*/

    private String[] operations = { "display", "delete", "save"/*, "saveas",
            "updateElement", "updateMap",*/ };

    private static Manager m_manager = new Manager();

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /*
     * (non Javadoc)
     * 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        HttpSession userSession = request.getSession(false);
        String operation = request.getParameter("operation");

        if (operation == null || operation.equals("")) {
            // send error
            this.log("ERROR: parameter 'operation' omitted");
            throw new ServletException("Parameter 'operation' omitted");
        }
        if (operation.equals(operations[DISPLAY])) {
            display(request, response, userSession);
            return;
        }
        if (operation.equals(operations[DELETE])) {
            //
            deleteMap(request, response, userSession);
            return;
        }
        if (operation.equals(operations[SAVE])) {
            //
            saveMap(request, response, userSession);
            return;
        }
       /* if (operation.equals(operations[SAVEAS])) {
            //
            saveMapAs(request, response, userSession);
            return;
        }
        if (operation.equals(operations[UPDATEELEMENT])) {
            //
            updateElement(request, response, userSession);
            return;
        }
        if (operation.equals(operations[UPDATEMAP])) {
            //
            updateMap(request, response, userSession);
            return;
        }*/

        // operation not defined,
        // send error HttpServletResponse.SC_NOT_IMPLEMENTED
        // String error = "<reponse>ERROR</response>";
        // this.log("ERROR: parameter 'operation' not valid");
        // response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, error);
        this.log("ERROR: parameter 'operation' omitted");
        throw new ServletException("Parameter 'operation' omitted");
    }

    void display(HttpServletRequest request, HttpServletResponse response,
            HttpSession userSession) {

        byte[] value = null;
        String contentType = "image/xml+svg"; // da leggere da file di
        // configurazione

        String idString = request.getParameter("id");
        if (idString == null || idString.equals("")) {
            // errore
        }
        int id = Integer.parseInt(idString);

        String displayType = request.getParameter("id");
        if (displayType == null || displayType.equals("")) {
            // errore
        }

        VMap map = null;

        try {
            map = m_manager.getMap(id);
        }
         catch (MapNotFoundException e) {
            //TODO log
            //String error = "<response>ERROR</response>";
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (IOException ee) {
                // TODO log
            }
        }catch(MapsException m){
        	//TODO manage exception 
        }

        // add Map to session variables
        userSession.setAttribute(key + idString, map);
        MapRenderer renderer = MapRendererFactory.getRenderer();
        value = renderer.getRenderedMap(map, displayType);
        response.setContentType(contentType);
        try {
            response.getOutputStream().write(value);
        } catch (IOException e) {
            // TODO log
        } finally {
            try {
                response.getOutputStream().close();
            } catch (IOException e) {
                // TODO log
            }
        }
    }

    void deleteMap(HttpServletRequest request, HttpServletResponse response,
            HttpSession userSession) {
        byte[] value = null;
        try{
        	m_manager.deleteMap(Integer.parseInt(request.getParameter("id")));
        }catch(MapsException m){
        	try{
        		//TODO log
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,m.getMessage());
        	}catch(IOException i){
        		//TODO log
        	}
        }
        try{
        	response.sendError(HttpServletResponse.SC_OK);
        }catch(IOException io){
        	//TODO manage exception
        }
    }

    void saveMap(HttpServletRequest request, HttpServletResponse response,
            HttpSession userSession) {
        byte[] value = null;
        try{
        	m_manager.save((VMap)userSession.getAttribute("CurrentMap"));
        }catch(MapsException m){
//        	TODO log
        	try{
        		//TODO log
        		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,m.getMessage());
        	}catch(IOException i){
        		//TODO log
        	}
        }
        
    }

/*    void saveMapAs(HttpServletRequest request, HttpServletResponse response,
            HttpSession userSession) {
        byte[] value = null;

    }

    void updateElement(HttpServletRequest request,
            HttpServletResponse response, HttpSession userSession) {
        byte[] value = null;

    }

    void updateMap(HttpServletRequest request, HttpServletResponse response,
            HttpSession userSession) {
        byte[] value = null;
    }*/
}

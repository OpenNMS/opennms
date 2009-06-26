package org.opennms.features.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint{

  /**
   * This is the entry point method.
   */
  
    String m_url = "http://localhost:8080/org.opennms.features.webapp.base-1.7.5-SNAPSHOT/rest/nodes/4";
    RequestBuilder m_builder = new RequestBuilder(RequestBuilder.GET, URL.encode(m_url));
  
  public void onModuleLoad(){
     final Label label = new Label ( "gwt-maven-plugin Archetype :: Project org.opennms.features.webapp.org.opennms.features.webapp.home" );
     final Label label2 = new Label("this is label2 where the response goes");
     RootPanel.get().add( label );
     RootPanel.get().add(label2);
     
     try{
         Request request = m_builder.sendRequest(null, new RequestCallback(){
    
            public void onError(Request arg0, Throwable arg1) {
                label.setText("could not load");
                
            }
    
            public void onResponseReceived(Request request, Response response) {
                label.setText("got a response");
                label2.setText(response.getText());
                
            }
             
         });
     }catch(RequestException e){
         
     }
  }
}

package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import com.googlecode.gwtmapquest.jsapi.MQExec;
import com.googlecode.gwtmapquest.transaction.MQATileMap;
import com.googlecode.gwtmapquest.transaction.MQAZoomControl;

public class MapquestLocationManager extends AbstractLocationManager implements LocationManager {
	private static final String MAPQUEST_URL="http://btilelog.access.mapquest.com/tilelog/transaction?transaction=script&itk=true&v=5.3.s&ipkg=controls1";
	private static final String GEOCODER_URL = "http://mapquestapi.com/geocoding/v1/address?location=lancaster%20pa";

	private final Application m_application;
	private final SplitLayoutPanel m_panel;

	private MQATileMap m_map;
	private MQExec m_geoExec;

	public MapquestLocationManager(Application application, HandlerManager eventBus, SplitLayoutPanel splitPanel) {
		super(eventBus);
		m_application = application;
		m_panel = splitPanel;
	}

	@Override
	public void initialize() {
		DeferredCommand.addCommand(new InitializationCommand() {

			@Override
			protected void loadMapApi() throws InitializationException {
				Document doc = Document.get();

				ScriptElement script = doc.createScriptElement();
				script.setSrc(MAPQUEST_URL + "&key=" + getApiKey());
				script.setType("text/javascript");
				doc.getBody().appendChild(script);

				script = doc.createScriptElement();
				script.setSrc("mapquest/debug/mqobjects.js");
				script.setType("text/javascript");
				doc.getBody().appendChild(script);
				
				script = doc.createScriptElement();
				script.setSrc("mapquest/debug/mqutils.js");
				script.setType("text/javascript");
				doc.getBody().appendChild(script);

				SimplePanel sp = new SimplePanel();
				m_panel.add(sp);

				m_map = MQATileMap.newInstance(sp.getElement());
				m_map.addControl(MQAZoomControl.newInstance());
			}
		});
	}

	@Override
	public void updateLocation(final Location location) {
		if (location == null) return;
		
	}
	
	@Override
	public void removeLocation(final Location location) {
		if (location == null) return;
		GWTLatLng latLng = getGeolocation(location);
		if (latLng != null) {
			MapQuestLocation loc = new MapQuestLocation(location);
			loc.setLatLng(latLng);
			updateMarker(loc);
		} else {
			RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, GEOCODER_URL + "&key=" + getApiKey());
			rb.setCallback(new RequestCallback() {
				public void onError(Request req, Throwable throwable) {
					final MapQuestLocation mql;
					if (location instanceof MapQuestLocation) {
						mql = (MapQuestLocation)location;
					} else {
						mql = new MapQuestLocation(location);
					}
					mql.setLatLng(GWTLatLng.getDefault());
					updateMarker(mql);
				}

				public void onResponseReceived(Request req, Response resp) {
					resp.getText();
					JSONValue value = JSONParser.parse(resp.getText());
				}
				
			});
		}
	}

	private void updateMarker(final Location location) {
		
	}
	
	@Override
	public void fitToMap() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Location> getAllLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Location> getVisibleLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Location getLocation(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Location> getLocations(int startIndex, int maxRows) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void selectLocation(String locationName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportError(String string, Throwable t) {
		// TODO Auto-generated method stub

	}

}

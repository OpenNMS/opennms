Leaflet.Control.Search
============

What ?
------

A leaflet control that search markers location by property, with ajax/jsonp autocomplete feature

Tested in Leaflet 0.5


How ?
------

Adding the search control to the map:

```

map.addControl(new L.Control.Search({layer: searchLayer}));
//searchLayer contains searched markers

map.addControl(new L.Control.Search({searchJsonpUrl: 'search.php?q={s}&callback={c}'}) );
//searchJsonpUrl is jsonp service for retrieve elements locations

and insert leaflet-search.css styles to your css page

```

Where ?
------

Source code:
	https://github.com/stefanocudini/leaflet-search
	https://bitbucket.org/zakis_/leaflet-search

Demos:
	http://labs.easyblog.it/maps/leaflet-search/


/**
 * Allows pre-fabricated graphs to be rendered using different graphing engines.
 *
 * This function aims to centralize all of the logic required to determine the
 * appropriate engine and trigger the rendering of the graphs.
 *
 * @author jwhite
 */
GraphContainers = (function () {
  "use strict";

  var $j = jQuery.noConflict(); // Avoid conflicts with prototype.js used by graph/cropper/zoom.js

  var librariesLoaded = {};
  var dependenciesResolved = false;

  var getGraphingEngine = function() {
    var graphingEngine = "png";
    if (window.onmsGraphContainers !== undefined
          && window.onmsGraphContainers.engine !== undefined
          && window.onmsGraphContainers.engine !== null) {
        graphingEngine = window.onmsGraphContainers.engine;
    }
    return graphingEngine.toLowerCase();
  };

  var loadCSS = function(href) {
    var cssLink = $j("<link rel='stylesheet' type='text/css' href='"+window.onmsGraphContainers.baseHref+href+"'>");
    $j("head").append(cssLink);
  };

  var loadJS = function(src) {
    return $j.getScript(window.onmsGraphContainers.baseHref+src);
  };

  /**
   * Waits for the libraries to be executed (not just loaded) by
   * listening for events.
   *
   * Every library that is used must contain a snippet of the form:
   *   jQuery(document).trigger("libraryLoaded", "name");
   *
   */
  var waitForLibraries = function(libraries, dfd) {
    var k, n = libraries.length, missingLibrary;
    $j(document).on("libraryLoaded", {}, function(event, name) {
      //console.log("Got library loaded for:", name);
      librariesLoaded[name] = true;

      missingLibrary = false;
      for (k = 0; k < n; k++) {
        if(!librariesLoaded.hasOwnProperty(libraries[k])) {
          //console.log("Still missing: " + libraries[k]);
          missingLibrary = true;
          break;
        }
      }

      if (!missingLibrary) {
        dfd.resolve();
      }
    });
  };

  /**
   * Load the dependenices required by the target graphing engine
   * and return a promise that will resolve once these are
   * ready to be used.
   */
  var loadDependencies = function() {
    var dfd = $j.Deferred();

    if (window.onmsGraphContainers === undefined) {
      dfd.reject();
      return dfd;
    }

    var graphingEngine = getGraphingEngine();
    if (graphingEngine == "placeholder") {
      loadJS("js/holder.min.js")
        .then(waitForLibraries(["holder"], dfd));
    } else if (graphingEngine == "backshift") {
      loadCSS("lib/c3/c3.min.css");
      loadJS("js/holder.min.js")
        .then(loadJS("lib/d3/d3.min.js"))
        .then(loadJS("lib/c3/c3.min.js"))
        .then(loadJS("lib/rsvp/rsvp.min.js"))
        .then(loadJS("js/backshift.onms.min.js"))
        .then(waitForLibraries(["holder", "d3", "c3", "rsvp", "backshift"], dfd));
    } else {
      // Nothing to load
      dfd.resolve();
    }

    return dfd;
  };

  /**
   * Renders the graph with an image tag pointed to graph/graph.png
   */
  var drawPngGraph = function(el, def, dim) {
    var graphUrlParams = {
      'resourceId': def.resourceId,
      'report': def.graphName,
      'start': def.start,
      'end': def.end,
      'width': dim.width,
      'height': dim.height
    };
    var graphUrl = window.onmsGraphContainers.baseHref + "graph/graph.png?" + $j.param(graphUrlParams);

    var altSuffix;
    var imgTagAttrs = "";
    if (def.zooming) {
      altSuffix = ' (drag to zoom)';
      imgTagAttrs = 'id="zoomImage"';
    } else {
      altSuffix = ' (click to zoom)';
    }

    var graphDom = '<img ' + imgTagAttrs + ' class="graphImg" src="' + graphUrl + '" alt="Resource graph: ' + def.graphTitle + altSuffix + '" />';
    if (def.zoomable && !def.zooming) {
      var zoomUrlParams = {
        'zoom': true,
        'relativetime': 'custom',
        'resourceId': def.resourceId,
        'reports': def.graphName,
        'start': def.start,
        'end': def.end
      };

      var zoomUrl = window.onmsGraphContainers.baseHref + 'graph/results.htm?' + $j.param(zoomUrlParams);
      graphDom = '<a href="' + zoomUrl + '">' + graphDom + '</a>';
    }

    el.html(graphDom);

    if (def.zooming) {
      // There can only be a single image on the page
      var img = $j("#zoomImage");
      img.width(dim.width);
      img.height(dim.height);
    }
  };

  /**
   * Renders a placeholder using holder.js
   */
  var drawPlaceholderGraph = function(el, def, dim) {
    var text = def.graphTitle;

    if (text === undefined || text === null) {
      // Use the dimensions if no title is set
      text = dim.width + 'x' + dim.height;
    } else {
      // Append the dimensions otherwise
      text += " (" +  dim.width + 'x' + dim.height + ")";
    }

    el.html('<img class="graph-placeholder" data-src="holder.js/' + dim.width + 'x' + dim.height + '?text=' + text + '">');
  };

  /**
   * Renders the graph using Backshift
   */
  var drawBackshiftGraph = function(el, def, dim) {
    // Pull in the graph definition
    $j.ajax({
      url: window.onmsGraphContainers.baseHref + 'rest/graphs/' + encodeURIComponent(def.graphName),
      dataType: 'json'
    }).done(function (graphDef) {
      // Convert the graph definition to a supported model
      var rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
        graphDef: graphDef,
        resourceId: def.resourceId
      });
      var graphModel = rrdGraphConverter.model;

      // Build the data-source
      var ds = new Backshift.DataSource.OpenNMS({
        url: window.onmsGraphContainers.baseHref + "rest/measurements",
        metrics: graphModel.metrics
      });

      // Build and render the graph
      var graph = new Backshift.Graph.C3({
        element: el[0],
        width: dim.width,
        height: dim.height,
        start: def.start,
        end: def.end,
        dataSource: ds,
        series: graphModel.series,
        step: true,
        title: graphModel.title,
        verticalLabel: graphModel.verticalLabel
      });
      graph.render();
    }).fail(function(jqXHR, textStatus) {
      var text = "Request failed: " + textStatus;
      el.html('<img class="graph-placeholder" data-src="holder.js/' + dim.width + 'x' + dim.height + '?text=' + text + '">');
    });
  };

  var getDimensionsForElement = function(el, def) {
    var width = Math.round(el.width() * def.widthRatio);
    return {
      'width': width,
      'height': Math.round(width * def.heightRatio)
    };
  };

  var render = function() {
    if (!dependenciesResolved) {
      loadDependencies().done(_render());
    } else {
      _render();
    }
  };

  var _render = function () {
    var didDrawOneOrMorePlaceholders = false;

    $j(".graph-container").each(function () {
      // Grab the element
      var el = $j(this);

      // Extract the attributes
      var def = {
        'resourceId': el.data("resource-id"),
        'graphName': el.data("graph-name"),
        'graphTitle': el.data("graph-title"),
        'start': el.data("graph-start"),
        'end': el.data("graph-end"),
        'zooming': el.data("graph-zooming"),
        'zoomable': el.data("graph-zoomable"),
        'widthRatio': el.data("width-ratio"),
        'heightRatio': el.data("height-ratio")
      };

      // Skip the entry when any of the required fields are missing
      if (def.resourceId === undefined || def.resourceId === null || def.resourceId === "") {
        return;
      }
      if (def.graphName === undefined || def.graphName === null || def.graphName === "") {
        return;
      }

      // Use sane defaults
      if (def.end === undefined || def.end === null) {
        def.end = new Date().getTime();
      }
      if (def.start === undefined || def.start === null) {
        def.start = def.end - (24 * 60 * 60 * 1000); // 24 hours ago.
      }
      if (def.widthRatio === undefined || def.widthRatio === null) {
        def.widthRatio = 0.8;
      }
      if (def.heightRatio === undefined || def.heightRatio === null) {
        def.heightRatio = 0.3;
      }

      // Determine the target dimensions
      var dim = getDimensionsForElement(el, def);

      // Render the graph using the appropriate engine
      var graphingEngine = "png";
      if (window.onmsGraphContainers != undefined) {
        graphingEngine = window.onmsGraphContainers.engine;
      }

      if (graphingEngine === "placeholder") {
        drawPlaceholderGraph(el, def, dim);
        didDrawOneOrMorePlaceholders = true;
      } else if (graphingEngine === "backshift") {
        drawBackshiftGraph(el, def, dim);
        didDrawOneOrMorePlaceholders = true;
      } else {
        drawPngGraph(el, def, dim);
      }

      // Notify other components (i.e cropper) that we have loaded a graph
      $j(document).trigger("graphLoaded", [dim.width, dim.height]);
    });

    if (didDrawOneOrMorePlaceholders) {
      if (window.Holder !== undefined) {
        Holder.run({images: ".graph-placeholder"});
      }
    }
  };

  // Automatically trigger a render on load
  $j(function () {
    render();
  });

  return {
    render: render
  }
})();

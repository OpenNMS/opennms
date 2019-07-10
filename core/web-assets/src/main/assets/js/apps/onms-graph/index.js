/**
 * Allows pre-fabricated graphs to be rendered using different graphing engines.
 *
 * This function aims to centralize all of the logic required to determine the
 * appropriate engine and trigger the rendering of the graphs.
 *
 * @author jwhite
 */

require('vendor/jquery-ui-js');
const $j = require('vendor/flot-js');

const Backshift = require('vendor/backshift-js');
const holder = require('vendor/holder-js');

let cssLoaded = false;

const getGraphingEngine = () => {
  let graphingEngine = 'png';
  if (window.onmsGraphContainers !== undefined
        && window.onmsGraphContainers.engine !== undefined
        && window.onmsGraphContainers.engine !== null) {
      graphingEngine = window.onmsGraphContainers.engine;
  }
  return graphingEngine.toLowerCase();
};

const loadCSS = (href) => {
  const cssLink = $j('<link rel="stylesheet" type="text/css" href="' + window.onmsGraphContainers.baseHref+href+'">');
  $j('head').append(cssLink);
};

/**
 * Renders the graph with an image tag pointed to graph/graph.png
 */
const drawPngGraph = (el, def, dim) => {
  const graphUrlParams = {
    'resourceId': def.resourceId,
    'report': def.graphName,
    'start': def.start,
    'end': def.end,
    'width': dim.width,
    'height': dim.height
  };
  const graphUrl = window.onmsGraphContainers.baseHref + 'graph/graph.png?' + $j.param(graphUrlParams);

  let altSuffix = ' (click to zoom)';
  let imgTagAttrs = '';
  if (def.zooming) {
    altSuffix = ' (drag to zoom)';
    imgTagAttrs = 'id="zoomImage"';
  }

  let graphDom = '<img ' + imgTagAttrs + ' class="graphImg" src="' + graphUrl + '" alt="Resource graph: ' + def.graphTitle + altSuffix + '" />';
  if (def.zoomable && !def.zooming) {
    const zoomUrlParams = {
      'zoom': true,
      'relativetime': 'custom',
      'resourceId': def.resourceId,
      'reports': def.graphName,
      'start': def.start,
      'end': def.end
    };

    const zoomUrl = window.onmsGraphContainers.baseHref + 'graph/results.htm?' + $j.param(zoomUrlParams);
    graphDom = '<a href="' + zoomUrl + '">' + graphDom + '</a>';
  }

  el.html(graphDom);

  if (def.zooming) {
    // There can only be a single image on the page
    const img = $j('#zoomImage');
    img.width(dim.width);
    img.height(dim.height);
  }
};

/**
 * Renders a placeholder using holder.js
 */
const drawPlaceholderGraph = (el, def, dim) => {
  let text = def.graphTitle;

  if (text === undefined || text === null) {
    // Use the dimensions if no title is set
    text = dim.width + 'x' + dim.height;
  } else {
    // Append the dimensions otherwise
    text += ' (' +  dim.width + 'x' + dim.height + ')';
  }

  el.html('<img class="graph-placeholder" data-src="holder.js/' + dim.width + 'x' + dim.height + '?text=' + text + '">');
};

/**
 * Renders the graph using Backshift
 */
const drawBackshiftGraph = (el, def, dim) => {
  // Pull in the graph definition
  $j.ajax({
    url: window.onmsGraphContainers.baseHref + 'rest/graphs/' + encodeURIComponent(def.graphName),
    dataType: 'json'
  }).done((graphDef) => {
    // Convert the graph definition to a supported model
    const rrdGraphConverter = new Backshift.Utilities.RrdGraphConverter({
      graphDef: graphDef,
      resourceId: def.resourceId
    });
    const graphModel = rrdGraphConverter.model;

    // Build the data-source
    const ds = new Backshift.DataSource.OpenNMS({
      url: window.onmsGraphContainers.baseHref + 'rest/measurements',
      metrics: graphModel.metrics
    });

    // Build and render the graph
    const graph = new Backshift.Graph.Flot({
      element: el[0],
      width: dim.width,
      height: dim.height,
      start: def.start,
      end: def.end,
      dataSource: ds,
      model: graphModel,
      printStatements: graphModel.printStatements,
      title: graphModel.title,
      verticalLabel: graphModel.verticalLabel
    });
    graph.render();
  }).fail((jqXHR, textStatus) => {
    const text = 'Request failed: ' + textStatus;
    el.html('<img class="graph-placeholder" data-src="holder.js/' + dim.width + 'x' + dim.height + '?text=' + text + '">');
  });
};

const getDimensionsForElement = (el, def) => {
  const width = Math.round(el.width() * def.widthRatio);
  return {
    'width': width,
    'height': Math.round(width * def.heightRatio)
  };
};

const render = () => {
  let didDrawOneOrMorePlaceholders = false;

  let graphingEngine = 'png';
  if (window.onmsGraphContainers !== undefined) {
    graphingEngine = window.onmsGraphContainers.engine;
  }

  if (graphingEngine === 'backshift' && !cssLoaded) {
      cssLoaded = true;
  }

  $j('.graph-container').each((index, e) => {
    const el = $j(e);
    let didGraphRendered = false;
    // Extract the attributes
    let def = {
      'resourceId': el.data('resource-id'),
      'graphName': el.data('graph-name'),
      'graphTitle': el.data('graph-title'),
      'start': el.data('graph-start'),
      'end': el.data('graph-end'),
      'zooming': el.data('graph-zooming'),
      'zoomable': el.data('graph-zoomable'),
      'widthRatio': el.data('width-ratio'),
      'heightRatio': el.data('height-ratio')
    };

    // Skip the entry when any of the required fields are missing
    if (def.resourceId === undefined || def.resourceId === null || def.resourceId === '') {
      return;
    }
    if (def.graphName === undefined || def.graphName === null || def.graphName === '') {
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
      def.heightRatio = 0.4;
    }

    // Determine the target dimensions
    const dim = getDimensionsForElement(el, def);

    // RenderGraph based on graphingEngine used.
    const renderGraph = () =>  {
      if (graphingEngine === 'placeholder') {
        drawPlaceholderGraph(el, def, dim);
        didDrawOneOrMorePlaceholders = true;
      } else if (graphingEngine === 'backshift') {
        drawBackshiftGraph(el, def, dim);
      } else {
        drawPngGraph(el, def, dim);
      }
      didGraphRendered = true;
      console.log('Rendered graph ' + def.graphName); // eslint-disable-line no-console
      // Notify other components (i.e cropper) that we have loaded a graph
      $j(document).trigger('graphLoaded', [dim.width, dim.height]);
    };

    // Calculate if div is in viewport and render if it is.
    const renderGraphWhenInView = () => {
      let scrollTop = $(window).scrollTop();
      let windowBottom = scrollTop + $(window).height();
      let offsetTop = el.offset().top;
      let offsetBottom = offsetTop + el.height();
      let divIsHidden = el.parent().hasClass('ng-hide');
      if ((scrollTop <= offsetBottom && windowBottom >= offsetTop) && !didGraphRendered && !divIsHidden) {
        renderGraph();
      }
    }

    // Render immediately if no scrolling possible or first two graphs
    if ($(document).height() <= $(window).height() || index <= 1) {
      renderGraph();
    }
    // Check window scroll and lazy load graphs that are in view and not hidden by 'ng-hide'
    $(window).scroll(function () {
      renderGraphWhenInView();
    });

    // Also render graphs that are in viewport when renderGraph is triggered.
    el.on('renderGraph', function (event) { 
      renderGraphWhenInView();
      event.stopPropagation();
    });

    // If print is triggered, render graph if div is not hidden.
    (function () {
      var beforePrint = function () {
        let divIsHidden = el.parent().hasClass('ng-hide');
        if (!didGraphRendered && !divIsHidden) {
          renderGraph();
        }
      };
      if (window.matchMedia) {
        var mediaQueryList = window.matchMedia('print');
        mediaQueryList.addListener(function (mql) {
          if (mql.matches) {
            beforePrint();
          }
        });
      }
      window.onbeforeprint = beforePrint;
    }());

  });

  if (didDrawOneOrMorePlaceholders) {
    holder.run({ images: '.graph-placeholder' });
  }

};

// Automatically trigger a render on load
$j(() => {
  render();
});

const GraphContainers = {
  render: render
};

console.log('init: onms-graph'); // eslint-disable-line no-console

module.exports = GraphContainers;
window.GraphContainers = GraphContainers;
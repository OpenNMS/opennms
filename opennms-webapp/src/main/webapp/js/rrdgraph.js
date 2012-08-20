/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 ******************************************************************************/

var RRDGraph = window['RRDGraph'] = {};

/*******************************************************************************
 * Data/config grabber
 ******************************************************************************/
(function () {
  var get = RRDGraph['get'] = {};

  get['config'] = function (src) { // {resourceId, report}
    // TODO: decide how to get this from the server
    return '';
  };

  get['data'] = function () { // {resourceId, report, start, end}
    // TODO: decide how to get this from the server
    return {};
  };
})();

/*******************************************************************************
 * The config
 ******************************************************************************/

(function () {
  var tokenize = function (str) {
    var delined = str.replace(/\\\n/g, ' ');

    var regex = /[^\s"]*["]+?[^"]+?["]+?[^\s"']*|[^\s"]+/g;
    var tokens = [];
    var match;
    while (match = regex.exec(delined)) {
      tokens.push(match[0].replace(/"/g, ''));
    }

    return tokens;
  };

  var regex = {
    def: /^[CV]?DEF:/,
    graph: /^PRINT|^GPRINT|^COMMENT|^VRULE|^HRULE|^LINE|^AREA|^TICK|^SHIFT|^TEXTALIGN|^STACK/,
    print: /^[G]?PRINT/,
    line: /^LINE/,
    split: /\\%%SPLIT%%/g,
    colon: /:/g
  };

  var short_tags = {
    's': 'start', 'e': 'end', 'S': 'step', 't': 'title', 'v': 'vertical-label',
    'w': 'width', 'h': 'height', 'j': 'only-graph', 'D': 'full-size-mode',
    'u': 'upper-limit', 'l': 'lower-limit', 'r': 'rigid', 'A': 'alt-autoscale',
    'J': 'alt-autoscale-min', 'M': 'alt-autoscale-max', 'N': 'no-gridfit',
    'x': 'x-grid', 'y': 'y-grid', 'Y': 'alt-y-grid', 'o': 'logarithmic',
    'X': 'units-exponent', 'L': 'units-length', 'g': 'no-legend',
    'F': 'force-rules-legend', 'c': 'color', 'm': 'zoom', 'n': 'font',
    'P': 'pango-markup', 'G': 'graph-render-mode', 'E': 'slope-mode',
    'T': 'tabwidth', 'b': 'base', 'W': 'watermark'
  };

  var cast_options = function (options, cast, option_names) {
    for (var o in option_names) {
      var option = option_names[o];
      options[option] = cast(options[option]);
    }
  };

  var parse = function (config_string) {
    var result = {
      options: {
        'start'             : +new Date() - 1200000,
        'end'               : +new Date(),
        'step'              : 1,
        'title'             : '',
        'vertical-label'    : '',
        'width'             : 400,
        'height'            : 100,
        'full-size-mode'    : false,
        'only-graph'        : false,
        'upper-limit'       : null,
        'lower-limit'       : null,
        'rigid'             : false,
        'alt-autoscale'     : false,
        'alt-autoscale-min' : false,
        'alt-autoscale-max' : false,
        'no-gridfit'        : true,
        'x-grid'            : '', // TODO: reasonable default
        'y-grid'            : '', // TODO: reasonable default
        'alt-y-grid'        : false,
        'logarithmic'       : false,
        'units-exponent'    : 0, // TODO: reasonable default
        'units-length'      : 0,
        'units'             : 'si',
        'right-axis'        : '1:1', // scale:shift
        'right-axis-label'  : '',
        'right-axis-format' : '',
        'no-legend'         : false,
        'force-rules-legend': false,
        'legend-position'   : 'south',
        'legend-direction'  : 'topdown',
        'color'             : '', // TODO: reasonable default
        'grid-dash'         : '1:1',
        'border'            : 2,
        'dynamic-labels'    : false,
        'zoom'              : 1,
        'font'              : {
          'title' : {
            'family': 'monospace',
            'size': 13
          },
          'axis' : {
            'family': 'monospace',
            'size': 10
          },
          'unit' : {
            'family': 'monospace',
            'size': 10
          },
          'legend' : {
            'family': 'monospace',
            'size': 11
          },
          'watermark' : {
            'family': 'monospace',
            'size': 8
          }
        },
        'pango-markup'      : false,
        'graph-render-mode' : 'normal',
        'slope-mode'        : false,
        'tabwidth'          : 40,
        'base'              : 1000,
        'watermark'         : '',
        /* RRDGRAPH-js only options */
        'compress'          : true
      },
      defs: {
        data: {},
        value: {},
        calc: {}
      },
      graphs: []
    };

    var tokens = tokenize(config_string);

    for (var t = 0, l = tokens.length; t < l; ++t) {
      var token = tokens[t];

      if (regex.def.test(token)) {
        var subtokens = token.split(':');
        
        var name;
        var value;
        var type;

        if (token.charAt(0) === 'D') { // DEF
          type = 'data';
          value = {};

          var name_rrd = subtokens[1].split('=');
          name = name_rrd[0];
          value.rrd = name_rrd[1];
          value.ds_name = subtokens[2];
          value.cf = subtokens[3];

          for (var st = 4, sl = subtokens.length; st < sl; ++st) {
            var st_kv = subtokens[st].split('=');
            value[st_kv[0]] = st_kv[1];
          }
        } else {
          type = (token.charAt(0) === 'C') ? 'calc' : 'value';

          var split_at = token.indexOf('=');
          name = token.slice(5, split_at);
          value = token.slice(split_at + 1);
        }

        result.defs[type][name] = value;
      } else if (token.charAt(0) === '-' && token.length > 1) {
        var n_dashes = (token.charAt(1) === '-') ? 2 : 1;
        var option = token.slice(n_dashes);
        
        var value = true;
        var key_value = option.split('=');

        if (key_value.length > 1) {
          option = key_value[0];
          value = key_value[1];
        } else if (tokens[t + 1].charAt(0) !== '-' && !regex.def.test(tokens[t + 1])) {
          value = tokens[++t];
        }

        if (option in short_tags) option = short_tags[option];

        if (option === 'font') {
          var font = value.split(':');

          var size = parseInt(font[1]);
          if (size) {
            result.options[option][font[0].toLowerCase()].size = size;
          }
          
          if (font[2]) {
            result.options[option][font[0].toLowerCase()].family = font[2];
          }
        } else {
          result.options[option] = value;
        }
      } else if (regex.graph.test(token)) {
        token = token.replace(regex.colon, '%%SPLIT%%');
        token = token.replace(regex.split, ':');

        var subtokens = token.split('%%SPLIT%%');
        var element = {
          type: null
        };

        if (regex.print.test(subtokens[0])) {
          element.type = subtokens[0].toLowerCase();

          element.vname = subtokens[1];
          if (subtokens.length === 3) { // newer
            element.format = subtokens[2];
          } else { // deprecated type
            element.format = subtokens[3];
            element.cf = subtokens[2];
          }
        } else if (subtokens[0] === 'COMMENT') {
          element.type = 'comment';

          element.text = subtokens[1];
        } else if (subtokens[0] === 'SHIFT') {
          element.type = 'shift';

          element.vname = subtokens[1];
          element.offset = subtokens[2];
        } else if (subtokens[0] === 'TEXTALIGN') {
          element.type = 'textalign';
          element.textalign = subtokens[1];
        } else if (subtokens[0] === 'TICK') {
          element.type = 'tick';

          var vname_color = subtokens[1].split('#');
          element.value = vname_color[0];
          element.color = vname_color[1];
          
          if (subtokens.length > 3) {
            element.legend = subtokens[3];
          }

          element.fraction = 0.1;

          if (subtokens.length >= 3) {
            element.fraction = +subtokens[2];
          }
        } else { // vrule, hrule, line or area
          if (regex.line.test(subtokens[0])) {
            element.type = 'line';

            if (subtokens[0].length > 4) {
              element.width = parseFloat(subtokens[0].slice(4));
            } else {
              element.width = 1;
            }
          } else {
            element.type = subtokens[0].toLowerCase();
          }

          var key_color = subtokens[1].split('#');
          if (element.type === 'vrule') {
            element.time = new Date(+key_color[0]);
          } else { // hrule, line, area
            element.value = key_color[0];
          }

          if (key_color.length === 2) {
            element.color = key_color[1];
          }

          if (subtokens.length == 3 && subtokens[2] === 'STACK') {
            element.stack = true;
          } else if (subtokens.length >= 3) { // legend
            element.legend = subtokens[2];

            for (var st = 3, sl = subtokens.length; st < sl; ++st) {
              if (subtokens[st] === 'STACK') {
                element.stack = true;
              } else { // dashes or dash-offset
                var key_value = subtokens[st].split('=');
                if (key_value[0] === 'dashes') {
                  if (key_value.length === 2) {
                    element.dashes = key_value[1].replace(',', ' ');
                  } else {
                    element.dashes = '5 5';
                  }
                } else { // dash-offset
                  element.dash_offset = subtokens[st].split('=')[1];
                }
              }
            }
          }
        }

        if ('color' in element) { // extract opacity
          if (element.color.length === 6) { // just color, default opacity
            element.color = '#' + element.color;
            element.opacity = 1.0;
          } else {
            var opacity_hex = parseInt(element.color.slice(6), 16);
            element.color = '#' + element.color.slice(0, 6);
            element.opacity = opacity_hex / 255.0;
          }
        }

        // convert deprecated STACK into a stacked AREA
        if (element.type === 'stack') {
          element.type = 'area';
          element.stack = true;
        }

        if (element.type !== null) {
          result.graphs.push(element);
        }
      }
    }

    cast_options(result.options, parseInt, [
      'step', 'width', 'height', 'units-exponent', 'units-length', 'border',
      'tabwidth', 'base', 'start', 'end'
    ]);

    cast_options(result.options, parseFloat, ['upper-limit', 'lower-limit', 'zoom']);

    result.options['grid-dash'] = result.options['grid-dash'].replace(':', ' ');
    return result;
  };

  legend = [
    {content: [], align: 'left'}
  ];

  var NO_FOLLOWING_SPACES = '\\n\\l\\r\\c\\j\\g\\u';
  var splitElementString = function (s) {
    var at = 0;
    var result = [];

    while (s.length) {
      var next_slash = s.indexOf('\\', at);
      if (next_slash !== -1) {
        if (s.charAt(next_slash + 1) !== '\\') {
          result.push(s.slice(0, next_slash));
          result.push(s.slice(next_slash, next_slash + 2));
          s = s.slice(next_slash + 2);
          at = 0;
        } else {
          at = next_slash + 2;
        }
      } else {
        result.push(s);
        break;
      }
    }
    
    // two spaces after every element unless \g
    var last = result.length - 1;
    if (NO_FOLLOWING_SPACES.indexOf(result[last]) === -1) {
      result[last] = result[last] + '  ';
    }


    return result;
  };

  var LINE_ENDS = '\\n\\l\\r\\c\\j';
  var ALIGN = {
    '\\n': 'left',
    '\\l': 'left',
    '\\r': 'right',
    '\\c': 'center',
    '\\j': 'justify'
  };
  var PRINT_FORMAT = /%(\d*)[.]?(\d*)l([ef]{1})[%]?([sS]?)/;

  var createLegendTemplate = function (config) {
    config.legend = [];

    var next_line = {content: [], align: 'left', position: 0};
    var graphs = config.graphs;

    for (var i = 0, len = graphs.length; i < len; ++i) {
      var g = graphs[i];
      if (g.type === 'comment') {
        var parts = splitElementString(g.text);
        while (parts.length) {
          var part = parts.shift();
          if (LINE_ENDS.indexOf(part) !== -1) {
            next_line.align = ALIGN[part];
            config.legend.push(next_line);
            next_line = {content: [], align: 'left', position: next_line.position + 1};
          } else if (part === '\\u') {
            next_line.position--;
          } else {
            next_line.content.push(part);
          }
        }
      } else if (g.type === 'line' || g.type === 'area' || g.type === 'tick' || 
                 g.type === 'hrule' || g.type === 'vrule') {
        if (g.color && g.legend) {
          next_line.content.push({type: 'box', color: g.color});

          var parts = splitElementString(g.legend);
          while (parts.length) {
            var part = parts.shift();
            if (LINE_ENDS.indexOf(part) !== -1) {
              next_line.align = ALIGN[part];
              config.legend.push(next_line);
              next_line = {content: [], align: 'left', position: next_line.position + 1};
            } else if (part === '\\u') {
              next_line.position--;
            } else {
              next_line.content.push(part);
            }
          }
        }
      } else if (g.type === 'gprint') {
        var vname = g.vname;
        if (!(vname in config.defs.value)) { // DEF or CDEF: old style
          // we need to define an internal VDEF
          var cf = g.cf;
          cf = cf.replace('MAX', 'MAXIMUM');
          cf = cf.replace('MIN', 'MINIMUM');

          vname = '_internal_' + g.vname + '_' + cf;
          config.defs.value[vname] = g.vname + ',' + cf;
        }

        var parts = splitElementString(g.format);
        while (parts.length) {
          var part = parts.shift();
          if (LINE_ENDS.indexOf(part) !== -1) {
            next_line.align = ALIGN[part];
            config.legend.push(next_line);
            next_line = {content: [], align: 'left', position: next_line.position + 1};
          } else if (part === '\\u') {
            next_line.position--;
          } else {
            part = part.replace(' %s', '%s');
            part = part.replace(' %S', '%S');
            
            var r = PRINT_FORMAT.exec(part);
            if (r !== null) {
              if (r.index > 0) {
                var text = part.slice(0, r.index);
                text = text.replace('%%', '%');
                next_line.content.push(text);
              }
              
              var next_var = {vname: vname};
              if (r[1] !== "") {
                next_var.width = +r[1];
              }
              if (r[2] !== "") {
                next_var.decimals = +r[2];
              }
              if (r[3] === 'e') {
                next_var.type = 'exponential';
              } else {
                next_var.type = 'float';
              }
              if (r[4] === 's') {
                next_var.si = true;
              } else if (r[4] === 'S') {
                next_var.si = 'same';
              }
              next_line.content.push(next_var);

              var end = r.index + r[0].length;
              if (end < part.length - 1) {
                var text = part.slice(end);
                text = text.replace('%%', '%');
                next_line.content.push(text);
              }
            } else {
              throw "bad format for GPRINT in '" + g.format + "'";
            }
          }
        }
      }
    }
    if (next_line.content.length) {
      next_line.align = 'left';
      config.legend.push(next_line);
      next_line = {content: [], align: 'left', position: next_line.position + 1};
    }
  };

  RRDGraph.Config = function (src, config_string) {
    if (src) {
      config_string = RRDGraph.get.config(src);
    }
    var config = parse(config_string);

    this.options = config.options;
    this.defs = config.defs;
    this.graphs = config.graphs;

    createLegendTemplate(this);
  };
})();

/*******************************************************************************
 * The data class
 ******************************************************************************/
(function () {
  var Data = RRDGraph.Data = function (src, config) {
    this.src = src;
    this.config = config;

    this.listeners = [];

    this.setup();
  };

  Data.prototype.setup = function () {
    this.data = {
      arrays: {}, // DEFS and CDEFS
      values: {} // VDEFS
    };
    this.affects_extremes = {};

    this.extremes = {
      x: {min: Number.POSITIVE_INFINITY, max: Number.NEGATIVE_INFINITY},
      y: {min: Number.POSITIVE_INFINITY, max: Number.NEGATIVE_INFINITY}
    };

    for (var def in this.config.defs.data) {
      this.data.arrays[def] = [];
    }
    for (var cdef in this.config.defs.calc) {
      this.data.arrays[cdef] = [];
    }
    for (var vdef in this.config.defs.value) {
      this.data.values[vdef] = {t: NaN, v: NaN};
    }

    // defs affect extremes only if they are used in a LINE or AREA
    // TODO: STACKed values
    for (var vname in this.data.arrays) {
      for (var g in this.config.graphs) {
        var graph = this.config.graphs[g];
        if (graph.value === vname && (graph.type === 'line' || graph.type === 'area') &&
           !graph.stack) {
          this.affects_extremes[vname] = true;
        }
      }
    }
  };

  Data.prototype.addListener = function (graph) {
    this.listeners.push(graph);
  };

  Data.prototype.notifyUpdate = function () {
    for (var l = 0, len = this.listeners.length; l < len; ++l) {
      this.listeners[l].update();
    }
  };

  /* RPN --------------------------------------------------------------------*/
  var operation = {
    'LT': function (a, b) { return +(a < b); },
    'GT': function (a, b) { return +(a > b); },
    'LE': function (a, b) { return +(a <= b); },
    'GE': function (a, b) { return +(a >= b); },
    'EQ': function (a, b) { return +(a === b); },
    'NE': function (a, b) { return +(a !== b); },
    'UN': function (a) { return +(isNaN(a)); },
    'ISINF': function (a) { return +(a == Number.POSITIVE_INFINITY || a == Number.NEGATIVE_INFINITY); },

    'MIN': function (a, b) {
      if (isNaN(a) || isNaN(a)) {
        return NaN;
      } if (a > b) {
        return b;
      } else {
        return a;
      }
    },
    'MAX': function (a, b) {
      if (isNaN(a) || isNaN(a)) {
        return NaN;
      } if (a > b) {
        return a;
      } else {
        return b;
      }
    },
    
    '+': function (a, b) { return a + b; },
    '*': function (a, b) { return a * b; },
    '-': function (a, b) { return a - b; },
    '/': function (a, b) { return a / b; },
    '%': function (a, b) { return a % b; },
    'ADDNAN': function (a, b) {
      var a_is_nan = isNaN(a);
      var b_is_nan = isNaN(b);

      if (!a_is_nan && b_is_nan) {
        return a;
      } else if (a_is_nan && !b_is_nan) {
        return b;
      } else if (a_is_nan && b_is_nan) {
        return NaN;
      } else {
        return a + b;
      }
    },
    'SIN': function (a) { return Math.sin(a); },
    'COS': function (a) { return Math.cos(a); },
    'LOG': function (a) { return Math.log(a); },
    'EXP': function (a) { return Math.exp(a); },
    'SQRT': function (a) { return Math.sqrt(a); },
    'ATAN': function (a) { return Math.atan(a); },
    'ATAN2': function (a, b) { return Math.atan2(a, b); },
    'FLOOR': function (a) { return Math.floor(a); },
    'CEIL': function (a) { return Math.ceil(a); },
    'DEG2RAD': function (a) { return a * Math.PI / 180.0; },
    'RAD2DEG': function (a) { return a * 180 / Math.PI; },
    'ABS': function (a) { return Math.abs(a); },



    'IF': function (stack) {
      var c = stack.pop();
      var b = stack.pop();
      var a = stack.pop();
      var a_is_number = !(a instanceof Array);
      var b_is_number = !(b instanceof Array);
      var c_is_number = !(c instanceof Array);

      if (a_is_number) {
        if (a !== 0) {
          stack.push(b);
        } else {
          stack.push(c);
        }
      } else {
        var result = [];
        if (b_is_number && c_is_number) {
          for (var i = 0, l = a.length; i < l; ++i) {
            result[i] = {
              t: a[i].t,
              v: (a[i].v !== 0) ? b : c
            };
          }
        } else if (b_is_number && !c_is_number) {
          for (var i = 0, l = a.length; i < l; ++i) {
            result[i] = {
              t: a[i].t,
              v: (a[i].v !== 0) ? b : c[i].v
            };
          }
        } else if (!b_is_number && c_is_number) {
          for (var i = 0, l = a.length; i < l; ++i) {
            result[i] = {
              t: a[i].t,
              v: (a[i].v !== 0) ? b[i].v : c
            };
          }
        } else {
          for (var i = 0, l = a.length; i < l; ++i) {
            result[i] = {
              t: a[i].t,
              v: (a[i].v !== 0) ? b[i].v : c[i].v
            };
          }
        }

        stack.push(result);
      }
    },

    'LIMIT': function (stack) {
      var c = stack.pop();
      var b = stack.pop();
      var a = stack.pop();
      var a_is_number = !(a instanceof Array);
      var b_is_number = !(b instanceof Array);
      var c_is_number = !(c instanceof Array);

      // TODO: b and c being (C)DEF, not numbers
      if (a_is_number && a >= b && a <= c) {
        stack.push(a);
      } else {
        var result = [];
        for (var i = 0, l = a.length; i < l; ++i) {
          result[i] = {
            t: a[i].t,
            v: (a[i].v >= b && a[i].v <= c) ? a[i].v : NaN
          };
        }

        stack.push(result);
      }
    }
  };

  var operation_template = {
    commutativeBinary: function (stack, operator) {
      var b = stack.pop();
      var a = stack.pop();
      var a_is_number = !(a instanceof Array);
      var b_is_number = !(b instanceof Array);
      var op = operation[operator];

      if (a_is_number && b_is_number) {
        stack.push(op(a, b));
      } else if (!a_is_number && !b_is_number) {
        if (a.length !== b.length) {
          throw 'DEFs have differing number of elements';
          // TODO: this may not be an error, not sure yet
        }
        
        var result = [];
        for (var i = 0, l = a.length; i < l; ++i) {
          result[i] = {
            t: a[i].t,
            v: op(a[i].v, b[i].v)
          };
        }

        stack.push(result);
      } else {
        if (a_is_number) {
          var temp = a;
          a = b;
          b = temp;
        }

        var result = [];
        for (var i = 0, l = a.length; i < l; ++i) {
          result[i] = {
            t: a[i].t,
            v: op(a[i].v, b)
          };
        }

        stack.push(result);
      }
    },

    nonCommutativeBinary: function (stack, operator) {
      var b = stack.pop();
      var a = stack.pop();
      var a_is_number = !(a instanceof Array);
      var b_is_number = !(b instanceof Array);
      var op = operation[operator];

      if (a_is_number && b_is_number) {
        stack.push(op(a, b));
      } else if (!a_is_number && !b_is_number) {
        if (a.length !== b.length) {
          throw 'DEFs have differing number of elements';
        }

        var result = [];
        for (var i = 0, l = a.length; i < l; ++i) {
          result[i] = {
            t: a[i].t,
            v: op(a[i].v, b[i].v)
          };
        }

        stack.push(result);
      } else {
        var result = [];
        if (b_is_number) {
          for (var i = 0, l = a.length; i < l; ++i) {
            result[i] = {
              t: a[i].t,
              v: op(a[i].v, b)
            };
          }
        } else { // a is number
          for (var i = 0, l = b.length; i < l; ++i) {
            result[i] = {
              t: b[i].t,
              v: op(a, b[i].v)
            };
          }
        }

        stack.push(result);
      }
    },

    unary: function (stack, operator) {
      var a = stack.pop();
      var a_is_number = !(a instanceof Array);
      var op = operation[operator];

      if (a_is_number) {
        stack.push(0);
      } else {
        var result = [];
        for (var i = 0, l = a.length; i < l; ++i) {
          result[i] = {
            t: a[i].t,
            v: op(a[i].v)
          };
        }

        stack.push(result);
      }
    }
  };

  var RPN = {
    'LT': function (stack) {
      operation_template.nonCommutativeBinary(stack, 'LT');
    },
    'LE': function (stack) {
      operation_template.nonCommutativeBinary(stack, 'LE');
    },
    'GT': function (stack) {
      operation_template.nonCommutativeBinary(stack, 'GT');
    },
    'GE': function (stack) {
      operation_template.nonCommutativeBinary(stack, 'GE');
    },
    'EQ': function (stack) {
      operation_template.commutativeBinary(stack, 'EQ');
    },
    'NE': function (stack) {
      operation_template.commutativeBinary(stack, 'NE');
    },
    'UN': function (stack) {
      operation_template.unary(stack, 'UN');
    },
    'ISINF': function (stack) {
      operation_template.unary(stack, 'ISINF');
    },
    'IF': function (stack) {
      operation['IF'](stack);
    },

    'MIN': function (stack) {
      operation_template.commutativeBinary(stack, 'MIN');
    },
    'MAX': function (stack) {
      operation_template.commutativeBinary(stack, 'MAX');
    },
    'LIMIT': function (stack) {
      operation['LIMIT'](stack);
    },

    '+': function (stack) {
      operation_template.commutativeBinary(stack, '+');
    },
    '*': function (stack) {
      operation_template.commutativeBinary(stack, '*');
    },
    '-': function (stack) {
      operation_template.nonCommutativeBinary(stack, '-');
    },
    '/': function (stack) {
      operation_template.nonCommutativeBinary(stack, '/');
    },
    '%': function (stack) {
      operation_template.nonCommutativeBinary(stack, '%');
    },
    'ADDNAN': function (stack) {
      operation_template.commutativeBinary(stack, 'ADDNAN');
    },
    'SIN': function (stack) {
      operation_template.unary(stack, 'SIN');
    },
    'COS': function (stack) {
      operation_template.unary(stack, 'COS');
    },
    'LOG': function (stack) {
      operation_template.unary(stack, 'LOG');
    },
    'EXP': function (stack) {
      operation_template.unary(stack, 'EXP');
    },
    'SQRT': function (stack) {
      operation_template.unary(stack, 'SQRT');
    },
    'ATAN': function (stack) {
      operation_template.unary(stack, 'ATAN');
    },
    'ATAN2': function (stack) {
      operation_template.nonCommutativeBinary(stack, 'ATAN2');
    },
    'FLOOR': function (stack) {
      operation_template.unary(stack, 'FLOOR');
    },
    'CEIL': function (stack) {
      operation_template.unary(stack, 'CEIL');
    },
    'DEG2RAD': function (stack) {
      operation_template.unary(stack, 'DEG2RAD');
    },
    'RAD2DEG': function (stack) {
      operation_template.unary(stack, 'RAD2DEG');
    },
    'ABS': function (stack) {
      operation_template.unary(stack, 'ABS');
    },

    'UNKN': function (stack) {
      stack.push(NaN);
    },
    'INF': function (stack) {
      stack.push(Number.POSITIVE_INFINITY);
    },
    'NEGINF': function (stack) {
      stack.push(Number.NEGATIVE_INFINITY);
    },
    'COUNT': function (stack) {
      var a = stack.pop();
      var a_is_number = !(a instanceof Array);
      if (a_is_number) {
        return 1;
      } else {
        var result = [];
        for (var i = 0, l = a.length; i < l; ++i) {
          result.push({
            t: a[i].t,
            v: i + 1
          });
        }
        stack.push(result);
      }
    },

    'DUP': function (stack) {
      stack.push(stack.slice(-1)[0]);
    },
    'POP': function (stack) {
      stack.pop();
    },
    'EXC': function (stack) {
      var b = stack.pop();
      var a = stack.pop();
      stack.push(b);
      stack.push(a);
    }
  };

  var VDEF_RPN = {
    'MAXIMUM': function (array) {
      if (array.length == 0) {
        return {t: 0, v: Number.NaN};
      }

      var result = array[0];
      for (var i = 1, len = array.length; i < len; ++i) {
        var value = array[i];
        if (value.v > result.v) {
          result = value;
        }
      }
      return result;
    },
    'MINIMUM': function (array) {
      if (array.length == 0) {
        return {t: 0, v: Number.NaN};
      }

      var result = array[0];
      for (var i = 1, len = array.length; i < len; ++i) {
        var value = array[i];
        if (value.v < result.v) {
          result = value;
        }
      }
      return result;
    },
    'AVERAGE': function (array) {
      var result = 0;
      for (var i = 0, len = array.length; i < len; ++i) {
        var value = array[i];
        result += value.v;
      }
      return {t: 0, v: result / array.length};
    },
    'STDEV': function (array) {
      // one pass calculation
      // http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm
      var m = 0;
      var m2 = 0;
      var len = array.length;

      for (var i = 0; i < len; ++i) {
        var v = array[i].v;
        var d = v - m;
        m += d / (i + 1);
        m2 += d * (v - m);
      }

      return {t: 0, v: Math.sqrt(m2 / (len - 1))};
    },
    'LAST': function (array) {
      return array.slice(-1)[0];
    },
    'FIRST': function (array) {
      return array[0];
    },
    'TOTAL': function (array) {
      var result = 0;
      for (var i = 0, len = array.length; i < len; ++i) {
        var value = array[i];
        result += value.v;
      }
      var time = (array.slice(-1)[0].t - array[0].t) / 1000;
      return {t: time , v: result};
    }
  };

  Data.prototype.push = function (data) {
    this.extremes.x.min = Number.POSITIVE_INFINITY;
    this.extremes.x.max = Number.NEGATIVE_INFINITY;
    this.extremes.y.min = Number.POSITIVE_INFINITY;
    this.extremes.y.max = Number.NEGATIVE_INFINITY;

    var temp_defs = {};
    var n_defs = 0;
    for (var el in data) {
      var element = data[el];
      var defs = [];
      for (var d in this.config.defs.data) {
        if (d === element.def) {
          defs.push(d);
        }
      }

      n_defs = defs.length;

      for (var d = 0; d < defs.length; ++d) {
        temp_defs[defs[d]] = [];
        for (var p = 0, len = element.points.length; p < len; ++p) {
          this.data.arrays[defs[d]].push(element.points[p]);
          temp_defs[defs[d]].push(element.points[p]);
        }
      }
    }

    // No points
    if (!n_defs) {
      return;
    }

    var temp_cdefs = {};

    for (var cdef in this.config.defs.calc) {
      var rpn = this.config.defs.calc[cdef].split(',');
      var stack = [];

      while (rpn.length > 0) {
        var element = rpn.shift();
        if (isNaN(element)) {
          if (element in temp_defs) { // DEF
            stack.push(temp_defs[element]);
          } else if (element in temp_cdefs) { // CDEF
            stack.push(temp_cdefs[element]);
          } else { // operator
            RPN[element](stack);
          }
        } else { // number
          stack.push(+element);
        }
      }

      if (stack.length > 1 || !(stack[0] instanceof Array)) {
        throw 'Bad RPN in CDEF named ' + cdef;
      }

      temp_cdefs[cdef] = stack[0];
    }

    for (var cdef in temp_cdefs) {
      var result = this.data.arrays[cdef];
      result.push.apply(result, temp_cdefs[cdef]);
    }

    for (var vdef in this.config.defs.value) {
      var expression = this.config.defs.value[vdef].split(',');
      var array = this.data.arrays[expression[0]];
      if (array === undefined) {
        throw 'Bad vname "' + expression[0] + '" in VDEF named ' + vdef;
      }
      var result = {t: NaN, v: NaN};

      if (expression.length === 3) {
        if (expression[2] !== 'PERCENT' || expression[2] !== 'PERCENTNAN') {
          throw 'Bad operation "' + expression[2] + '" in VDEF named ' + vdef;
        }
        var percent = +expression[1];
        if (isNaN(percent) || percent < 0 || percent > 100) {
          throw 'Bad percentage "' + expression[1] + '" in VDEF named ' + vdef;
        }
        
        result = VDEF_RPN[expression[2]](array, percent);
      } else if (expression.length === 2) {
        result = VDEF_RPN[expression[1]](array);
      } else {
        throw 'Bad expression in VDEF named ' + vdef;
      }

      this.data.values[vdef] = result;
    }

    for (var d in this.data.arrays) {
      var def = this.data.arrays[d];

      // defs affect extremes only if they are used in a LINE or AREA
      if (!(d in this.affects_extremes)) {
        continue;
      }

      // Value extremes, remove NaNs
      for (var p = 0, len = def.length; p < len; ++p) {
        if (isNaN(def[p].v)) {
          continue;
        }
        if (def[p].v > this.extremes.y.max) this.extremes.y.max = def[p].v;
        if (def[p].v < this.extremes.y.min) this.extremes.y.min = def[p].v;
      }

      // Time extremes
      if (def.length > 0) {
        var period = this.config.options.end - this.config.options.start;

        if (def[def.length - 1].t > this.extremes.x.max) this.extremes.x.max = def[def.length - 1].t;
        if (this.config.options.compress) {
          if (def[0].t < this.extremes.x.min) this.extremes.x.min = def[0].t;
        } else {
          this.extremes.x.min = this.extremes.x.max - period;
        }
      }
    }

    this.notifyUpdate();
  };
})();


/*******************************************************************************
 * The graph class
 ******************************************************************************/

(function () {
  var Graph = RRDGraph.Graph = function (element, config) {
    this.element = d3.select(element);
    this.config = config;

    this.svg = {};

    this.createStatics();
    this.adjustDimensionsAndPositions();
    this.defineDynamics();
  };

  Graph.prototype.createStatics = function () {
    var container = this.svg.container = this.element.append('svg:svg').
      attr('shape-rendering', 'crispEdges').
      attr('xml:space', 'preserve').
      style('background-color', '#f3f3f3').
      style('border-right', this.config.options.border + 'px solid #999').
      style('border-bottom', this.config.options.border + 'px solid #999').
      style('border-top', this.config.options.border + 'px solid #ccc').
      style('border-left', this.config.options.border + 'px solid #ccc');

    this.canvas_padding = {
      x: this.config.options.font.unit.size + 50,
      y: this.config.options.font.title.size + 20
    };

    var canvas = this.svg.canvas = container.append('svg:g').
      attr('transform', 
           'translate(' + this.canvas_padding.x + ',' + this.canvas_padding.y + ')');

    this.svg.bottom_layer = canvas.append('svg:g').attr('class', 'bottom-layer');
    this.svg.middle_layer = canvas.append('svg:g').attr('class', 'middle-layer');
    this.svg.top_layer = canvas.append('svg:g').attr('class', 'top-layer');
    this.svg.hover_layer = canvas.append('svg:g').attr('class', 'hover-layer');

    this.svg.clip = container.append('svg:clipPath').
      attr('id', 'clip').
      append('svg:rect');

    this.svg.canvas_bg = this.svg.bottom_layer.append('svg:rect').
      attr('fill', '#fff');

    this.svg.canvas_hover = this.svg.hover_layer.append('svg:rect').
      attr('fill-opacity', 0);

    this.tooltip = this.element.append('div').
      style('position', 'absolute').
      style('background-color', 'white').
      style('border-right', this.config.options.border + 'px solid #999').
      style('border-bottom', this.config.options.border + 'px solid #999').
      style('border-top', this.config.options.border + 'px solid #ccc').
      style('border-left', this.config.options.border + 'px solid #ccc').
      style('padding', '3px').
      style('text-align', 'center').
      style('visibility', 'hidden').
      style('font-size', this.config.options.font.legend.size + 'px').
      style('font-family', this.config.options.font.legend.family).
      style('z-index', '20');

    var _this = this;
    var formatTime = d3.time.format('%c');
    var formatValue = d3.format(' .1f');
    this.svg.canvas_hover.on('mousemove', function () {
      var e = d3.event;
      var x = e.offsetX - _this.canvas_padding.x;
      var y = e.offsetY - _this.canvas_padding.y;

      var time = formatTime(_this.scales.x.invert(x));
      var value = formatValue(_this.scales.y.invert(y));
      _this.tooltip.
        style('visibility', 'visible').
        style('top', (e.pageY + 8) + 'px').
        style('left', (e.pageX + 8) + 'px').
        html(time + '<br/>' + 'Value: ' + value);
    });
    
    this.svg.canvas_hover.on('mouseout', function () {
      _this.tooltip.
        style('visibility', 'hidden');
    });

    this.svg.title = container.append('svg:text').
      attr('text-anchor', 'middle').
      text(this.config.options.title).
      style('font-size', this.config.options.font.title.size + 'px').
      style('font-family', this.config.options.font.title.family);

    this.svg.v_label = container.append('svg:text').
      style('font-size', this.config.options.font.unit.size + 'px').
      style('font-family', this.config.options.font.unit.family).
      attr('transform', 'rotate(270)').
      text(this.config.options['vertical-label']);

    this.svg.logo = container.append('svg:text').
      attr('fill', '#999').
      style('font', '9px monospace').
      attr('transform', 'rotate(90)').
      text('RRDGRAPH-JS / D3.JS');

    if (this.config.options.watermark !== '') {
      this.svg.watermark = container.append('svg:text').
        attr('fill', '#aaa').
        attr('text-anchor', 'middle').
        text(this.config.options.watermark).
        style('font-size', this.config.options.font.watermark.size + 'px').
        style('font-family', this.config.options.font.watermark.family);
    }

    var compress_toggle_text = this.config.options.compress ? 'COMPRESS' : 'FIXED';
    var _this = this;
    this.svg.compress_toggle = container.append('svg:text').
      style('font', '9px monospace').
      text(compress_toggle_text).
      style('cursor', 'pointer').
      on('click', function () {
        _this.config.options.compress ^= 1; // flip it
        var compress_toggle_text = _this.config.options.compress ? 'COMPRESS' : 'FIXED';
        _this.svg.compress_toggle.text(compress_toggle_text);
      });
  };

  Graph.prototype.defineDynamics = function () {
    var scales = this.scales = {
      y: d3.scale.linear().rangeRound([this.config.options.height, 0]),
      x: d3.time.scale().rangeRound([0, this.config.options.width])
    };

    this.graph_elements = [];
    var _this = this;

    for (var g = 0, len = this.config.graphs.length; g < len; ++g) {
      var e = this.config.graphs[g];
      if (e.type === 'line' || e.type === 'area' || e.type === 'tick') {
        var svg_type = e.type === 'line' ? 'line' : 'area';
        var shape = this.graph_elements[g] = d3.svg[svg_type]().
          x(function (d) { return scales.x(new Date(d.t)) }).
          interpolate('linear');

        if (e.type === 'line') {
          shape.defined(function (d) { return !isNaN(d.v); });
          if (e.stack) {
            shape.y(function (d) { return scales.y(d.v + _this.previous_array[d.t]) });
          } else {
            shape.y(function (d) { return scales.y(d.v) });
          }
        } else if (e.type === 'area') {
          shape.defined(function (d) { return !isNaN(d.v); });
          if (e.stack) {
            shape.y1(function (d) { return scales.y(d.v + _this.previous_array[d.t]) });
            shape.y0(function (d) { return scales.y(_this.previous_array[d.t]) });
          } else {
            shape.y1(function (d) { return scales.y(d.v) });
            shape.y0(function (d) { return scales.y(0) });
          }
        } else if (e.type === 'tick') {
          shape.defined(function (d) { return !isNaN(d.v) && d.v != 0.0; });
          var fraction = e.fraction;
          if (fraction >= 0.0) {
            shape.y0(function (d) { 
              return scales.y(scales.y.domain()[0]);
            });
            shape.y1(function (d) {
              var domain = scales.y.domain();
              return scales.y(domain[0] + (domain[1] - domain[0]) * fraction);
            });
          } else {
            shape.y0(function (d) {
              var domain = scales.y.domain();
              return scales.y(domain[0] + (domain[1] - domain[0]) * (1.0 + fraction));
            });
            shape.y1(function (d) {
              return scales.y(scales.y.domain()[1]);
            });
          }
        }

        if (e.type === 'tick' || e.color) {
          this.svg.middle_layer.selectAll('path.' + e.type + '-' + g).
            data([[]]).enter().append('svg:path').
            attr('d', shape).
            attr('class', e.type + '-' + g).
            attr('fill', (e.type === 'line' ? 'none' : e.color)).
            attr('fill-opacity', e.opacity).
            attr('stroke', e.color).
            attr('stroke-width', (e.type === 'line' ? e.width : 0)).
            attr('stroke-opacity', e.opacity).
            attr('clip-path', 'url(#clip)').
            attr('stroke-dasharray', e.dashes).
            attr('shape-rendering', 'auto');
        }
      } else if (e.type === 'hrule' || e.type === 'vrule') {
        this.svg.middle_layer.append('svg:line').
          attr('class', e.type + '-' + g).
          attr('fill', 'none').
          attr('stroke', e.color).
          attr('stroke-width', 1).
          attr('clip-path', 'url(#clip)').
          attr('stroke-dasharray', e.dashes).
          attr('shape-rendering', 'auto');
      }
    }

    // TODO: axes, grid ?? what did I mean to do here? :D
  };

  var ALIGN_TO_ANCHOR = {
    'left': 'start',
    'center': 'middle',
    'right': 'end',
    'justify': 'start'
  };

  Graph.prototype.createLegend = function () { // TODO: justify
    var y = this.canvas_padding.y + this.config.options.height + 
      this.config.options.font.axis.size + 5;
    var line_height = this.config.options.font.legend.size + 4;

    var legend_container = this.svg.container.append('svg:g').
      attr('transform', 'translate(0,' + y + ')');

    var legend = [];
    var createLine = function (n) {
    };

    for (var i = 0; i < this.config.legend.length; ++i) {
      var line = this.config.legend[i];
      if (!(line.position in legend)) {
        legend[line.position] = legend_container.append('svg:g').
          attr('xml:space', 'preserve').
          attr('transform', 'translate(15,' + (line_height * (line.position + 1)) + ')');
      }

      var text = legend[line.position].append('svg:text').
        attr('fill', '#000').
        attr('text-anchor', ALIGN_TO_ANCHOR[line.align]).
        attr('width', this.svg.container.attr('width') - 30).
        style('text-rendering', 'geometricPrecision').
        style('font-size', this.config.options.font.legend.size + 'px').
        style('font-family', this.config.options.font.legend.family);

      for (var j = 0; j < line.content.length; ++j) {
        var element = line.content[j];
        if (typeof element === 'string') {
          text.append('svg:tspan').text(element).
            attr('xml:space', 'preserve');
        } else {
          if (element.type === 'box') {
            text.append('svg:tspan').text("\u25A0 ").
            //text.append('svg:tspan').text("\u2B1B ").
              attr('fill', element.color).
              attr('stroke', '#000').
              attr('xml:space', 'preserve').
              style('font-size', (+this.config.options.font.legend.size + 4) + 'px').
              attr('stroke-width', '0.75').
              attr('stroke-linecap', 'square');
          } else if (element.type === 'float' || element.type === 'exponential') {
            text.append('svg:tspan').
              attr('xml:space', 'preserve').
              attr('id', 'tspan_' + i + '_' + j);
          }
        }
      }
    }

    var total_line_height = (legend.length) * line_height;

    return {y: y, height: total_line_height};
  };

  Graph.prototype.adjustDimensionsAndPositions = function () {
    /* TODO: full-size-mode and only-graph are not supported (yet) */

    var dims = {
      width: this.config.options.width,
      height: this.config.options.height
    };

    this.svg.canvas_bg.
      attr('width', dims.width).
      attr('height', dims.height);

    this.svg.canvas_hover.
      attr('width', dims.width).
      attr('height', dims.height);

    this.svg.clip.
      attr('width', dims.width).
      attr('height', dims.height);

    this.svg.container.
      attr('width', dims.width + this.canvas_padding.x + 30); // TODO: right-axis padding

    this.svg.title.
      attr('x', this.svg.container.attr('width') / 2).
      attr('y', this.config.options.font.title.size + 5);

    this.svg.v_label.
      attr('x', -dims.height / 2 - this.canvas_padding.y).
      attr('y', this.config.options.font.unit.size + 2);

    this.svg.logo.
      attr('x', 2).
      attr('y', 9 - this.svg.container.attr('width'));

    this.svg.compress_toggle.
      attr('x', 1).
      attr('y', 10);

    var legend;
    if (!this.config.options['no-legend']) {
      /* The legend is created here because it needs to know the dimensions
       * of the container and the canvas */
      var legend = this.createLegend();
    } else {
      legend = {
        y: this.canvas_padding.y + this.config.options.height + 
          this.config.options.font.axis.size + 10,
        h: 0
      };
    }

    var container_height = legend.y + legend.height + 8;

    if (this.svg.watermark !== undefined) {
      var watermark_y = legend.y + legend.height + 
        this.config.options.font.watermark.size + 5;
      this.svg.watermark.
        attr('x', this.svg.container.attr('width') / 2).
        attr('y', watermark_y);
      container_height += this.config.options.font.watermark.size + 5;
    }

    this.svg.container.
      attr('height', container_height);
  };

  Graph.prototype.bind = function (data) {
    this.data = data;
    data.addListener(this);
  };

  Graph.prototype.update = function () {
    var y_min = Math.floor(this.data.extremes.y.min);
    var y_max = Math.floor(this.data.extremes.y.max);

    var conf_y_max = this.config.options['upper-limit'];
    var conf_y_min = this.config.options['lower-limit'];
    var rigid = this.config.options['rigid'];
    
    if (!isNaN(conf_y_max) && (y_max < conf_y_max || rigid)) {
      y_max = conf_y_max;
    }
    if (!isNaN(conf_y_min) && (y_min > conf_y_min || rigid)) {
      y_min = conf_y_min;
    }


    this.scales.y.
      domain([y_min, y_max]);

    this.scales.x.
        domain([this.data.extremes.x.min, this.data.extremes.x.max]);

    
    var dashes = this.config.options['grid-dash'];
    var width = this.svg.canvas_bg.attr('width');
    var height = this.svg.canvas_bg.attr('height');

    this.svg.canvas.selectAll('g.grid').remove();

    // Horizontal Grid, TODO: color
    var h_ticks = this.scales.y.ticks(6).length;
    var hgrid_minor = this.svg.bottom_layer.selectAll('g.hgrid-minor').
      data(this.scales.y.ticks(h_ticks * 5)).
      enter().append('svg:g').
      attr('stroke', '#ddd').
      attr('class', 'grid hgrid-minor').
      attr('stroke-dasharray', dashes);
    
    hgrid_minor.append('svg:line').
      attr('x1', 0).
      attr('x2', width).
      attr('y1', this.scales.y).
      attr('y2', this.scales.y);
    
    var hgrid_major = this.svg.top_layer.selectAll('g.hgrid-major').
      data(this.scales.y.ticks(h_ticks)).
      enter().append('svg:g').
      attr('stroke', '#f00').
      attr('stroke-opacity', '0.2').
      attr('class', 'grid hgrid-major').
      attr('stroke-dasharray', dashes);

    hgrid_major.append('svg:line').
      attr('x1', 0).
      attr('x2', width).
      attr('y1', this.scales.y).
      attr('y2', this.scales.y);

    // Vertical Grid, TODO: color
    var tick = {vgrid: {}, xaxis: {}};
    var timespan = this.data.extremes.x.max - this.data.extremes.x.min;
    if (timespan > 2419200000) { // > month, year view
      tick.vgrid.min_type = d3.time.months;
      tick.vgrid.min_count = 1;
      tick.vgrid.maj_type = d3.time.months;
      tick.vgrid.maj_count = 1;
      tick.xaxis.type = d3.time.months;
      tick.xaxis.count = 1;
      tick.xaxis.sub = 0;
      tick.xaxis.format = d3.time.format('%b');
    } else if (timespan > 604800000) { // up to 1 month
      tick.vgrid.min_type = d3.time.hours;
      tick.vgrid.min_count = 12;
      tick.vgrid.maj_type = d3.time.days;
      tick.vgrid.maj_count = 1;
      tick.xaxis.type = d3.time.weeks;
      tick.xaxis.count = 1;
      tick.xaxis.sub = 7;
      tick.xaxis.format = d3.time.format('Week %W');
    } else if (timespan > 86400000 * 4) { // up to 7 days
      tick.vgrid.min_type = d3.time.hours;
      tick.vgrid.min_count = 6;
      tick.vgrid.maj_type = d3.time.days;
      tick.vgrid.maj_count = 1;
      tick.xaxis.type = d3.time.days;
      tick.xaxis.count = 1;
      tick.xaxis.sub = 4;
      tick.xaxis.format = d3.time.format('%a');
    } else if (timespan > 86400000 * 2) { // up to 4 days
      tick.vgrid.min_type = d3.time.hours;
      tick.vgrid.min_count = 3;
      tick.vgrid.maj_type = d3.time.hours;
      tick.vgrid.maj_count = 12;
      tick.xaxis.type = d3.time.hours;
      tick.xaxis.count = 12;
      tick.xaxis.sub = 3;
      tick.xaxis.format = d3.time.format('%a %H:%M');
    } else if (timespan > 86400000) { // up to 2 days
      tick.vgrid.min_type = d3.time.hours;
      tick.vgrid.min_count = 1;
      tick.vgrid.maj_type = d3.time.hours;
      tick.vgrid.maj_count = 4;
      tick.xaxis.type = d3.time.hours;
      tick.xaxis.count = 8;
      tick.xaxis.sub = 4;
      tick.xaxis.format = d3.time.format('%a %H:%M');
    } else if (timespan > 43200000) { // up to 1 day
      tick.vgrid.min_type = d3.time.minutes;
      tick.vgrid.min_count = 30;
      tick.vgrid.maj_type = d3.time.hours;
      tick.vgrid.maj_count = 2;
      tick.xaxis.type = d3.time.hours;
      tick.xaxis.count = 4;
      tick.xaxis.sub = 3;
      tick.xaxis.format = d3.time.format('%a %H:%M');
    } else if (timespan > 21600000) { // up to 12 hours
      tick.vgrid.min_type = d3.time.minutes;
      tick.vgrid.min_count = 30;
      tick.vgrid.maj_type = d3.time.hours;
      tick.vgrid.maj_count = 2;
      tick.xaxis.type = d3.time.hours;
      tick.xaxis.count = 2;
      tick.xaxis.sub = 3;
      tick.xaxis.format = d3.time.format('%a %H:%M');
    } else if (timespan > 7200000) { // up to 6 hours
      tick.vgrid.min_type = d3.time.minutes;
      tick.vgrid.min_count = 10;
      tick.vgrid.maj_type = d3.time.minutes;
      tick.vgrid.maj_count = 30;
      tick.xaxis.type = d3.time.hours;
      tick.xaxis.count = 1;
      tick.xaxis.sub = 6;
      tick.xaxis.format = d3.time.format('%a %H:%M');
    } else if (timespan > 1800000) { // up to 2 hours
      tick.vgrid.min_type = d3.time.minutes;
      tick.vgrid.min_count = 2;
      tick.vgrid.maj_type = d3.time.minutes;
      tick.vgrid.maj_count = 10;
      tick.xaxis.type = d3.time.minutes;
      tick.xaxis.count = 20;
      tick.xaxis.sub = 6;
      tick.xaxis.format = d3.time.format('%a %H:%M');
    } else if (timespan > 600000) { // up to 30 minutes
      tick.vgrid.min_type = d3.time.minutes;
      tick.vgrid.min_count = 1;
      tick.vgrid.maj_type = d3.time.minutes;
      tick.vgrid.maj_count = 5;
      tick.xaxis.type = d3.time.minutes;
      tick.xaxis.count = 5;
      tick.xaxis.sub = 5;
      tick.xaxis.format = d3.time.format('%H:%M:%S');
    } else if (timespan > 300000) { // up to 10 minutes
      tick.vgrid.min_type = d3.time.seconds;
      tick.vgrid.min_count = 20;
      tick.vgrid.maj_type = d3.time.minutes;
      tick.vgrid.maj_count = 1;
      tick.xaxis.type = d3.time.minutes;
      tick.xaxis.count = 2;
      tick.xaxis.sub = 5;
      tick.xaxis.format = d3.time.format('%H:%M:%S');
    } else if (timespan > 60000) { // up to 5 minutes
      tick.vgrid.min_type = d3.time.seconds;
      tick.vgrid.min_count = 10;
      tick.vgrid.maj_type = d3.time.seconds;
      tick.vgrid.maj_count = 30;
      tick.xaxis.type = d3.time.minutes;
      tick.xaxis.count = 1;
      tick.xaxis.sub = 5;
      tick.xaxis.format = d3.time.format('%H:%M:%S');
    } else if (timespan > 30000) { // up to 1 minute
      tick.vgrid.min_type = d3.time.seconds;
      tick.vgrid.min_count = 1;
      tick.vgrid.maj_type = d3.time.seconds;
      tick.vgrid.maj_count = 5;
      tick.xaxis.type = d3.time.seconds;
      tick.xaxis.count = 10;
      tick.xaxis.sub = 5;
      tick.xaxis.format = d3.time.format('%H:%M:%S');
    } else if (timespan > 5000) { // up to 30 seconds
      tick.vgrid.min_type = d3.time.seconds;
      tick.vgrid.min_count = 1;
      tick.vgrid.maj_type = d3.time.seconds;
      tick.vgrid.maj_count = 5;
      tick.xaxis.type = d3.time.seconds;
      tick.xaxis.count = 5;
      tick.xaxis.sub = 5;
      tick.xaxis.format = d3.time.format('%H:%M:%S');
    } else { // up to 5 seconds
      tick.vgrid.min_type = d3.time.seconds;
      tick.vgrid.min_count = 1;
      tick.vgrid.maj_type = d3.time.seconds;
      tick.vgrid.maj_count = 1;
      tick.xaxis.type = d3.time.seconds;
      tick.xaxis.count = 1;
      tick.xaxis.sub = 5;
      tick.xaxis.format = d3.time.format('%H:%M:%S');
    }

    var vgrid_minor = this.svg.bottom_layer.selectAll('g.vgrid-minor').
      data(this.scales.x.ticks(tick.vgrid.min_type, tick.vgrid.min_count)).
      enter().append('svg:g').
      attr('stroke', '#ddd').
      attr('class', 'grid hgrid-minor').
      attr('stroke-dasharray', dashes);

    vgrid_minor.append('svg:line').
      attr('x1', this.scales.x).
      attr('x2', this.scales.x).
      attr('y1', 0).
      attr('y2', height);

    var vgrid_major = this.svg.top_layer.selectAll('g.vgrid-major').
      data(this.scales.x.ticks(tick.vgrid.maj_type, tick.vgrid.maj_count)).
      enter().append('svg:g').
      attr('stroke', '#f00').
      attr('class', 'grid hgrid-major').
      attr('stroke-opacity', '0.2').
      attr('stroke-dasharray', dashes);

    vgrid_major.append('svg:line').
      attr('x1', this.scales.x).
      attr('x2', this.scales.x).
      attr('y1', 0).
      attr('y2', height);

    // Axes, TODO: color
    this.svg.canvas.selectAll('g.axis').remove();

    var yAxis = d3.svg.axis().scale(this.scales.y).
      orient('left').
      tickFormat(d3.format(' s')).
      ticks(6).tickSize(2);

    this.svg.canvas.append('svg:g').
      attr('class', 'y axis').
      call(yAxis);

    var xAxis = d3.svg.axis().scale(this.scales.x).orient('bottom').
      ticks(tick.xaxis.type, tick.xaxis.count).tickSubdivide(tick.xaxis.sub).
      tickSize(2, 1, 1).tickFormat(tick.xaxis.format);

    this.svg.canvas.append('svg:g').
      attr('class', 'x axis').
      attr('transform', 'translate(0,' + height + ')').
      call(xAxis);
    
    this.svg.canvas.selectAll('.axis line, .axis path').
      style('fill', 'none').
      style('stroke', '#000');

    this.svg.canvas.selectAll('.axis text').
      style('font-size', this.config.options.font.axis.size + 'px').
      style('font-family', this.config.options.font.axis.family);


    for (var g = 0, len = this.config.graphs.length; g < len; ++g) {
      var e = this.config.graphs[g];
      if (e.type === 'line' || e.type === 'area' || e.type === 'tick') {
        var shape = this.graph_elements[g];

        if (e.type !== 'tick') {
          var next = this.config.graphs[g + 1];
          if (next && next.stack) {
            this.previous_array = {};
            var array = this.data.data.arrays[e.value];
            for (var element = 0, l = array.length; element < l; ++element) {
              this.previous_array[array[element].t] = array[element].v;
            }
          }
        }

        if (e.type === 'tick' || e.color) {
          this.svg.middle_layer.selectAll('path.' + e.type + '-' + g).
            data([this.data.data.arrays[e.value]]).
            attr('d', shape);
        }
      } else if (e.type === 'hrule') {
        var y = 0.5 + this.scales.y(this.data.data.values[e.value].v);
        this.svg.middle_layer.selectAll('line.' + e.type + '-' + g).
          attr('x1', 0).
          attr('y1', y).
          attr('x2', this.scales.x(this.scales.x.domain()[1])).
          attr('y2', y);
      } else if (e.type === 'vrule') {
        var x = 0.5 + this.scales.x(e.time);
        this.svg.middle_layer.selectAll('line.' + e.type + '-' + g).
          attr('x1', x).
          attr('y1', 0).
          attr('x2', x).
          attr('y2', this.scales.y(this.scales.y.domain()[0]));
      }
    }

    for (var i = 0; i < this.config.legend.length; ++i) {
      var line = this.config.legend[i];
      for (var j = 0; j < line.content.length; ++j) {
        var element = line.content[j];
        if (typeof element === 'object') {
          if (element.type === 'float' || element.type === 'exponential') {
            var format = ' ';
            format += element.width;
            format += '.';
            format += element.decimals;
            if (element.si) {
              format += 's';
            } else if (element.type === 'float') {
              format += 'f';
            } else {
              format += 'e';
            }

            var formatter = d3.format(format);
            var value = this.data.data.values[element.vname].v;
            var text = formatter(value);
            while (text.length < element.width) {
              text = ' ' + text;
            }
            this.svg.container.select('#tspan_' + i + '_' + j).
              text(text);
          }
        }
      }
    }

  };


})();

/*******************************************************************************
 * Main functions
 ******************************************************************************/

(function () {
  var parseSrc = function (src) {
    var parts = src.split('&');
    var kv = {};
    for (var p in parts) {
      var key_value = parts[p].split('=');
      kv[key_value[0]] = key_value[1];
    }
    return kv;
  };

  window['RRDGraph']['init'] = function (selector, config_str) {
    var result = [];
    d3.selectAll(selector).each(function () {
      var src = parseSrc(this.dataset.src);
      var element = this;

      var config;

      if (src.realtime) {
        config = new RRDGraph.Config(null, config_str);
      } else {
        config = new RRDGraph.Config(src);
      }

      var graph = new RRDGraph.Graph(element, config);
      var data = new RRDGraph.Data(src, config);

      graph.bind(data);

      if (!src.realtime) {
        data.push(RRDGraph.get.data(src));
      }

      result.push({
        config: config,
        graph: graph,
        data: data
      });
    });

    return result;
  };

})();

/*******************************************************************************
 * Realtime data collector
 ******************************************************************************/
(function () {
  var DataCollector = window['RRDGraph']['DataCollector'] = function (config, data, mappings) {
    this.config = config;
    this.data = data;
    this.setRate(5);

    this.setupMappings(mappings);
    this.setupStorage();
  };

  DataCollector.prototype.setupMappings = function (mappings) {
    this.mappings = {};
    for (var m in mappings) {
      var mapping = mappings[m];
      var rrd_ds_name = mapping.split(':');
      this.mappings[m] = {
        rrd: rrd_ds_name[0],
        ds_name: rrd_ds_name[1]
      };
    }
  };

  DataCollector.prototype.setupStorage = function () {
    this.storage = {};
    for (var d in this.config.defs.data) {
      var def = this.config.defs.data[d];
      if (!(def.rrd in this.storage)) {
        this.storage[def.rrd] = {};
      }
      this.storage[def.rrd][def.ds_name] = [];
    }
  };

  DataCollector.prototype.setRate = function (rate) {
    this.rate = rate;
    if (rate > 500) {
      this.measurements = 1;
    } else if (rate > 200) {
      this.measurements = 2;
    } else if (rate > 100) {
      this.measurements = 5;
    } else {
      this.measurements = 10;
    }
  };

  DataCollector.prototype.push = function (metrics) {
    for (var m = 0; m < metrics.length; ++m) {
      var metric = metrics[m];
      var mapping = this.mappings[metric.metricId];
      if (mapping && mapping.rrd in this.storage && 
          mapping.ds_name in this.storage[mapping.rrd]) {
        this.storage[mapping.rrd][mapping.ds_name].push({
          t: metric.timeStamp, v: metric.value
        });
      }
    }


    this.consolidate();
  };

  var CONSOLIDATE = {
    AVERAGE: function (data) {
      var result = {
        t: data.slice(-1)[0].t
      };

      var sum = 0;
      for (var d = 0; d < data.length; ++d) {
        sum += data[d].v;
      }
      result.v = sum / data.length;

      return result;
    },
    MIN: function (data) {
      var result = {
        t: data.slice(-1)[0].t,
        v: Number.POSITIVE_INFINITY
      };

      for (var d = 0; d < data.length; ++d) {
        if (data[d].v < result.v) {
          result.v = data[d].v;
        }
      }

      return result;
    },
    MAX: function (data) {
      var result = {
        t: data.slice(-1)[0].t,
        v: Number.NEGATIVE_INFINITY
      };

      for (var d = 0; d < data.length; ++d) {
        if (data[d].v > result.v) {
          result.v = data[d].v;
        }
      }

      return result;
    },
    LAST: function (data) {
      return data.slice(-1)[0];
    }
  };

  DataCollector.prototype.consolidate = function () {
    // Check if we have enough measurements for all DEFs
    for (var d in this.config.defs.data) {
      var def = this.config.defs.data[d];
      if (this.storage[def.rrd][def.ds_name].length < this.measurements) {
        return;
      }
    }
    
    var result = [];
    var max_t = 0;

    // Consolidate all DEFs
    for (var d in this.config.defs.data) {
      var def = this.config.defs.data[d];
      var data = 
        this.storage[def.rrd][def.ds_name].slice(0, this.measurements);
      var consolidated = CONSOLIDATE[def.cf](data);

      if (max_t < consolidated.t) {
        max_t = consolidated.t;
      }

      result.push({
        def: d,
        points: [consolidated]
      });
    }

    // Set the same time for all defs
    for (var r = 0; r < result.length; ++r) {
      result[r].points[0].t = max_t;
    }

    // Remove consolidated measurements
    for (var rrd in this.storage) {
      for (var ds_name in this.storage[rrd]) {
        this.storage[rrd][ds_name].splice(0, this.measurements);
      }
    }

    // Pass result to data for graphing
    this.data.push(result);
  };
})();

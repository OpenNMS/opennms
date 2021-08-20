'use strict';

var utils = require('primevue/utils');
var Ripple = require('primevue/ripple');
var vue = require('vue');

function _interopDefaultLegacy (e) { return e && typeof e === 'object' && 'default' in e ? e : { 'default': e }; }

var Ripple__default = /*#__PURE__*/_interopDefaultLegacy(Ripple);

var script = {
    name: 'Fieldset',
    emits: ['update:collapsed', 'toggle'],
    props: {
        legend: String,
        toggleable: Boolean,
        collapsed: Boolean
    },
    data() {
        return {
           d_collapsed: this.collapsed
        }
    },
    watch: {
        collapsed(newValue) {
            this.d_collapsed = newValue;
        }
    },
    methods: {
        toggle(event) {
            this.d_collapsed = !this.d_collapsed;
            this.$emit('update:collapsed', this.d_collapsed);
            this.$emit('toggle', {
                originalEvent: event,
                value: this.d_collapsed
            });
        }
    },
	computed: {
		iconClass() {
			return ['p-fieldset-toggler pi ', {
				'pi-minus': !this.d_collapsed,
				'pi-plus': this.d_collapsed
			}]
        },
        ariaId() {
            return utils.UniqueComponentId();
        }
    },
    directives: {
        'ripple': Ripple__default['default']
    }
};

const _hoisted_1 = { class: "p-fieldset-legend" };
const _hoisted_2 = { class: "p-fieldset-legend-text" };
const _hoisted_3 = { class: "p-fieldset-content" };

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _directive_ripple = vue.resolveDirective("ripple");

  return (vue.openBlock(), vue.createBlock("fieldset", {
    class: ['p-fieldset p-component', {'p-fieldset-toggleable': $props.toggleable}]
  }, [
    vue.createVNode("legend", _hoisted_1, [
      (!$props.toggleable)
        ? vue.renderSlot(_ctx.$slots, "legend", { key: 0 }, () => [
            vue.createVNode("span", {
              class: "p-fieldset-legend-text",
              id: $options.ariaId + '_header'
            }, vue.toDisplayString($props.legend), 9, ["id"])
          ])
        : vue.createCommentVNode("", true),
      ($props.toggleable)
        ? vue.withDirectives((vue.openBlock(), vue.createBlock("a", {
            key: 1,
            tabindex: "0",
            onClick: _cache[1] || (_cache[1] = (...args) => ($options.toggle && $options.toggle(...args))),
            onKeydown: _cache[2] || (_cache[2] = vue.withKeys((...args) => ($options.toggle && $options.toggle(...args)), ["enter"])),
            id: $options.ariaId +  '_header',
            "aria-controls": $options.ariaId + '_content',
            "aria-expanded": !$data.d_collapsed
          }, [
            vue.createVNode("span", { class: $options.iconClass }, null, 2),
            vue.renderSlot(_ctx.$slots, "legend", {}, () => [
              vue.createVNode("span", _hoisted_2, vue.toDisplayString($props.legend), 1)
            ])
          ], 40, ["id", "aria-controls", "aria-expanded"])), [
            [_directive_ripple]
          ])
        : vue.createCommentVNode("", true)
    ]),
    vue.createVNode(vue.Transition, { name: "p-toggleable-content" }, {
      default: vue.withCtx(() => [
        vue.withDirectives(vue.createVNode("div", {
          class: "p-toggleable-content",
          role: "region",
          id: $options.ariaId + '_content',
          "aria-labelledby": $options.ariaId + '_header'
        }, [
          vue.createVNode("div", _hoisted_3, [
            vue.renderSlot(_ctx.$slots, "default")
          ])
        ], 8, ["id", "aria-labelledby"]), [
          [vue.vShow, !$data.d_collapsed]
        ])
      ]),
      _: 3
    })
  ], 2))
}

function styleInject(css, ref) {
  if ( ref === void 0 ) ref = {};
  var insertAt = ref.insertAt;

  if (!css || typeof document === 'undefined') { return; }

  var head = document.head || document.getElementsByTagName('head')[0];
  var style = document.createElement('style');
  style.type = 'text/css';

  if (insertAt === 'top') {
    if (head.firstChild) {
      head.insertBefore(style, head.firstChild);
    } else {
      head.appendChild(style);
    }
  } else {
    head.appendChild(style);
  }

  if (style.styleSheet) {
    style.styleSheet.cssText = css;
  } else {
    style.appendChild(document.createTextNode(css));
  }
}

var css_248z = "\n.p-fieldset-legend > a,\n.p-fieldset-legend > span {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    -webkit-box-pack: center;\n        -ms-flex-pack: center;\n            justify-content: center;\n}\n.p-fieldset-toggleable .p-fieldset-legend a {\n    cursor: pointer;\n    -webkit-user-select: none;\n       -moz-user-select: none;\n        -ms-user-select: none;\n            user-select: none;\n    overflow: hidden;\n    position: relative;\n    text-decoration: none;\n}\n.p-fieldset-legend-text {\n    line-height: 1;\n}\n";
styleInject(css_248z);

script.render = render;

module.exports = script;

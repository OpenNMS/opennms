'use strict';

var Button = require('primevue/button');
var vue = require('vue');

function _interopDefaultLegacy (e) { return e && typeof e === 'object' && 'default' in e ? e : { 'default': e }; }

var Button__default = /*#__PURE__*/_interopDefaultLegacy(Button);

var script = {
    name: 'Inplace',
    emits: ['open', 'close', 'update:active'],
    props: {
        closable: {
            type: Boolean,
            default: false
        },
        active: {
            type: Boolean,
            default: false
        }
    },
    watch: {
        active(newValue) {
            this.d_active = newValue;
        }
    },
    data() {
        return {
            d_active: this.active
        }
    },
    methods: {
        open(event) {
            this.$emit('open', event);
            this.d_active = true;
            this.$emit('update:active', true);
        },
        close(event) {
            this.$emit('close', event);
            this.d_active = false;
            this.$emit('update:active', false);
        }
    },
    computed: {
        containerClass() {
            return ['p-inplace p-component', {'p-inplace-closable': this.closable}];
        }
    },
    components: {
        'IPButton': Button__default['default']
    }
};

const _hoisted_1 = {
  key: 1,
  class: "p-inplace-content"
};

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_IPButton = vue.resolveComponent("IPButton");

  return (vue.openBlock(), vue.createBlock("div", { class: $options.containerClass }, [
    (!$data.d_active)
      ? (vue.openBlock(), vue.createBlock("div", {
          key: 0,
          class: "p-inplace-display",
          tabindex: _ctx.$attrs.tabindex||'0',
          onClick: _cache[1] || (_cache[1] = (...args) => ($options.open && $options.open(...args))),
          onKeydown: _cache[2] || (_cache[2] = vue.withKeys((...args) => ($options.open && $options.open(...args)), ["enter"]))
        }, [
          vue.renderSlot(_ctx.$slots, "display")
        ], 40, ["tabindex"]))
      : (vue.openBlock(), vue.createBlock("div", _hoisted_1, [
          vue.renderSlot(_ctx.$slots, "content"),
          ($props.closable)
            ? (vue.openBlock(), vue.createBlock(_component_IPButton, {
                key: 0,
                icon: "pi pi-times",
                onClick: $options.close
              }, null, 8, ["onClick"]))
            : vue.createCommentVNode("", true)
        ]))
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

var css_248z = "\n.p-inplace .p-inplace-display {\n    display: inline;\n    cursor: pointer;\n}\n.p-inplace .p-inplace-content {\n    display: inline;\n}\n.p-fluid .p-inplace.p-inplace-closable .p-inplace-content {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n}\n.p-fluid .p-inplace.p-inplace-closable .p-inplace-content > .p-inputtext {\n    -webkit-box-flex: 1;\n        -ms-flex: 1 1 auto;\n            flex: 1 1 auto;\n    width: 1%;\n}\n";
styleInject(css_248z);

script.render = render;

module.exports = script;

'use strict';

var utils = require('primevue/utils');
var Ripple = require('primevue/ripple');
var vue = require('vue');

function _interopDefaultLegacy (e) { return e && typeof e === 'object' && 'default' in e ? e : { 'default': e }; }

var Ripple__default = /*#__PURE__*/_interopDefaultLegacy(Ripple);

var script = {
    name: 'TabView',
    emits: ['update:activeIndex', 'tab-change', 'tab-click'],
    props: {
        activeIndex: {
            type: Number,
            default: 0
        },
        lazy: {
            type: Boolean,
            default: false
        }
    },
    data() {
        return {
            d_activeIndex: this.activeIndex
        }
    },
    watch: {
        activeIndex(newValue) {
            this.d_activeIndex = newValue;
        }
    },
    updated() {
        this.updateInkBar();
    },
    mounted() {
        this.updateInkBar();
    },
    methods: {
        onTabClick(event, i) {
            if (!this.isTabDisabled(this.tabs[i]) && i !== this.d_activeIndex) {
                this.d_activeIndex = i;
                this.$emit('update:activeIndex', this.d_activeIndex);

                this.$emit('tab-change', {
                    originalEvent: event,
                    index: i
                });
            }

            this.$emit('tab-click', {
                originalEvent: event,
                index: i
            });
        },
        onTabKeydown(event, i) {
            if (event.which === 13) {
                this.onTabClick(event, i);
            }
        },
        updateInkBar() {
            let tabHeader = this.$refs.nav.children[this.d_activeIndex];
            this.$refs.inkbar.style.width = utils.DomHandler.getWidth(tabHeader) + 'px';
            this.$refs.inkbar.style.left =  utils.DomHandler.getOffset(tabHeader).left - utils.DomHandler.getOffset(this.$refs.nav).left + 'px';
        },
        getKey(tab, i) {
            return (tab.props && tab.props.header) ? tab.props.header : i;
        },
        isTabDisabled(tab) {
            return (tab.props && tab.props.disabled);
        },
        isTabPanel(child) {
            return child.type.name === 'TabPanel'
        }
    },
    computed: {
        tabs() {
            const tabs = [];
            this.$slots.default().forEach(child => {
                    if (this.isTabPanel(child)) {
                        tabs.push(child);
                    }
                    else if (child.children && child.children instanceof Array) {
                        child.children.forEach(nestedChild => {
                            if (this.isTabPanel(nestedChild)) {
                                tabs.push(nestedChild);
                            }
                        });
                    }
                }
            );
            return tabs;
        },
    },
    directives: {
        'ripple': Ripple__default['default']
    }
};

const _hoisted_1 = { class: "p-tabview p-component" };
const _hoisted_2 = {
  ref: "nav",
  class: "p-tabview-nav",
  role: "tablist"
};
const _hoisted_3 = {
  key: 0,
  class: "p-tabview-title"
};
const _hoisted_4 = {
  ref: "inkbar",
  class: "p-tabview-ink-bar"
};
const _hoisted_5 = { class: "p-tabview-panels" };
const _hoisted_6 = {
  key: 0,
  class: "p-tabview-panel",
  role: "tabpanel"
};

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _directive_ripple = vue.resolveDirective("ripple");

  return (vue.openBlock(), vue.createBlock("div", _hoisted_1, [
    vue.createVNode("ul", _hoisted_2, [
      (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList($options.tabs, (tab, i) => {
        return (vue.openBlock(), vue.createBlock("li", {
          role: "presentation",
          key: $options.getKey(tab,i),
          class: [{'p-highlight': ($data.d_activeIndex === i), 'p-disabled': $options.isTabDisabled(tab)}]
        }, [
          vue.withDirectives(vue.createVNode("a", {
            role: "tab",
            class: "p-tabview-nav-link",
            onClick: $event => ($options.onTabClick($event, i)),
            onKeydown: $event => ($options.onTabKeydown($event, i)),
            tabindex: $options.isTabDisabled(tab) ? null : '0',
            "aria-selected": $data.d_activeIndex === i
          }, [
            (tab.props && tab.props.header)
              ? (vue.openBlock(), vue.createBlock("span", _hoisted_3, vue.toDisplayString(tab.props.header), 1))
              : vue.createCommentVNode("", true),
            (tab.children && tab.children.header)
              ? (vue.openBlock(), vue.createBlock(vue.resolveDynamicComponent(tab.children.header), { key: 1 }))
              : vue.createCommentVNode("", true)
          ], 40, ["onClick", "onKeydown", "tabindex", "aria-selected"]), [
            [_directive_ripple]
          ])
        ], 2))
      }), 128)),
      vue.createVNode("li", _hoisted_4, null, 512)
    ], 512),
    vue.createVNode("div", _hoisted_5, [
      (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList($options.tabs, (tab, i) => {
        return (vue.openBlock(), vue.createBlock(vue.Fragment, {
          key: $options.getKey(tab,i)
        }, [
          ($props.lazy ? ($data.d_activeIndex === i) : true)
            ? vue.withDirectives((vue.openBlock(), vue.createBlock("div", _hoisted_6, [
                (vue.openBlock(), vue.createBlock(vue.resolveDynamicComponent(tab)))
              ], 512)), [
                [vue.vShow, $props.lazy ? true: ($data.d_activeIndex === i)]
              ])
            : vue.createCommentVNode("", true)
        ], 64))
      }), 128))
    ])
  ]))
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

var css_248z = "\n.p-tabview-nav {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    margin: 0;\n    padding: 0;\n    list-style-type: none;\n    -ms-flex-wrap: wrap;\n        flex-wrap: wrap;\n}\n.p-tabview-nav-link {\n    cursor: pointer;\n    -webkit-user-select: none;\n       -moz-user-select: none;\n        -ms-user-select: none;\n            user-select: none;\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    position: relative;\n    text-decoration: none;\n    overflow: hidden;\n}\n.p-tabview-ink-bar {\n    display: none;\n    z-index: 1;\n}\n.p-tabview-nav-link:focus {\n    z-index: 1;\n}\n.p-tabview-title {\n    line-height: 1;\n}\n";
styleInject(css_248z);

script.render = render;

module.exports = script;

this.primevue = this.primevue || {};
this.primevue.steps = (function (utils, vue) {
    'use strict';

    var script = {
        name: 'Steps',
        props: {
            id: {
                type: String,
                default: utils.UniqueComponentId()
            },
    		model: {
                type: Array,
                default: null
            },
            readonly: {
                type: Boolean,
                default: true
            }
        },
        methods: {
            onItemClick(event, item, navigate) {
                if (item.disabled || this.readonly) {
                    event.preventDefault();
                    return;
                }

                if (item.command) {
                    item.command({
                        originalEvent: event,
                        item: item
                    });
                }

                if (item.to && navigate) {
                    navigate(event);
                }
            },
            isActive(item) {
                return this.activeRoute === item.to || this.activeRoute === item.to + '/' ;
            },
            getItemClass(item) {
                return ['p-steps-item', item.class, {
                    'p-highlight p-steps-current': this.isActive(item),
                    'p-disabled': this.isItemDisabled(item)
                }];
            },
            isItemDisabled(item) {
                return (item.disabled || (this.readonly && !this.isActive(item)));
            },
            visible(item) {
                return (typeof item.visible === 'function' ? item.visible() : item.visible !== false);
            }
        },
        computed: {
            activeRoute() {
                return this.$route.path;
            },
            containerClass() {
                return ['p-steps p-component', {'p-readonly': this.readonly}];
            }
        }
    };

    const _hoisted_1 = { role: "tablist" };
    const _hoisted_2 = { class: "p-steps-number" };
    const _hoisted_3 = { class: "p-steps-title" };
    const _hoisted_4 = {
      key: 1,
      class: "p-menuitem-link",
      role: "presentation"
    };
    const _hoisted_5 = { class: "p-steps-number" };
    const _hoisted_6 = { class: "p-steps-title" };

    function render(_ctx, _cache, $props, $setup, $data, $options) {
      const _component_router_link = vue.resolveComponent("router-link");

      return (vue.openBlock(), vue.createBlock("div", {
        id: $props.id,
        class: $options.containerClass
      }, [
        vue.createVNode("ul", _hoisted_1, [
          (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList($props.model, (item, index) => {
            return (vue.openBlock(), vue.createBlock(vue.Fragment, {
              key: item.to
            }, [
              ($options.visible(item))
                ? (vue.openBlock(), vue.createBlock("li", {
                    key: 0,
                    class: $options.getItemClass(item),
                    style: item.style,
                    role: "tab",
                    "aria-selected": $options.isActive(item),
                    "aria-expanded": $options.isActive(item)
                  }, [
                    (!_ctx.$slots.item)
                      ? (vue.openBlock(), vue.createBlock(vue.Fragment, { key: 0 }, [
                          (!$options.isItemDisabled(item))
                            ? (vue.openBlock(), vue.createBlock(_component_router_link, {
                                key: 0,
                                to: item.to,
                                custom: ""
                              }, {
                                default: vue.withCtx(({navigate, href}) => [
                                  vue.createVNode("a", {
                                    href: href,
                                    class: "p-menuitem-link",
                                    onClick: $event => ($options.onItemClick($event, item, navigate)),
                                    role: "presentation"
                                  }, [
                                    vue.createVNode("span", _hoisted_2, vue.toDisplayString(index + 1), 1),
                                    vue.createVNode("span", _hoisted_3, vue.toDisplayString(item.label), 1)
                                  ], 8, ["href", "onClick"])
                                ]),
                                _: 2
                              }, 1032, ["to"]))
                            : (vue.openBlock(), vue.createBlock("span", _hoisted_4, [
                                vue.createVNode("span", _hoisted_5, vue.toDisplayString(index + 1), 1),
                                vue.createVNode("span", _hoisted_6, vue.toDisplayString(item.label), 1)
                              ]))
                        ], 64))
                      : (vue.openBlock(), vue.createBlock(vue.resolveDynamicComponent(_ctx.$slots.item), {
                          key: 1,
                          item: item
                        }, null, 8, ["item"]))
                  ], 14, ["aria-selected", "aria-expanded"]))
                : vue.createCommentVNode("", true)
            ], 64))
          }), 128))
        ])
      ], 10, ["id"]))
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

    var css_248z = "\n.p-steps {\n    position: relative;\n}\n.p-steps ul {\n    padding: 0;\n    margin: 0;\n    list-style-type: none;\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n}\n.p-steps-item {\n    position: relative;\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-pack: center;\n        -ms-flex-pack: center;\n            justify-content: center;\n    -webkit-box-flex: 1;\n        -ms-flex: 1 1 auto;\n            flex: 1 1 auto;\n}\n.p-steps-item .p-menuitem-link {\n    display: -webkit-inline-box;\n    display: -ms-inline-flexbox;\n    display: inline-flex;\n    -webkit-box-orient: vertical;\n    -webkit-box-direction: normal;\n        -ms-flex-direction: column;\n            flex-direction: column;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    overflow: hidden;\n    text-decoration: none;\n}\n.p-steps.p-steps-readonly .p-steps-item {\n    cursor: auto;\n}\n.p-steps-item.p-steps-current .p-menuitem-link {\n    cursor: default;\n}\n.p-steps-title {\n    white-space: nowrap;\n}\n.p-steps-number {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    -webkit-box-pack: center;\n        -ms-flex-pack: center;\n            justify-content: center;\n}\n.p-steps-title {\n    display: block;\n}\n";
    styleInject(css_248z);

    script.render = render;

    return script;

}(primevue.utils, Vue));

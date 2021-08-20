'use strict';

var utils = require('primevue/utils');
var Ripple = require('primevue/ripple');
var vue = require('vue');

function _interopDefaultLegacy (e) { return e && typeof e === 'object' && 'default' in e ? e : { 'default': e }; }

var Ripple__default = /*#__PURE__*/_interopDefaultLegacy(Ripple);

var script = {
    name: 'MegaMenu',
    props: {
		model: {
            type: Array,
            default: null
        },
        orientation: {
            type: String,
            default: 'horizontal'
        }
    },
    documentClickListener: null,
    data() {
        return {
            activeItem: null
        }
    },
    beforeUnmount() {
        this.unbindDocumentClickListener();
    },
    methods: {
        onLeafClick(event, item, navigate) {
            if (item.disabled) {
                event.preventDefault();
                return;
            }

            if (item.command) {
                item.command({
                    originalEvent: event,
                    item: item
                });
            }

            this.activeItem = null;

            if (item.to && navigate) {
                navigate(event);
            }
        },
        onCategoryMouseEnter(event, category) {
            if (category.disabled) {
                event.preventDefault();
                return;
            }

            if (this.activeItem) {
                this.activeItem = category;
            }
        },
        onCategoryClick(event, category, navigate) {
            if (category.disabled) {
                event.preventDefault();
                return;
            }

            if (category.command) {
                category.command({
                    originalEvent: event,
                    item: category
                });
            }

            if (category.items) {
                if (this.activeItem && this.activeItem === category) {
                    this.activeItem = null;
                    this.unbindDocumentClickListener();
                }
                else {
                    this.activeItem = category;
                    this.bindDocumentClickListener();
                }
            }

            if (category.to && navigate) {
                navigate(event);
            }
        },
        onCategoryKeydown(event, category) {
            let listItem = event.currentTarget.parentElement;

            switch(event.which) {
                //down
                case 40:
                    if (this.horizontal)
                        this.expandMenu(category);
                    else
                        this.navigateToNextItem(listItem);

                    event.preventDefault();
                break;

                //up
                case 38:
                    if (this.vertical)
                        this.navigateToPrevItem(listItem);
                    else if (category.items && category === this.activeItem)
                        this.collapseMenu();

                    event.preventDefault();
                break;

                //right
                case 39:
                    if (this.horizontal)
                        this.navigateToNextItem(listItem);
                    else
                        this.expandMenu(category);

                    event.preventDefault();
                break;

                //left
                case 37:
                    if (this.horizontal)
                        this.navigateToPrevItem(listItem);
                    else if (category.items && category === this.activeItem)
                        this.collapseMenu();

                    event.preventDefault();
                break;
            }
        },
        expandMenu(item) {
            if (item.items) {
                this.activeItem = item;
            }
        },
        collapseMenu() {
            this.activeItem = null;
        },
        findNextItem(item) {
            let nextItem = item.nextElementSibling;

            if (nextItem)
                return utils.DomHandler.hasClass(nextItem, 'p-disabled') || !utils.DomHandler.hasClass(nextItem, 'p-menuitem') ? this.findNextItem(nextItem) : nextItem;
            else
                return null;
        },
        findPrevItem(item) {
            let prevItem = item.previousElementSibling;

            if (prevItem)
                return utils.DomHandler.hasClass(prevItem, 'p-disabled') || !utils.DomHandler.hasClass(prevItem, 'p-menuitem') ? this.findPrevItem(prevItem) : prevItem;
            else
                return null;
        },
        navigateToNextItem(listItem) {
            var nextItem = this.findNextItem(listItem);
            if (nextItem) {
                nextItem.children[0].focus();
            }
        },
        navigateToPrevItem(listItem) {
            var prevItem = this.findPrevItem(listItem);
            if (prevItem) {
                prevItem.children[0].focus();
            }
        },
        getCategoryClass(category) {
            return ['p-menuitem', {
                'p-menuitem-active': category === this.activeItem
            }, category.class];
        },
        getCategorySubMenuIcon() {
            return ['p-submenu-icon pi', {
                'pi-angle-down': this.horizontal,
                'pi-angle-right': this.vertical
            }];
        },
        getCategoryIcon(category) {
            return ['p-menuitem-icon', category.icon];
        },
        getColumnClassName(category) {
            let length = category.items ? category.items.length: 0;
            let columnClass;

            switch(length) {
                case 2:
                    columnClass= 'p-megamenu-col-6';
                break;

                case 3:
                    columnClass= 'p-megamenu-col-4';
                break;

                case 4:
                    columnClass= 'p-megamenu-col-3';
                break;

                case 6:
                    columnClass= 'p-megamenu-col-2';
                break;

                default:
                    columnClass= 'p-megamenu-col-12';
                break;
            }

            return columnClass;
        },
        getSubmenuHeaderClass(submenu) {
            return ['p-megamenu-submenu-header', submenu.class, {'p-disabled': submenu.disabled}];
        },
        getSubmenuItemClass(item) {
            return ['p-menuitem', item.class];
        },
        getLinkClass(item) {
            return ['p-menuitem-link', {'p-disabled': item.disabled}];
        },
        bindDocumentClickListener() {
            if (!this.documentClickListener) {
                this.documentClickListener = (event) => {
                    if (this.$el && !this.$el.contains(event.target)) {
                        this.activeItem = null;
                        this.unbindDocumentClickListener();
                    }
                };

                document.addEventListener('click', this.documentClickListener);
            }
        },
        unbindDocumentClickListener() {
            if (this.documentClickListener) {
                document.removeEventListener('click', this.documentClickListener);
                this.documentClickListener = null;
            }
        },
        visible(item) {
            return (typeof item.visible === 'function' ? item.visible() : item.visible !== false);
        }
    },
    computed: {
        containerClass() {
            return ['p-megamenu p-component', {
                'p-megamenu-horizontal': this.horizontal,
                'p-megamenu-vertical': this.vertical
            }];
        },
        horizontal() {
            return this.orientation === 'horizontal';
        },
        vertical() {
            return this.orientation === 'vertical';
        }
    },
    directives: {
        'ripple': Ripple__default['default']
    }
};

const _hoisted_1 = {
  class: "p-megamenu-root-list",
  role: "menubar"
};
const _hoisted_2 = { class: "p-menuitem-text" };
const _hoisted_3 = { class: "p-menuitem-text" };
const _hoisted_4 = {
  key: 2,
  class: "p-megamenu-panel"
};
const _hoisted_5 = { class: "p-megamenu-grid" };
const _hoisted_6 = { class: "p-menuitem-text" };
const _hoisted_7 = { class: "p-menuitem-text" };
const _hoisted_8 = {
  key: 0,
  class: "p-megamenu-custom"
};

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_router_link = vue.resolveComponent("router-link");
  const _directive_ripple = vue.resolveDirective("ripple");

  return (vue.openBlock(), vue.createBlock("div", { class: $options.containerClass }, [
    vue.createVNode("ul", _hoisted_1, [
      (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList($props.model, (category, index) => {
        return (vue.openBlock(), vue.createBlock(vue.Fragment, {
          key: category.label + '_' + index
        }, [
          ($options.visible(category))
            ? (vue.openBlock(), vue.createBlock("li", {
                key: 0,
                class: $options.getCategoryClass(category),
                style: category.style,
                onMouseenter: $event => ($options.onCategoryMouseEnter($event, category)),
                role: "none"
              }, [
                (!_ctx.$slots.item)
                  ? (vue.openBlock(), vue.createBlock(vue.Fragment, { key: 0 }, [
                      (category.to && !category.disabled)
                        ? (vue.openBlock(), vue.createBlock(_component_router_link, {
                            key: 0,
                            to: category.to,
                            custom: ""
                          }, {
                            default: vue.withCtx(({navigate, href}) => [
                              vue.withDirectives(vue.createVNode("a", {
                                href: href,
                                class: $options.getLinkClass(category),
                                onClick: $event => ($options.onCategoryClick($event, category, navigate)),
                                onKeydown: $event => ($options.onCategoryKeydown($event, category)),
                                role: "menuitem"
                              }, [
                                (category.icon)
                                  ? (vue.openBlock(), vue.createBlock("span", {
                                      key: 0,
                                      class: $options.getCategoryIcon(category)
                                    }, null, 2))
                                  : vue.createCommentVNode("", true),
                                vue.createVNode("span", _hoisted_2, vue.toDisplayString(category.label), 1)
                              ], 42, ["href", "onClick", "onKeydown"]), [
                                [_directive_ripple]
                              ])
                            ]),
                            _: 2
                          }, 1032, ["to"]))
                        : vue.withDirectives((vue.openBlock(), vue.createBlock("a", {
                            key: 1,
                            href: category.url,
                            class: $options.getLinkClass(category),
                            target: category.target,
                            onClick: $event => ($options.onCategoryClick($event, category)),
                            onKeydown: $event => ($options.onCategoryKeydown($event, category)),
                            role: "menuitem",
                            "aria-haspopup": category.items != null,
                            "aria-expanded": category === $data.activeItem,
                            tabindex: category.disabled ? null : '0'
                          }, [
                            (category.icon)
                              ? (vue.openBlock(), vue.createBlock("span", {
                                  key: 0,
                                  class: $options.getCategoryIcon(category)
                                }, null, 2))
                              : vue.createCommentVNode("", true),
                            vue.createVNode("span", _hoisted_3, vue.toDisplayString(category.label), 1),
                            (category.items)
                              ? (vue.openBlock(), vue.createBlock("span", {
                                  key: 1,
                                  class: $options.getCategorySubMenuIcon()
                                }, null, 2))
                              : vue.createCommentVNode("", true)
                          ], 42, ["href", "target", "onClick", "onKeydown", "aria-haspopup", "aria-expanded", "tabindex"])), [
                            [_directive_ripple]
                          ])
                    ], 64))
                  : (vue.openBlock(), vue.createBlock(vue.resolveDynamicComponent(_ctx.$slots.item), {
                      key: 1,
                      item: _ctx.item
                    }, null, 8, ["item"])),
                (category.items)
                  ? (vue.openBlock(), vue.createBlock("div", _hoisted_4, [
                      vue.createVNode("div", _hoisted_5, [
                        (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList(category.items, (column, columnIndex) => {
                          return (vue.openBlock(), vue.createBlock("div", {
                            key: category.label + '_column_' + columnIndex,
                            class: $options.getColumnClassName(category)
                          }, [
                            (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList(column, (submenu, submenuIndex) => {
                              return (vue.openBlock(), vue.createBlock("ul", {
                                class: "p-megamenu-submenu",
                                key: submenu.label + '_submenu_' + submenuIndex,
                                role: "menu"
                              }, [
                                vue.createVNode("li", {
                                  class: $options.getSubmenuHeaderClass(submenu),
                                  style: submenu.style,
                                  role: "presentation"
                                }, vue.toDisplayString(submenu.label), 7),
                                (vue.openBlock(true), vue.createBlock(vue.Fragment, null, vue.renderList(submenu.items, (item, i) => {
                                  return (vue.openBlock(), vue.createBlock(vue.Fragment, {
                                    key: item.label + i.toString()
                                  }, [
                                    ($options.visible(item) && !item.separator)
                                      ? (vue.openBlock(), vue.createBlock("li", {
                                          key: 0,
                                          role: "none",
                                          class: $options.getSubmenuItemClass(item),
                                          style: item.style
                                        }, [
                                          (!_ctx.$slots.item)
                                            ? (vue.openBlock(), vue.createBlock(vue.Fragment, { key: 0 }, [
                                                (item.to && !item.disabled)
                                                  ? (vue.openBlock(), vue.createBlock(_component_router_link, {
                                                      key: 0,
                                                      to: item.to,
                                                      custom: ""
                                                    }, {
                                                      default: vue.withCtx(({navigate, href}) => [
                                                        vue.withDirectives(vue.createVNode("a", {
                                                          href: href,
                                                          class: $options.getLinkClass(item),
                                                          onClick: $event => ($options.onLeafClick($event, item, navigate)),
                                                          role: "menuitem"
                                                        }, [
                                                          (item.icon)
                                                            ? (vue.openBlock(), vue.createBlock("span", {
                                                                key: 0,
                                                                class: ['p-menuitem-icon', item.icon]
                                                              }, null, 2))
                                                            : vue.createCommentVNode("", true),
                                                          vue.createVNode("span", _hoisted_6, vue.toDisplayString(item.label), 1)
                                                        ], 10, ["href", "onClick"]), [
                                                          [_directive_ripple]
                                                        ])
                                                      ]),
                                                      _: 2
                                                    }, 1032, ["to"]))
                                                  : vue.withDirectives((vue.openBlock(), vue.createBlock("a", {
                                                      key: 1,
                                                      href: item.url,
                                                      class: $options.getLinkClass(item),
                                                      target: item.target,
                                                      onClick: $event => ($options.onLeafClick($event, item)),
                                                      role: "menuitem",
                                                      tabindex: item.disabled ? null : '0'
                                                    }, [
                                                      (item.icon)
                                                        ? (vue.openBlock(), vue.createBlock("span", {
                                                            key: 0,
                                                            class: ['p-menuitem-icon', item.icon]
                                                          }, null, 2))
                                                        : vue.createCommentVNode("", true),
                                                      vue.createVNode("span", _hoisted_7, vue.toDisplayString(item.label), 1),
                                                      (item.items)
                                                        ? (vue.openBlock(), vue.createBlock("span", {
                                                            key: 1,
                                                            class: _ctx.getSubmenuIcon()
                                                          }, null, 2))
                                                        : vue.createCommentVNode("", true)
                                                    ], 10, ["href", "target", "onClick", "tabindex"])), [
                                                      [_directive_ripple]
                                                    ])
                                              ], 64))
                                            : (vue.openBlock(), vue.createBlock(vue.resolveDynamicComponent(_ctx.$slots.item), {
                                                key: 1,
                                                item: item
                                              }, null, 8, ["item"]))
                                        ], 6))
                                      : vue.createCommentVNode("", true),
                                    ($options.visible(item) && item.separator)
                                      ? (vue.openBlock(), vue.createBlock("li", {
                                          class: ['p-menu-separator', item.class],
                                          style: item.style,
                                          key: 'separator' + i.toString(),
                                          role: "separator"
                                        }, null, 6))
                                      : vue.createCommentVNode("", true)
                                  ], 64))
                                }), 128))
                              ]))
                            }), 128))
                          ], 2))
                        }), 128))
                      ])
                    ]))
                  : vue.createCommentVNode("", true)
              ], 46, ["onMouseenter"]))
            : vue.createCommentVNode("", true)
        ], 64))
      }), 128))
    ]),
    (_ctx.$slots.default)
      ? (vue.openBlock(), vue.createBlock("div", _hoisted_8, [
          vue.renderSlot(_ctx.$slots, "default")
        ]))
      : vue.createCommentVNode("", true)
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

var css_248z = "\n.p-megamenu-root-list {\n    margin: 0;\n    padding: 0;\n    list-style: none;\n}\n.p-megamenu-root-list > .p-menuitem {\n    position: relative;\n}\n.p-megamenu .p-menuitem-link {\n    cursor: pointer;\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    text-decoration: none;\n    overflow: hidden;\n    position: relative;\n}\n.p-megamenu .p-menuitem-text {\n    line-height: 1;\n}\n.p-megamenu-panel {\n    display: none;\n    position: absolute;\n    width: auto;\n    z-index: 1;\n}\n.p-megamenu-root-list > .p-menuitem-active > .p-megamenu-panel {\n    display: block;\n}\n.p-megamenu-submenu {\n    margin: 0;\n    padding: 0;\n    list-style: none;\n}\n\n/* Horizontal */\n.p-megamenu-horizontal .p-megamenu-root-list {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n    -webkit-box-align: center;\n        -ms-flex-align: center;\n            align-items: center;\n    -ms-flex-wrap: wrap;\n        flex-wrap: wrap;\n}\n\n/* Vertical */\n.p-megamenu-vertical .p-megamenu-root-list {\n    -webkit-box-orient: vertical;\n    -webkit-box-direction: normal;\n        -ms-flex-direction: column;\n            flex-direction: column;\n}\n.p-megamenu-vertical .p-megamenu-root-list > .p-menuitem-active > .p-megamenu-panel {\n    left: 100%;\n    top: 0;\n}\n.p-megamenu-vertical .p-megamenu-root-list > .p-menuitem > .p-menuitem-link > .p-submenu-icon {\n    margin-left: auto;\n}\n.p-megamenu-grid {\n    display: -webkit-box;\n    display: -ms-flexbox;\n    display: flex;\n}\n.p-megamenu-col-2,\n.p-megamenu-col-3,\n.p-megamenu-col-4,\n.p-megamenu-col-6,\n.p-megamenu-col-12 {\n    -webkit-box-flex: 0;\n        -ms-flex: 0 0 auto;\n            flex: 0 0 auto;\n    padding: 0.5rem;\n}\n.p-megamenu-col-2 {\n    width: 16.6667%;\n}\n.p-megamenu-col-3 {\n    width: 25%;\n}\n.p-megamenu-col-4 {\n    width: 33.3333%;\n}\n.p-megamenu-col-6 {\n    width: 50%;\n}\n.p-megamenu-col-12 {\n    width: 100%;\n}\n";
styleInject(css_248z);

script.render = render;

module.exports = script;

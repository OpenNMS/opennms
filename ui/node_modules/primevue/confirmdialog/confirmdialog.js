this.primevue = this.primevue || {};
this.primevue.confirmdialog = (function (ConfirmationEventBus, Dialog, Button, vue) {
    'use strict';

    function _interopDefaultLegacy (e) { return e && typeof e === 'object' && 'default' in e ? e : { 'default': e }; }

    var ConfirmationEventBus__default = /*#__PURE__*/_interopDefaultLegacy(ConfirmationEventBus);
    var Dialog__default = /*#__PURE__*/_interopDefaultLegacy(Dialog);
    var Button__default = /*#__PURE__*/_interopDefaultLegacy(Button);

    var script = {
        name: 'ConfirmDialog',
        props: {
            group: String,
            breakpoints: {
                type: Object,
                default: null
            }
        },
        confirmListener: null,
        closeListener: null,
        data() {
            return {
                visible: false,
                confirmation: null,
            }
        },
        mounted() {
            this.confirmListener = (options) => {
                if (!options) {
                    return;
                }

                if (options.group === this.group) {
                    this.confirmation = options;
                    this.visible = true;
                }
            };

            this.closeListener = () => {
                this.visible = false;
                this.confirmation = null;
            };
            ConfirmationEventBus__default['default'].on('confirm', this.confirmListener);
            ConfirmationEventBus__default['default'].on('close', this.closeListener);
        },
        beforeUnmount() {
            ConfirmationEventBus__default['default'].off('confirm', this.confirmListener);
            ConfirmationEventBus__default['default'].off('close', this.closeListener);
        },
        methods: {
            accept() {
                if (this.confirmation.accept) {
                    this.confirmation.accept();
                }

                this.visible = false;
            },
            reject() {
                if (this.confirmation.reject) {
                    this.confirmation.reject();
                }

                this.visible = false;
            }
        },
        computed: {
            header() {
                return this.confirmation ? this.confirmation.header : null;
            },
            message() {
                return this.confirmation ? this.confirmation.message : null;
            },
            blockScroll() {
                return this.confirmation ? this.confirmation.blockScroll : true;
            },
            position() {
                return this.confirmation ? this.confirmation.position : null;
            },
            iconClass() {
                return ['p-confirm-dialog-icon', this.confirmation ? this.confirmation.icon : null];
            },
            acceptLabel() {
                return this.confirmation ? (this.confirmation.acceptLabel || this.$primevue.config.locale.accept) : null;
            },
            rejectLabel() {
                return this.confirmation ? (this.confirmation.rejectLabel || this.$primevue.config.locale.reject) : null;
            },
            acceptIcon() {
                return this.confirmation ? this.confirmation.acceptIcon : null;
            },
            rejectIcon() {
                return this.confirmation ? this.confirmation.rejectIcon : null;
            },
            acceptClass() {
                return ['p-confirm-dialog-accept', this.confirmation ? this.confirmation.acceptClass : null];
            },
            rejectClass() {
                return ['p-confirm-dialog-reject', this.confirmation ? (this.confirmation.rejectClass || 'p-button-text') : null];
            }
        },
        components: {
            'CDialog': Dialog__default['default'],
            'CDButton': Button__default['default']
        }
    };

    const _hoisted_1 = { class: "p-confirm-dialog-message" };

    function render(_ctx, _cache, $props, $setup, $data, $options) {
      const _component_CDButton = vue.resolveComponent("CDButton");
      const _component_CDialog = vue.resolveComponent("CDialog");

      return (vue.openBlock(), vue.createBlock(_component_CDialog, {
        visible: $data.visible,
        "onUpdate:visible": _cache[3] || (_cache[3] = $event => ($data.visible = $event)),
        modal: true,
        header: $options.header,
        blockScroll: $options.blockScroll,
        position: $options.position,
        class: "p-confirm-dialog",
        breakpoints: $props.breakpoints
      }, {
        footer: vue.withCtx(() => [
          vue.createVNode(_component_CDButton, {
            label: $options.rejectLabel,
            icon: $options.rejectIcon,
            class: $options.rejectClass,
            onClick: _cache[1] || (_cache[1] = $event => ($options.reject()))
          }, null, 8, ["label", "icon", "class"]),
          vue.createVNode(_component_CDButton, {
            label: $options.acceptLabel,
            icon: $options.acceptIcon,
            class: $options.acceptClass,
            onClick: _cache[2] || (_cache[2] = $event => ($options.accept())),
            autofocus: ""
          }, null, 8, ["label", "icon", "class"])
        ]),
        default: vue.withCtx(() => [
          vue.createVNode("i", { class: $options.iconClass }, null, 2),
          vue.createVNode("span", _hoisted_1, vue.toDisplayString($options.message), 1)
        ]),
        _: 1
      }, 8, ["visible", "header", "blockScroll", "position", "breakpoints"]))
    }

    script.render = render;

    return script;

}(primevue.confirmationeventbus, primevue.dialog, primevue.button, Vue));

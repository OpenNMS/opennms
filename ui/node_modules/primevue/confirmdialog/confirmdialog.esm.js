import ConfirmationEventBus from 'primevue/confirmationeventbus';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import { resolveComponent, openBlock, createBlock, withCtx, createVNode, toDisplayString } from 'vue';

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
        ConfirmationEventBus.on('confirm', this.confirmListener);
        ConfirmationEventBus.on('close', this.closeListener);
    },
    beforeUnmount() {
        ConfirmationEventBus.off('confirm', this.confirmListener);
        ConfirmationEventBus.off('close', this.closeListener);
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
        'CDialog': Dialog,
        'CDButton': Button
    }
};

const _hoisted_1 = { class: "p-confirm-dialog-message" };

function render(_ctx, _cache, $props, $setup, $data, $options) {
  const _component_CDButton = resolveComponent("CDButton");
  const _component_CDialog = resolveComponent("CDialog");

  return (openBlock(), createBlock(_component_CDialog, {
    visible: $data.visible,
    "onUpdate:visible": _cache[3] || (_cache[3] = $event => ($data.visible = $event)),
    modal: true,
    header: $options.header,
    blockScroll: $options.blockScroll,
    position: $options.position,
    class: "p-confirm-dialog",
    breakpoints: $props.breakpoints
  }, {
    footer: withCtx(() => [
      createVNode(_component_CDButton, {
        label: $options.rejectLabel,
        icon: $options.rejectIcon,
        class: $options.rejectClass,
        onClick: _cache[1] || (_cache[1] = $event => ($options.reject()))
      }, null, 8, ["label", "icon", "class"]),
      createVNode(_component_CDButton, {
        label: $options.acceptLabel,
        icon: $options.acceptIcon,
        class: $options.acceptClass,
        onClick: _cache[2] || (_cache[2] = $event => ($options.accept())),
        autofocus: ""
      }, null, 8, ["label", "icon", "class"])
    ]),
    default: withCtx(() => [
      createVNode("i", { class: $options.iconClass }, null, 2),
      createVNode("span", _hoisted_1, toDisplayString($options.message), 1)
    ]),
    _: 1
  }, 8, ["visible", "header", "blockScroll", "position", "breakpoints"]))
}

script.render = render;

export default script;

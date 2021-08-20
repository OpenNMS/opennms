import { DomHandler, ZIndexUtils } from 'primevue/utils';
import { openBlock, createBlock, renderSlot } from 'vue';

var script = {
    name: 'BlockUI',
    emits: ['block', 'unblock'],
    props: {
        blocked: {
            type: Boolean,
            default: false
        },
        fullScreen: {
            type: Boolean,
            default: false
        },
        baseZIndex: {
            type: Number,
            default: 0
        },
        autoZIndex: {
            type: Boolean,
            default: true
        }
    },
    mask: null,
    mounted() {
        if (this.blocked) {
            this.block();
        }
    },
    watch: {
        blocked(newValue) {
            if (newValue === true)
                this.block();
            else
                this.unblock();
        }
    },
    methods: {
        block() {
            if (this.fullScreen) {
                this.mask = document.createElement('div');
                this.mask.setAttribute('class', 'p-blockui p-blockui-document');
                document.body.appendChild(this.mask);
                DomHandler.addClass(document.body, 'p-overflow-hidden');
                document.activeElement.blur();
            }
            else {
                this.mask = document.createElement('div');
                this.mask.setAttribute('class', 'p-blockui');
                this.$refs.container.appendChild(this.mask);
            }

            if (this.mask) {
                setTimeout(() => {
                    DomHandler.addClass(this.mask, 'p-component-overlay');
                }, 1);
            }

            if (this.autoZIndex) {
                ZIndexUtils.set('modal', this.mask, this.baseZIndex + this.$primevue.config.zIndex.modal);
            }

            this.$emit('block');
        },
        unblock() {
            DomHandler.addClass(this.mask, 'p-blockui-leave');
            this.mask.addEventListener('transitionend', () => {
                this.removeMask();
            });
        },
        removeMask() {
            ZIndexUtils.clear(this.mask);
             if (this.fullScreen) {
                document.body.removeChild(this.mask);
                DomHandler.removeClass(document.body, 'p-overflow-hidden');
            }
            else {
                this.$refs.container.removeChild(this.mask);
            }

            this.$emit('unblock');
        }
    }
};

const _hoisted_1 = {
  ref: "container",
  class: "p-blockui-container"
};

function render(_ctx, _cache, $props, $setup, $data, $options) {
  return (openBlock(), createBlock("div", _hoisted_1, [
    renderSlot(_ctx.$slots, "default")
  ], 512))
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

var css_248z = "\n.p-blockui-container {\n    position: relative;\n}\n.p-blockui {\n    position: absolute;\n    top: 0;\n    left: 0;\n    width: 100%;\n    height: 100%;\n    background-color: transparent;\n    -webkit-transition-property: background-color;\n    transition-property: background-color;\n}\n.p-blockui.p-component-overlay {\n    position: absolute;\n}\n.p-blockui-document.p-component-overlay {\n    position: fixed;\n}\n.p-blockui.p-blockui-leave.p-component-overlay {\n    background-color: transparent;\n}\n";
styleInject(css_248z);

script.render = render;

export default script;

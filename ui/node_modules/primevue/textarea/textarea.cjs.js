'use strict';

var vue = require('vue');

var script = {
    name: 'Textarea',
    emits: ['update:modelValue'],
    props: {
        modelValue: null,
        autoResize: Boolean
    },
    mounted() {
        if (this.$el.offsetParent && this.autoResize) {
            this.resize();
        }
    },
    updated() {
        if (this.$el.offsetParent && this.autoResize) {
            this.resize();
        }
    },
    methods: {
        resize() {
            const style = window.getComputedStyle(this.$el);
            this.$el.style.height = 'auto';
            this.$el.style.height = `calc(${style.borderTopWidth} + ${style.borderBottomWidth} + ${this.$el.scrollHeight}px)`;

            if (parseFloat(this.$el.style.height) >= parseFloat(this.$el.style.maxHeight)) {
                this.$el.style.overflowY = "scroll";
                this.$el.style.height = this.$el.style.maxHeight;
            }
            else {
                this.$el.style.overflow = "hidden";
            }
        },
        onInput(event) {
             if (this.autoResize) {
                this.resize();
            }

            this.$emit('update:modelValue', event.target.value);
        }
    },
    computed: {
        filled() {
            return (this.modelValue != null && this.modelValue.toString().length > 0)
        }
    }
};

function render(_ctx, _cache, $props, $setup, $data, $options) {
  return (vue.openBlock(), vue.createBlock("textarea", vue.mergeProps({
    class: ['p-inputtextarea p-inputtext p-component', {'p-filled': $options.filled, 'p-inputtextarea-resizable ': $props.autoResize}]
  }, _ctx.$attrs, {
    value: $props.modelValue,
    onInput: _cache[1] || (_cache[1] = (...args) => ($options.onInput && $options.onInput(...args)))
  }), null, 16, ["value"]))
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

var css_248z = "\n.p-inputtextarea-resizable {\n    overflow: hidden;\n    resize: none;\n}\n.p-fluid .p-inputtextarea {\n    width: 100%;\n}\n";
styleInject(css_248z);

script.render = render;

module.exports = script;

this.primevue = this.primevue || {};
this.primevue.radiobutton = (function (utils, vue) {
    'use strict';

    var script = {
        name: 'RadioButton',
        inheritAttrs: false,
        emits: ['click', 'update:modelValue', 'change'],
        props: {
    		value: null,
            modelValue: null,
            class: null,
            style: null
        },
        data() {
            return {
                focused: false
            };
        },
        methods: {
            onClick(event) {
                if (!this.$attrs.disabled) {
                    this.$emit('click', event);
                    this.$emit('update:modelValue', this.value);
                    this.$refs.input.focus();

                    if (!this.checked) {
                        this.$emit('change', event);
                    }
                }
            },
            onFocus() {
                this.focused = true;
            },
            onBlur() {
                this.focused = false;
            }
        },
        computed: {
            checked() {
                return this.modelValue != null && utils.ObjectUtils.equals(this.modelValue, this.value);
            },
            containerClass() {
                return ['p-radiobutton p-component', this.class, {'p-radiobutton-checked': this.checked, 'p-radiobutton-disabled': this.$attrs.disabled, 'p-radiobutton-focused': this.focused}];
            }
        }
    };

    const _hoisted_1 = { class: "p-hidden-accessible" };
    const _hoisted_2 = /*#__PURE__*/vue.createVNode("div", { class: "p-radiobutton-icon" }, null, -1);

    function render(_ctx, _cache, $props, $setup, $data, $options) {
      return (vue.openBlock(), vue.createBlock("div", {
        class: $options.containerClass,
        onClick: _cache[3] || (_cache[3] = $event => ($options.onClick($event))),
        style: $props.style
      }, [
        vue.createVNode("div", _hoisted_1, [
          vue.createVNode("input", vue.mergeProps({
            ref: "input",
            type: "radio",
            checked: $options.checked,
            value: $props.value
          }, _ctx.$attrs, {
            onFocus: _cache[1] || (_cache[1] = (...args) => ($options.onFocus && $options.onFocus(...args))),
            onBlur: _cache[2] || (_cache[2] = (...args) => ($options.onBlur && $options.onBlur(...args)))
          }), null, 16, ["checked", "value"])
        ]),
        vue.createVNode("div", {
          ref: "box",
          class: ['p-radiobutton-box', {'p-highlight': $options.checked, 'p-disabled': _ctx.$attrs.disabled, 'p-focus': $data.focused}],
          role: "radio",
          "aria-checked": $options.checked
        }, [
          _hoisted_2
        ], 10, ["aria-checked"])
      ], 6))
    }

    script.render = render;

    return script;

}(primevue.utils, Vue));

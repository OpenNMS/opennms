this.primevue = this.primevue || {};
this.primevue.checkbox = (function (utils, vue) {
    'use strict';

    var script = {
        name: 'Checkbox',
        inheritAttrs: false,
        emits: ['click', 'update:modelValue', 'change'],
        props: {
            value: null,
            modelValue: null,
            binary: Boolean,
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
                    let newModelValue;

                    if (this.binary) {
                        newModelValue = !this.modelValue;
                    }
                    else {
                        if (this.checked)
                            newModelValue = this.modelValue.filter(val => !utils.ObjectUtils.equals(val, this.value));
                        else
                            newModelValue = this.modelValue ? [...this.modelValue, this.value] : [this.value];
                    }

                    this.$emit('click', event);
                    this.$emit('update:modelValue', newModelValue);
                    this.$emit('change', event);
                    this.$refs.input.focus();
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
                return this.binary ? this.modelValue : utils.ObjectUtils.contains(this.value, this.modelValue);
            },
            containerClass() {
                return ['p-checkbox p-component', this.class, {'p-checkbox-checked': this.checked, 'p-checkbox-disabled': this.$attrs.disabled, 'p-checkbox-focused': this.focused}];
            }
        }
    };

    const _hoisted_1 = { class: "p-hidden-accessible" };

    function render(_ctx, _cache, $props, $setup, $data, $options) {
      return (vue.openBlock(), vue.createBlock("div", {
        class: $options.containerClass,
        onClick: _cache[3] || (_cache[3] = $event => ($options.onClick($event))),
        style: $props.style
      }, [
        vue.createVNode("div", _hoisted_1, [
          vue.createVNode("input", vue.mergeProps({
            ref: "input",
            type: "checkbox",
            checked: $options.checked,
            value: $props.value
          }, _ctx.$attrs, {
            onFocus: _cache[1] || (_cache[1] = (...args) => ($options.onFocus && $options.onFocus(...args))),
            onBlur: _cache[2] || (_cache[2] = (...args) => ($options.onBlur && $options.onBlur(...args)))
          }), null, 16, ["checked", "value"])
        ]),
        vue.createVNode("div", {
          ref: "box",
          class: ['p-checkbox-box', {'p-highlight': $options.checked, 'p-disabled': _ctx.$attrs.disabled, 'p-focus': $data.focused}],
          role: "checkbox",
          "aria-checked": $options.checked
        }, [
          vue.createVNode("span", {
            class: ['p-checkbox-icon', {'pi pi-check': $options.checked}]
          }, null, 2)
        ], 10, ["aria-checked"])
      ], 6))
    }

    script.render = render;

    return script;

}(primevue.utils, Vue));

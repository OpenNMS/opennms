this.primevue = this.primevue || {};
this.primevue.inputtext = (function (vue) {
    'use strict';

    var script = {
        name: 'InputText',
        emits: ['update:modelValue'],
        props: {
            modelValue: null
        },
        methods: {
            onInput(event) {
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
      return (vue.openBlock(), vue.createBlock("input", {
        class: ['p-inputtext p-component', {'p-filled': $options.filled}],
        value: $props.modelValue,
        onInput: _cache[1] || (_cache[1] = (...args) => ($options.onInput && $options.onInput(...args)))
      }, null, 42, ["value"]))
    }

    script.render = render;

    return script;

}(Vue));

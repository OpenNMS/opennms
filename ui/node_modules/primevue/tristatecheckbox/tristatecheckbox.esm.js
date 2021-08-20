import { openBlock, createBlock, createVNode, mergeProps } from 'vue';

var script = {
    name: 'TriStateCheckbox',
    inheritAttrs: false,
    emits: ['click', 'update:modelValue', 'change'],
    props: {
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
                let newValue;

                switch (this.modelValue) {
                    case true:
                        newValue = false;
                    break;

                    case false:
                        newValue = null;
                    break;

                    case null:
                        newValue = true;
                    break;
                }

                this.$emit('click', event);
                this.$emit('update:modelValue', newValue);
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
        icon() {
            let icon;
            switch (this.modelValue) {
                case true:
                    icon = 'pi pi-check';
                break;

                case false:
                    icon = 'pi pi-times';
                break;

                case null:
                    icon = null;
                break;
            }

            return icon;
        },
        containerClass() {
            return ['p-checkbox p-component', this.class, {'p-checkbox-checked': this.modelValue === true, 'p-checkbox-disabled': this.$attrs.disabled, 'p-checkbox-focused': this.focused}];
        }
    }
};

const _hoisted_1 = { class: "p-hidden-accessible" };

function render(_ctx, _cache, $props, $setup, $data, $options) {
  return (openBlock(), createBlock("div", {
    class: $options.containerClass,
    onClick: _cache[3] || (_cache[3] = $event => ($options.onClick($event))),
    style: $props.style
  }, [
    createVNode("div", _hoisted_1, [
      createVNode("input", mergeProps({
        ref: "input",
        type: "checkbox",
        checked: $props.modelValue === true
      }, _ctx.$attrs, {
        onFocus: _cache[1] || (_cache[1] = $event => ($options.onFocus())),
        onBlur: _cache[2] || (_cache[2] = $event => ($options.onBlur()))
      }), null, 16, ["checked"])
    ]),
    createVNode("div", {
      ref: "box",
      class: ['p-checkbox-box', {'p-highlight': ($props.modelValue != null), 'p-disabled': _ctx.$attrs.disabled, 'p-focus': $data.focused}],
      role: "checkbox",
      "aria-checked": $props.modelValue === true
    }, [
      createVNode("span", {
        class: ['p-checkbox-icon', $options.icon]
      }, null, 2)
    ], 10, ["aria-checked"])
  ], 6))
}

script.render = render;

export default script;

<script lang="ts">
import { defineComponent, h, toRaw, PropType, DefineComponent } from 'vue'

// Self-contained icon wrapper (replaces @featherds/icon's FeatherIcon).
// Depends only on `vue`. Renders an icon component as an inline <svg>, sized
// in `em` and coloured via currentColor. Same API/behavior as FeatherIcon.
// The icon set itself lives alongside this wrapper in ../icons/<category>/
// (NMS-20243) and is reached via the './icons/*' subpath export, NOT the
// barrel — see the "Icons" section in the package README for why.
export const props = {
  icon: {
    type: Object as PropType<unknown>,
    required: false
  },
  flex: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    required: false
  }
} as const

export default defineComponent({
  props,
  render() {
    const attrs = this.$attrs
    const cls = attrs['class']
      ? (attrs['class'] as string).split(' ').reduce((o, key) => {
        o[key] = true
        return o
      }, {} as Record<string, boolean>)
      : {}
    const _attrs: Record<string, unknown> = {}
    cls['onms-icon'] = true

    if (this.flex) {
      cls['onms-icon-flex'] = true
    }

    if (this.title) {
      _attrs['aria-label'] = this.title
      _attrs['aria-hidden'] = 'false'
    } else {
      _attrs['aria-hidden'] = 'true'
    }

    _attrs['focusable'] = 'false'
    _attrs['role'] = 'img'

    const icon = toRaw(this.icon) as DefineComponent

    if (this.$slots.default) {
      return h('span', { class: 'onms-icon-container' }, [
        h(this.$slots.default()[0]!, {
          class: cls,
          ..._attrs
        })
      ])
    }

    return h(icon, {
      class: cls,
      ..._attrs
    })
  }
})
</script>

<style lang="scss" scoped>
.onms-icon {
  height: 1em;
  width: 1em;
  color: inherit;
  fill: currentColor;
  display: inline-block;
  overflow: visible;
  vertical-align: -0.125em;
}
.onms-icon-flex {
  vertical-align: baseline;
}

.onms-icon-container {
  :deep(svg) {
    height: 1em;
    width: 1em;
    color: inherit;
    fill: currentColor;
    display: inline-block;
    overflow: visible;
    vertical-align: -0.125em;
  }
  :deep(.onms-icon-flex) {
    vertical-align: baseline;
  }
}
</style>

<template>
  <div :class="['app-layout', navLayout]">
    <div class="app-header">
      <slot
        v-if="navLayout === 'horizontal'"
        name="header"
      />
      <slot
        v-else
        name="rail"
      />
    </div>
    <div class="app-aside">
      <slot
        v-if="navLayout === 'vertical'"
        name="header"
      />
      <slot
        v-else
        name="rail"
      />
    </div>
    <div class="app-content">
      <div :class="['app-content-container', { 'full-width': contentLayout === 'full' }]">
        <slot />
      </div>
    </div>
    <div class="app-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
// Self-contained app shell (replaces FeatherAppLayout). A CSS-grid frame with
// header / aside (rail) / content / footer regions and the same slot API. The
// rail-expansion provide/inject machinery FeatherAppLayout carried is omitted:
// nothing in the app injected those keys. Depends on nothing but Vue.
withDefaults(defineProps<{
  contentLayout?: 'center' | 'full'
  navLayout?: 'horizontal' | 'vertical'
}>(), {
  contentLayout: 'center',
  navLayout: 'horizontal'
})
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-areas: "header header" "rail main" "rail footer";
  grid-template-rows: auto minmax(0, 1fr) auto;
  grid-template-columns: auto minmax(0, 1fr);
}
.app-layout.vertical {
  grid-template-areas: "header rail" "header main" "header footer";
}
.app-header {
  grid-area: header;
}
.app-footer {
  grid-area: footer;
}
.app-content {
  grid-area: main;
  display: flex;
  justify-content: center;
  width: 100%;
}
.app-aside {
  grid-area: rail;
}
.app-content > .app-content-container {
  // FeatherDS content-width (75rem); only relevant for the 'center' layout.
  max-width: 75rem;
  width: 100%;
}
.app-content > .app-content-container.full-width {
  max-width: none;
}
</style>

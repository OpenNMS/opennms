  
<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
    </template>

    <Sidebar />

    <router-view />
  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
import { FeatherAppLayout } from '@featherds/app-layout'
import Menubar from './components/Layout/Menubar.vue'
import Sidebar from './components/Layout/Sidebar.vue'
</script>
  
<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";
.full-width {
  padding: 0px !important;
}
body {
  margin: 0px;
}
.logo {
  color: var(--feather-primary-text-on-color) !important;
}
a {
  text-decoration: none;
  color: var(--feather-primary);
}
.flex-container {
  padding: 0;
  margin: 0;
  list-style: none;
  display: flex;
}
.space-between {
  justify-content: space-between;
}
.pointer {
  cursor: pointer !important;
}

// global feather typography classes
.headline1 { @include headline1(); }
.headline2 { @include headline2(); }
.headline3 { @include headline3(); }
.headline4 { @include headline4(); }
.subtitle1 { @include subtitle1(); }
.subtitle2 { @include subtitle2(); }
</style>

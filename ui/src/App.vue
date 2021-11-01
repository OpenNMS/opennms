  
<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
    </template>

    <Sidebar />
    <Spinner />
    <router-view />
  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
import { FeatherAppLayout } from '@featherds/app-layout'
import Menubar from './components/Layout/Menubar.vue'
import Sidebar from './components/Layout/Sidebar.vue'
import Spinner from './components/Common/Spinner.vue'
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
  display: flex;
  justify-content: space-between;
}
.pointer {
  cursor: pointer !important;
}
.feather-secondary {
  background: var(--feather-secondary);
}
.feather-secondary-variant {
  background: var(--feather-secondary-variant);
}
.feather-shade3 {
  background: var(--feather-shade-3);
}
.headline1 {
  @include headline1();
}
.headline2 {
  @include headline2();
}
.headline3 {
  @include headline3();
}
.headline4 {
  @include headline4();
}
.subtitle1 {
  @include subtitle1();
}
.subtitle2 {
  @include subtitle2();
}
.body-small {
  @include body-small();
}
</style>

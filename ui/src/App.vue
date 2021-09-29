  
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
  color: white !important;
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
.space-evenly {
  justify-content: space-evenly;
}
.top-10 {
  margin-top: 10px;
}
.page {
  max-width: 2400px;
  margin: auto;
}
.pointer {
  cursor: pointer !important;
}
</style>

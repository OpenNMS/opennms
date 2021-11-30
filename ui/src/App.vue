  
<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
    </template>

    <template v-slot:rail>
      <NavigationRail :modelValue="true" />
    </template>

    <div class="main-content">
      <Spinner />
      <router-view v-slot="{ Component }">
        <keep-alive include="MapKeepAlive">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>

  </FeatherAppLayout>
</template>
  
<script setup lang="ts">
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import { FeatherAppLayout } from '@featherds/app-layout'
import Menubar from './components/Layout/Menubar.vue'
import Spinner from './components/Common/Spinner.vue'
import NavigationRail from './components/Layout/NavigationRail.vue'

const store = useStore()
onMounted(() => store.dispatch('authModule/getWhoAmI'))
</script>
  
<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";
html {
  overflow: hidden;
}
.main-content {
  margin-left: 218px;
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
.body-small {
  @include body-small();
}
.subtitle1 {
  @include subtitle1();
}
.subtitle2 {
  @include subtitle2();
}

// global feather typography classes
.headline1 { @include headline1(); }
.headline2 { @include headline2(); }
.headline3 { @include headline3(); }
.headline4 { @include headline4(); }
.subtitle1 { @include subtitle1(); }
.subtitle2 { @include subtitle2(); }
</style>

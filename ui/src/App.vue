<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
    </template>

    <div class="main-content">
      <Spinner />
      <Snackbar />
      <router-view v-slot="{ Component }">
        <keep-alive include="MapKeepAlive">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
  </FeatherAppLayout>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { FeatherAppLayout } from '@featherds/app-layout'
import Menubar from './components/Layout/Menubar.vue'
import Spinner from './components/Common/Spinner.vue'
import Snackbar from '@/components/Common/Snackbar.vue'

const store = useStore()

onMounted(() => {
  store.dispatch('authModule/getWhoAmI')
  store.dispatch('infoModule/getInfo')
  store.dispatch('menuModule/getMainMenu')
  store.dispatch('pluginModule/getPlugins')
  store.dispatch('menuModule/getNotificationSummary')
})
</script>

<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";

html {
  overflow-x: hidden;
}
.main-content {
  table {
    width: 100%;
  }
}
a {
  text-decoration: none;
  color: var($clickable-normal);
}
.pointer {
  cursor: pointer !important;
}

// global feather typography classes
.headline3 {
  @include headline3;
}
.headline4 {
  @include headline4;
}
.subtitle1 {
  @include subtitle1;
}
.subtitle2 {
  @include subtitle2;
}
</style>


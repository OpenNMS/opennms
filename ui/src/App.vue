<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
    </template>

    <template v-slot:rail>
      <NavigationRail :modelValue="navRailOpen" />
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
import NavigationRail from './components/Layout/NavigationRail.vue'

const store = useStore()
const navRailOpen = computed(() => store.state.appModule.navRailOpen)
const contentMargin = computed(() => navRailOpen.value ? '218px' : '0px')
const ease = computed(() => navRailOpen.value ? '10ms' : '80ms')
const maxWidth = computed(() => navRailOpen.value ? '223px' : '0px')
onMounted(() => {
  store.dispatch('authModule/getWhoAmI')
  store.dispatch('infoModule/getInfo')
  store.dispatch('pluginModule/getPlugins')
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
  margin-left: v-bind(contentMargin);
  transition: margin-left 0.28s ease-in-out v-bind(ease);
  max-width: calc(100% - v-bind(maxWidth));

  table {
    width: 100%;
  }
}
a {
  text-decoration: none;
  color: var($primary);
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


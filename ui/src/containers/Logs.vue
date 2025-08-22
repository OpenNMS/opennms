<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <div class="feather-row">
          <div class="feather-col-3">
            <Logs />
          </div>
          <div :class="`feather-col-9`">
            <Editor />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Editor from '@/components/Logs/Editor.vue'
import Logs from '@/components/Logs/Logs.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useLogStore } from '@/stores/logStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const logStore = useLogStore()
const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Logs', to: '#', position: 'last' }
  ]
})

onMounted(() => logStore.getLogs())
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/themes/variables";

.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
  position: relative;
}
.feather-row {
  flex-wrap: nowrap;
}
</style>

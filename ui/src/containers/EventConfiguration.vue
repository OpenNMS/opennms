<template>
  <div class="event-config">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage Event Configurations</h1>
      </div>
      <div class="action">
        <FeatherButton
          primary
          @click="store.showCreateEventConfigSourceDialog"
        >
          Create Event Source
        </FeatherButton>
      </div>
    </div>
    <div class="tabs">
      <EventConfigTabContainer />
    </div>
  </div>
  <CreateEventConfigurationDialog />
</template>

<script lang="ts" setup>
import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'

const store = useEventConfigStore()
const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Event Configurations', to: '#', position: 'last' }
  ]
})
</script>

<style lang="scss" scoped>
.event-config {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
}
</style>

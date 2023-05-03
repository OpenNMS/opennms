<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <div class="usage-stats-container">
          <div class="table-container">
            <div class="title-container">
              <span class="title">Usage Statistics Sharing</span>
            </div>
            <UsageStatisticsHeader />
            <div class="spacer-medium"></div>
            <UsageStatisticsTable />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import UsageStatisticsHeader from '@/components/UsageStatistics/UsageStatisticsHeader.vue'
import UsageStatisticsTable from '@/components/UsageStatistics/UsageStatisticsTable.vue'
import { BreadCrumb } from '@/types'

const store = useStore()
const homeUrl = computed<string>(() => store.state.menuModule.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Usage Statistics Collection', to: '#', position: 'last' }
  ]
})

// TODO: Spinner until we get data
onMounted(async () => {
  store.dispatch('usageStatisticsModule/getStatus')
  store.dispatch('usageStatisticsModule/getMetadata')
  store.dispatch('usageStatisticsModule/getStatistics')
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.card {
  background: var($surface);
  padding: 0px 20px 20px 20px;

  .usage-stats-container {
    display: flex;

    .table-container {
      width: 35rem;
      flex: auto;

      .title-container {
        display: flex;
        justify-content: space-between;

        .title {
          @include headline1;
          margin: 24px 0px 24px 19px;
          display: block;
        }
      }
    }
  }
}
.spacer-medium {
  margin-bottom: 0.25rem;
}
</style>

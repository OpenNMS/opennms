<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12">
        <div class="dcb-container">
          <div class="table-container">
            <p class="title">Device Configuration</p>
            <DCBTable />
          </div>
          <div class="filters-container">
            <DCBGroupFilters />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import DCBTable from '@/components/Device/DCBTable.vue'
import DCBGroupFilters from '@/components/Device/DCBGroupFilters.vue'

const store = useStore()

onMounted(() => store.dispatch('deviceModule/getDeviceConfigBackups'))
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

@mixin status-bar($color) {
  background: $color;
  background: linear-gradient(90deg, $color 1%, rgba(255, 255, 255, 0) 9%);
}
::v-deep .Success {
  @include status-bar(var($success));
}
::v-deep .Failed {
  @include status-bar(var($error));
}
::v-deep .InProgress {
  @include status-bar(var($warning));
}
::v-deep .Paused {
  @include status-bar(var($primary-variant));
}
::v-deep .NoBackup {
  @include status-bar(var($secondary-variant));
}

.card {
  @include elevation(2);
  background: var($surface);
  padding: 10px 20px 20px 20px;

  .dcb-container {
    display: flex;

    .table-container {
      width: 35rem;
      flex: auto;
      .title {
        @include headline4;
        margin-left: 19px;
      }
    }

    .filters-container {
      width: 15rem;
    }
  }
}
</style>

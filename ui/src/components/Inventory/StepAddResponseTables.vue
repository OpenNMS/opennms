<template>
  <div class="feather-row" v-if="ipRangeResponses.length" v-for="ipRangeResponse in ipRangeResponses" :key="ipRangeResponse.location">
    <div class="feather-col-12">
      <div class="title">{{ ipRangeResponses.location }}</div>
      <table class="tl1 tl2 tl3" summary="IP Range Responses">
        <thead>
          <tr>
            <th scope="col">Hostname</th>
            <th scope="col">IP Address</th>
            <th scope="col">RTT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="scanResult in ipRangeResponse.scanResults" :key="scanResult.hostname">
            <td>{{ scanResult.hostname }}</td>
            <td>{{ scanResult.ipAddress }}</td>
            <td>{{ scanResult.rtt }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const ipRangeResponses = computed(() => store.state.inventoryModule.ipRangeResponses)
</script> 

<style lang="scss">
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/typography";
.title {
  @include headline3();
}
</style>

<template>
  <TableCard class="mib-groups-table-container">
    <h1>MIB Groups</h1>
  </TableCard>
</template>

<script setup lang="ts">
import TableCard from '../Common/TableCard.vue'
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.mib-groups-table-container {
    margin-top: 10px;
    padding: 25px;
}
</style>

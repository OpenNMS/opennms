<template>
  <TableCard class="resource-types-table-container">
    <h1>Resource Types</h1>
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

.resource-types-table-container {
  margin-top: 10px;
  padding: 25px;
}
</style>


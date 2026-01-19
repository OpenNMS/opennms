<template>
  <TableCard class="system-definitions-table-container">
    <h1>System Definitions</h1>
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

.system-definitions-table-container {
    margin-top: 10px;
    padding: 25px;
}
</style>

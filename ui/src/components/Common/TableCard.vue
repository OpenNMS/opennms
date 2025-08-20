<template>
  <div class="table-card">
    <slot></slot>
  </div>
</template>

<style lang="scss" scoped>
@use "@featherds/styles/themes/variables";

.table-card {
  background: var(variables.$surface);
  width: 100%;
  padding: 25px 0px;
  border-radius: 5px;
  display: inline-block;
}
</style>

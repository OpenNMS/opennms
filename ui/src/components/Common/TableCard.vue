<template>
  <div class="table-card">
    <slot></slot>
  </div>
</template>

<style lang="scss" scoped>
.table-card {
  background: var(--p-content-background);
  width: 100%;
  padding: 25px 0px;
  border-radius: 5px;
  display: inline-block;
}
</style>

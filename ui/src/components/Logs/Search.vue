<template>
  <div class="search-bar">
    <div class="search">
      <FeatherInput label="Search logs" v-model="searchValue" @update:modelValue="search" />
    </div>
  </div>
  <hr />
</template>

<script setup lang="ts">
import { useLogStore } from '@/stores/logStore'
import { FeatherInput } from '@featherds/input'

const logStore = useLogStore()
const searchValue = ref('')
const search = () => logStore.setSearchValue(searchValue.value)
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  .search {
    width: 100%;
    .feather-input-container {
      padding: 0px;
      margin-bottom: -26px;
    }
  }
}
</style>

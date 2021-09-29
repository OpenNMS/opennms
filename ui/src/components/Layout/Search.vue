<template>
  <Dropdown
    ref="childRef"
    v-model="searchStr"
    :options="results"
    optionLabel="label"
    optionGroupLabel="label"
    optionGroupChildren="results"
    placeholder="Search"
    type="text"
    class="search"
    @input="search"
    @change="selectItem"
    :loading="loading"
    editable
  >
    <template #optiongroup="slotProps">
      <div>
        <div>{{ slotProps.option.label }}</div>
      </div>
    </template>
  </Dropdown>
</template>
  
<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { debounce } from 'lodash'
  import { useStore } from 'vuex'
  import Dropdown from 'primevue/dropdown'
  import { useRouter } from 'vue-router'

  const router = useRouter()
  const store = useStore()

  // data
  const searchStr = ref()
  const loading = ref(false)
  const childRef = ref()

  // methods
  const openDropdown = () => childRef.value.show()
  const selectItem = ({ value }: { value: { url: string } }) => {
    // parse selected item url and redirect
    const path = value.url.split('?')[1].split('=')
    router.push(`/${path[0]}/${path[1]}`)
  }
  const search = debounce(async (e: any) => {
    loading.value = true
    await store.dispatch('searchModule/search', e.target.value)
    loading.value = false
    openDropdown()
  }, 600)

  // computed
  const results = computed(() => store.state.searchModule.searchResults)
</script>
  
<style lang="scss" scoped>
.search {
  margin-right: 20px;
}
</style>

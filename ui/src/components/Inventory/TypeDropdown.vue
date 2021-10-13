<template>
  <FeatherSelect 
    @change="$emit('set-type', type)" 
    v-model="type" 
    :options="types" 
    text-prop="name"
    class="types-dropdown"
    label="type"/>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import { FeatherSelect } from '@featherds/select'

export default defineComponent({
  components: {
    FeatherSelect
  },
  emits: ['set-type'],
  setup() {
    const types = [
      {id: 1, name: 'VMware vSphere'}, 
      {id: 2, name: 'OpenDaylight'}
    ]

    const type = ref(types[0])

    return {
      type,
      types
    }
  }
})
</script>

<style scoped lang="scss">
  .types-dropdown {
    width: 100%;
  }
</style>


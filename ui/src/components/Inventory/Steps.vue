<template>
  <div class="steps-container">
    <PrimeSteps :model="items" :readonly="true" />
  </div>
  <router-view
    v-slot="{ Component }"
    @prevPage="prevPage($event)"
    @nextPage="nextPage($event)"
    @complete="complete"
  >
    <keep-alive>
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script lang="ts">
import { ref, defineComponent, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import PrimeSteps from './PrimeSteps.vue'

export default defineComponent({
  components: {
    PrimeSteps
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const items = ref([
      {
        label: 'Add Nodes',
        to: '/inventory'
      },
      {
        label: 'Configure Services',
        to: '/inventory/configure',
      },
      {
        label: 'Schedule',
        to: '/inventory/schedule',
      }
    ])

    const nextPage = (event: any) => {
      router.push(items.value[event.pageIndex + 1].to)
    }
    const prevPage = (event: any) => {
      router.push(items.value[event.pageIndex - 1].to)
    }

    const completionType = computed(() => store.state.inventoryModule.showCompleteButton)

    const complete = async () => {
      if (completionType.value === 'later') {
        const success = await store.dispatch('inventoryModule/scheduleProvision')
        if (success) router.push('/')
      } else {
        router.push('/')
      }
    }

    return {
      items,
      nextPage,
      prevPage,
      complete
    }
  }
})
</script>

<style scoped lang="scss">
.steps-container {
  max-width: 500px;
  margin-bottom: 20px;
}
</style>

<template>
  <div class="node-lookup">
    <OnmsMessage
      :severity="state === 'error' ? 'error' : 'warn'"
      data-test="node-lookup-message"
    >
      <strong>{{ heading }}</strong>
      <div>{{ detail }}</div>
    </OnmsMessage>
    <!-- Replaces the legacy error page's "search again" form, which goes away with node.jsp. -->
    <form
      class="node-lookup-form"
      @submit.prevent="lookUp"
    >
      <FormField
        label="Get details for node"
        for="node-lookup-input"
      >
        <div class="node-lookup-row">
          <OnmsInputText
            id="node-lookup-input"
            v-model="entry"
            placeholder="Node ID or foreignSource:foreignId"
            data-test="node-lookup-input"
          />
          <OnmsButton
            label="Go"
            type="submit"
            data-test="node-lookup-button"
          />
        </div>
      </FormField>
    </form>
    <p>
      Or browse the <router-link
        :to="{ name: 'Nodes' }"
        data-test="node-lookup-list-link"
      >node list</router-link>.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { OnmsButton, OnmsInputText, OnmsMessage } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { nodeDetailsRoute } from './nodeDetailsRoute'

const props = defineProps<{
  state: 'invalid' | 'notFound' | 'error'
  value: string
}>()

const router = useRouter()
const entry = ref('')

const heading = computed(() => {
  switch (props.state) {
    case 'invalid':
      return 'Invalid Node ID'
    case 'notFound':
      return 'Node Not Found'
  }
  return 'Node Lookup Failed'
})

const detail = computed(() => {
  switch (props.state) {
    case 'invalid':
      return `'${props.value}' is not a valid node ID. Use a node ID or foreignSource:foreignId.`
    case 'notFound':
      return `No node '${props.value}' exists.`
  }
  return `Node '${props.value}' could not be loaded. Try again later.`
})

const lookUp = () => {
  const value = entry.value.trim()
  if (!value) {
    return
  }
  router.push(nodeDetailsRoute(value))
}
</script>

<style lang="scss" scoped>
.node-lookup {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 40rem;
}

.node-lookup-row {
  display: flex;
  gap: 0.5rem;
}
</style>

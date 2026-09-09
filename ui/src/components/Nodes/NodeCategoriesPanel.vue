<template>
  <NodeDetailsPanel title="Surveillance Category Memberships">
    <template v-if="adminRole" #actions>
      <OnmsIconButton
        aria-label="Edit"
        tooltip="Edit"
        data-test="edit-button"
        :icon="IconEdit"
        @click="onEditClick"
      />
    </template>
    <div class="onms-row" v-if="props.node?.categories?.length === 0">
      <div class="onms-col-12">
        <span class="attribute-value">This node is not a member of any categories.</span>
      </div>
    </div>
    <div class="onms-row" v-for="item in props.node?.categories" :key="item.id">
      <div class="onms-col-12">
        <span class="attribute-value">{{ item.name }}</span>
      </div>
    </div>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconEdit from '@opennms/onms-ui/icons/action/Edit.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import useRole from '@/composables/useRole'
import { Node } from '@/types'

const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const { adminRole } = useRole()

const onEditClick = () => {
  if (adminRole.value) {
    const editUrl = `${props.baseHref}admin/categories.htm?edit&node=${props.node.id}`
    window.location.assign(editUrl)
  }
}
</script>

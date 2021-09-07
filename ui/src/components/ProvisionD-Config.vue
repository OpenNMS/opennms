<template>
    <TabMenu :model="TabMenuItem" v-model:activeIndex="active"/>
    <router-view></router-view>
</template>
<script setup lang="ts">

import TabMenu from 'primevue/tabmenu';
import { ref } from 'vue';

const active = ref(0);

const TabMenuItem = ref([
    { label: 'Requisition Definitions', to: '/reqDefinition' },
    { label: 'Thread Pools', to: '/threadPools' },
])
</script>
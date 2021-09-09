<template>
    <p>
        <Button :label="buttonLabel" primeClass="rounded" @click="onBackClick"></Button>
    </p>
    <p>
        Edit Node ID ::
        <strong>{{ $route.params.id }}</strong>
    </p>
</template>

<script setup lang="ts">

import { ref } from 'vue'
import Button from "../Button.vue";
import router from '@/router';

const buttonLabel = ref('Back To Node Table')

//Routing Back to Table
const onBackClick = () => {
    router.push("/provisionConfig/reqDefinition");
}

</script>
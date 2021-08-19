<template>
            <InputText
              cmclass="sm"
              type="text"
              placeholder="Enter text"
              v-model="show"
            />
            <p>Input Text :: {{show}}</p>

</template>

<script setup lang="ts">

import {ref} from 'vue'
import InputText from '../InputText.vue'

const show = ref('');

</script>
<template>
    <div @click="() => itemClicked(item)">
        <div class="label-wrapper">
            <div :style="{ paddingRight: '20px' }">
                <div :style="{ display: 'flex', alignItems: 'center' }">
                    <font-awesome-icon v-if="item.icon" :icon="item.icon" :style="{ paddingRight: '6px' }" />
                    <div>
                        {{ item.label }}
                    </div>
                </div>
                <div class="short" v-if="item.matches">
                    <div v-for="match, matchKey in item.matches" :key="matchKey">
                        {{ match.label }}: {{ match.value }}
                    </div>
                </div>
            </div>
            <span :class="iconClass">
                <FeatherIcon :icon="SubdirectoryArrowLeft"></FeatherIcon>
            </span>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import SubdirectoryArrowLeft from '@featherds/icon/navigation/SubdirectoryArrowLeft'
import { SearchResultItem } from '@/types'

defineProps({
  item: { type: Object as PropType<SearchResultItem>, default: () => { return } },
  itemClicked: { type: Function as PropType<(item: SearchResultItem) => void>, default: () => { return } },
  iconClass: { type: String, default: '' }
})
</script>
<style lang="scss" scoped>
.label-wrapper {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: '#e9ecef';
    color: '#495057';
    font-weight: 500;
    white-space: break-spaces;
    padding: 0 3px;

    .visible {
        display: block;
    }

    .short {
        font-size: 0.8em;
        margin-left: 3px;
    }

    span {
        opacity: 0;
    }

    &:hover {
        span {
            opacity: 1;
        }
    }
}
</style>
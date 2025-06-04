<template>
    <button class="search-result-button" @click="() => itemClicked(item)" :tabIndex="0">
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
    </button>
</template>
<script lang="ts" setup>
import { PropType } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import SubdirectoryArrowLeft from '@featherds/icon/navigation/SubdirectoryArrowLeft'
import { SearchResultItem } from '@/types'

defineProps({
  // TODO: SearchResult:item in components/Layout/Search.vue should be a SearchResultItem but may not be,
  // that's why we are using "| any" here. Need to make sure we are passing correct objects and types
  item: { type: Object as PropType<SearchResultItem | any>, default: () => { return } },
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
/** 
    These styles are a bit wild, but they were take from a diff done
    between this component as a div vs a button. Initially coded using the div
    we had to move to the button to get better accessibility. But then the styles were off.
    These styles below make the button in the header look significantly better.
 */
.search-result-button {
    background: transparent;
    border:none;
    appearance: none;
    block-size:24px;
    caret-color: rgb(10,12,27,0.7);
    color: rgba(10, 12, 27, 0.7);
    column-rule-color: rgba(10, 12, 27, 0.7);
    cursor: pointer;
    display:block;
    font-family: OpenSans, Helvetica, Arial, sans-serif;
    font-size:14px;
    height:24px;
    inline-size:266px;
    letter-spacing:0.25px;
    line-height:24px;
    outline-color:rgba(10, 12, 27, 0.7);
    padding:0;
    perspective-origin: 133px 12px;
    text-align:left;
    text-decoration-color: rgba(10, 12, 27, 0.7);
    text-emphasis-color: rgba(10, 12, 27, 0.7);
    transform-origin: 133px 12px;
    unicode-bidi: isolate;
    user-select: auto;
}
</style>

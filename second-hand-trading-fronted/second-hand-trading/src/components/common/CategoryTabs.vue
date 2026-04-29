<template>
    <div class="category-tabs-container">
        <el-tabs :model-value="modelValue" @tab-click="handleTabClick" class="category-tabs">
            <el-tab-pane
                v-for="item in tabs"
                :key="item.name"
                :label="item.label"
                :name="item.name">
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>
import { IDLE_CATEGORY_TABS } from '@/constants/idle-category';

const props = defineProps({
    modelValue: {
        type: String,
        default: '0'
    }
});

const tabs = IDLE_CATEGORY_TABS;

const emit = defineEmits(['update:modelValue', 'change']);

const handleTabClick = (tab) => {
    const value = String(tab.props.name);
    emit('update:modelValue', value);
    emit('change', value);
};
</script>

<style scoped>
.category-tabs-container {
    background: transparent;
    box-shadow: none;
}

:deep(.category-tabs .el-tabs__item) {
    font-size: 16px;
    color: var(--text-main);
}

:deep(.category-tabs .el-tabs__item:hover) {
    color: var(--primary-color);
    transform: none;
}

:deep(.category-tabs .el-tabs__item.is-active) {
    color: var(--primary-color);
    font-weight: 600;
    transform: none;
}

:deep(.category-tabs .el-tabs__nav-wrap::after) {
    background-color: transparent;
}

:deep(.category-tabs .el-tabs__active-bar) {
    background-color: var(--primary-color);
    height: 3px;
}
</style>

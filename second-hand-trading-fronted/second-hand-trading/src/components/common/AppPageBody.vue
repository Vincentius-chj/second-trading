<template>
    <div class="main-container">
        <div class="main-content">
            <slot></slot>
        </div>
    </div>
</template>

<script setup>
// 纯展示组件，无逻辑
</script>

<style scoped>
    .main-container{
        /* margin-top: 67px; */
        /* display: flex; */
        /* justify-content: center; */
        min-width: 1000px; /* 确保容器本身也有最小宽度，传递给父级 */
    }
    .main-content{
        width: 100%;
        max-width: 1000px;
        min-width: 1000px;
        background-color: #ffffff;
        min-height: 90vh;
        margin: 0 auto; /* 使用 margin auto 进行居中 */
    }
</style>
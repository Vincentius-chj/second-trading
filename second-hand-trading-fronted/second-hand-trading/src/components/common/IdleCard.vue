<template>
    <div class="idle-card" @click="toDetails(idle)">
        <el-image
            style="width: 100%; height: 200px;"
            :src="idle.imgUrl"
            fit="cover">
            <template #error>
                <div class="image-slot">
                    <i class="el-icon-picture-outline">无图</i>
                </div>
            </template>
        </el-image>
        <div class="idle-info">
            <div class="idle-name-price">
                <div class="idle-name">{{ idle.idleName }}</div>
                <div class="idle-price">¥{{ idle.idlePrice }}</div>
            </div>
            <div class="user-info-date">
                <div class="user-info">
                    <el-avatar :size="24" :src="idle.user && idle.user.avatar ? idle.user.avatar : ''" />
                    <span class="username">{{ idle.user && idle.user.nickname ? idle.user.nickname : '未知用户' }}</span>
                </div>
                <div class="date">{{ idle.timeStr }}</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

// 定义组件接收的属性
defineProps({
    idle: {
        type: Object,
        required: true
    }
});

// 获取路由器实例
const router = useRouter();

// 跳转到详情页
const toDetails = (idle) => {
    router.push({ path: '/details', query: { id: idle.id } });
};
</script>

<style scoped>
.idle-card {
    height: 250px;
    background: var(--card-bg);
    border-radius: var(--radius-main);
    overflow: hidden;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F1F2F3;
    display: flex;
    flex-direction: column;
}

.idle-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-hover);
}

.idle-info {
    padding: 10px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.idle-name-price {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}

.idle-name {
    flex: 1;
    font-size: 14px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-left: 1px;
    margin-right: 10px;
}

.idle-price {
    font-size: 16px;
    color: #ff69b4; /* 粉色 */
    font-weight: bold;
    white-space: nowrap;
    min-width: fit-content;
}

.user-info-date {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #999;
}

.user-info {
    display: flex;
    align-items: center;
}

.username {
    margin-left: 5px;
}

.date {
    white-space: nowrap;
    min-width: fit-content;
}

.image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    background: #f5f7fa;
    color: #909399;
}
</style>
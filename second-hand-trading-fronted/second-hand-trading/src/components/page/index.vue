<template>
    <div class="index-container">
        <app-head></app-head>
        <app-body>
            <div class="main-content">
                <div class="tabs-container">
                    <category-tabs v-model="labelName" @change="handleClick"></category-tabs>
                </div>
                
                <div
                    class="goods-list-container"
                    v-loading="listLoading"
                    element-loading-text="搜索中..."
                    element-loading-background="rgba(255, 255, 255, 0.7)">
                    <el-row :gutter="30">
                        <el-col :span="6" v-for="idle in idleList" :key="idle.id">
                            <IdleCard :idle="idle" />
                        </el-col>
                    </el-row>
                    
                    <!-- 空状态 -->
                    <div v-if="idleList.length === 0" class="empty-state">
                        <el-icon class="empty-icon"><Box /></el-icon>
                        <p>暂无相关闲置物品</p>
                    </div>
                </div>

                <div class="pagination-container">
                    <el-pagination
                            background
                            @current-change="handleCurrentChange"
                            v-model="currentPage"
                            :page-size="8"
                            layout="prev, pager, next, jumper"
                            :total="totalItem">
                    </el-pagination>
                </div>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script setup>
    import { ref, watch, getCurrentInstance } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import AppHead from '../common/AppHeader.vue';
    import AppBody from '../common/AppPageBody.vue'
    import AppFoot from '../common/AppFoot.vue'
    import IdleCard from '../common/IdleCard.vue'
    import CategoryTabs from '../common/CategoryTabs.vue'
    import { Box, Picture, Location } from '@element-plus/icons-vue'
    
    // 获取当前实例以访问全局属性
    const instance = getCurrentInstance();
    const $api = instance.appContext.config.globalProperties.$api;
    const $loading = instance.appContext.config.globalProperties.$loading;
    
    // 路由相关
    const route = useRoute();
    const router = useRouter();
    
    // 响应式数据
    const labelName = ref('0');
    const idleList = ref([]);
    const listLoading = ref(false);
    const currentPage = ref(1);
    const totalItem = ref(0);
    const searchValue = ref('');
    
    // 格式化时间
    const formatTime = (timeStr) => {
        if (!timeStr) return '';
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now - date;
        
        // 小于1天
        if (diff < 86400000) {
            if (diff < 3600000) {
                return Math.max(1, Math.floor(diff / 60000)) + '分钟前';
            }
            return Math.floor(diff / 3600000) + '小时前';
        }
        // 小于3天
        if (diff < 259200000) {
            return Math.floor(diff / 86400000) + '天前';
        }
        return timeStr.substring(0, 10);
    }

    // 处理图片URL，确保图片能正确显示
    const getImageUrl = (url) => {
        if (!url) return '';
        // 如果已经是完整URL，直接返回
        if (url.startsWith('http://') || url.startsWith('https://')) {
            return url;
        }
        // 如果是相对路径，添加基础路径
        if (url.startsWith('/')) {
            return url;
        }
        // 其他情况直接返回
        return url;
    };
    
    // 方法定义
    const normalizeIdleList = (list) => {
        const normalized = Array.isArray(list) ? list : [];
        for (let i = 0; i < normalized.length; i++) {
            normalized[i].timeStr = normalized[i].releaseTime;
            let pictureList = JSON.parse(normalized[i].pictureList);
            normalized[i].imgUrl = pictureList.length > 0 ? getImageUrl(pictureList[0]) : '';
        }
        return normalized;
    };

    const matchKeyword = (item, keyword) => {
        if (!keyword || keyword.trim() === '') {
            return true;
        }
        const q = keyword.trim().toLowerCase();
        const name = (item.idleName || '').toLowerCase();
        const details = (item.idleDetails || '').toLowerCase();
        return name.includes(q) || details.includes(q);
    };

    const findIdleTiem = (page) => {
        const keyword = searchValue.value || '';
        listLoading.value = true;

        if (labelName.value > 0) {
            $api.findIdleTiemByLable({ idleLabel: labelName.value, page: 1, nums: 500 }).then(res => {
                let list = normalizeIdleList(res.data.list || []);
                list = list.filter(item => matchKeyword(item, keyword));
                totalItem.value = list.length;
                const begin = (page - 1) * 8;
                idleList.value = list.slice(begin, begin + 8);
            }).catch(e => {
                console.log(e)
            }).finally(()=>{
                listLoading.value = false;
                window.scrollTo({ top: 0, behavior: 'smooth' });
            })
            return;
        }

        $api.findIdleTiem({ page: page, nums: 8, findValue: keyword }).then(res => {
            let list = normalizeIdleList(res.data.list || []);
            idleList.value = list;
            totalItem.value = res.data.count;
        }).catch(e => {
            console.log(e)
        }).finally(()=>{
            listLoading.value = false;
            // 回到顶部
            window.scrollTo({ top: 0, behavior: 'smooth' });
        })
    };
    
    const handleClick = (newLabel) => {
        labelName.value = String(newLabel);
        router.replace({query: {page: 1, labelName: labelName.value, searchValue: searchValue.value || ''}});
    };
    
    const handleCurrentChange = (val) => {
        currentPage.value = val;
        router.replace({query: {page: val, labelName: labelName.value, searchValue: searchValue.value || ''}});
    };
    
    const toDetails = (idle) => {
        router.push({path: '/details', query: {id: idle.id}});
    };
    
    // 明确监听 query 字段，保证点击搜索后一定触发请求
    watch(
        [() => route.query.page, () => route.query.labelName, () => route.query.searchValue],
        ([page, label, keyword]) => {
            const nextPage = parseInt(page) > 0 ? parseInt(page) : 1;
            const nextLabel = label ? String(label) : '0';
            const nextKeyword = keyword ? String(keyword) : '';
            currentPage.value = nextPage;
            labelName.value = nextLabel;
            searchValue.value = nextKeyword;
            findIdleTiem(nextPage);
        },
        { immediate: true }
    );
    
    defineExpose({
        labelName,
        idleList,
        currentPage,
        totalItem,
        handleClick,
        handleCurrentChange,
        toDetails
    });
</script>

<style scoped>
    .index-container {
        min-height: 100vh;
        background-color: var(--bg-color);
    }

    .main-content {
        max-width: 1000px;
        margin: 0 auto;
        padding: 20px;
    }

    .tabs-container {
        background: transparent;
        padding: 0;
        margin-bottom: 20px;
        box-shadow: none;
    }
    
    .goods-list-container {
        min-height: 600px;
    }

    /* B站风格卡片 */
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

    .img-wrapper {
        width: 100%;
        padding-top: 75%; /* 4:3 比例 - 更适合展示图片 */
        position: relative;
        overflow: hidden;
        background-color: #f4f5f7;
    }

    .goods-img {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .hover-overlay {
        display: none; /* B站风格通常没有这个遮罩 */
    }

    .card-body {
        padding: 8px 10px 10px 10px !important;
        flex: 1;
        display: flex !important;
        flex-direction: column !important;
        justify-content: flex-start !important;
        gap: 0 !important;
        min-height: 100px;
    }

    .idle-title {
        font-size: 15px !important;
        color: var(--text-main) !important;
        line-height: 16px !important;
        margin: 0 !important;
        padding: 0 !important;
        overflow: hidden !important;
        font-weight: 500 !important;
        transition: color 0.2s !important;
    }
    
    .title-and-price {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 32px;
    }
    
    .title-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 8px;
    }
    
    .price-wrapper {
        color: var(--primary-color) !important;
        font-weight: 600 !important;
        display: inline-flex !important;
        align-items: center !important;
        line-height: 1 !important;
        white-space: nowrap;
        flex-shrink: 0;
    }
    
    .idle-card:hover .idle-title {
        color: var(--primary-color) !important;
    }

    .idle-info {
        display: flex !important;
        justify-content: space-between !important;
        align-items: center !important;
        margin: 0 !important;
        margin-top: 6px !important;
        padding: 0 !important;
    }

    .currency {
        font-size: 12px !important;
        margin-right: 2px !important;
    }

    .price {
        font-size: 18px !important;
        line-height: 1 !important;
    }

    .idle-place {
        display: none; /* 简化显示 */
    }

    .card-footer {
        display: flex !important;
        align-items: center !important;
        justify-content: space-between !important;
        margin-top: 4px !important;
        padding-top: 0 !important;
        border-top: none !important;
        color: var(--text-secondary) !important;
    }

    .user-info {
        display: flex;
        align-items: center;
        gap: 6px;
        flex: 1;
    }

    .user-avatar {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        border: none;
    }
    
    /* 覆盖 Element UI avatar size */
    :deep(.user-avatar .el-avatar) {
        width: 20px !important;
        height: 20px !important;
        line-height: 20px !important;
    }

    .user-nickname {
        font-size: 12px;
        color: var(--text-secondary);
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .idle-time {
        font-size: 12px;
        color: var(--text-secondary);
    }

    .pagination-container {
        display: flex;
        justify-content: center;
        margin-top: -32px;
        padding-bottom: 40px;
    }
    
    /* 分页器样式适配 */
    :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
        background-color: var(--primary-color);
    }
    
    :deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
        color: var(--primary-color);
    }

    .empty-state {
        text-align: center;
        padding: 100px 0;
        color: var(--text-secondary);
    }

    .empty-icon {
        font-size: 60px;
        margin-bottom: 20px;
        color: #dcdfe6;
    }
</style>
<template>
    <div>
        <app-head :searchInput="searchValue"></app-head>
        <app-body>
            <div style="min-height: 85vh;">
                <div class="tabs-container">
                    <category-tabs v-model="labelName" @change="handleLabelChange"></category-tabs>
                </div>
                <div
                    style="margin: 0 20px;padding-top: 20px;"
                    v-loading="listLoading"
                    element-loading-text="搜索中..."
                    element-loading-background="rgba(255, 255, 255, 0.7)">
                    <div style="text-align: center;color: #555555;padding: 20px;" v-if="idleList.length===0">暂无匹配的闲置物品</div>
                    <el-row :gutter="30">
                        <el-col :span="6" v-for="idle in idleList" :key="idle.id">
                            <IdleCard :idle="idle" />
                        </el-col>
                    </el-row>
                </div>
                <div class="fenye">
                    <el-pagination
                            background
                            @current-change="handleCurrentChange"
                            :current-page="currentPage"
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
import { ref, onMounted, watch, getCurrentInstance } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import AppHead from '../common/AppHeader.vue';
import AppBody from '../common/AppPageBody.vue';
import AppFoot from '../common/AppFoot.vue';
import IdleCard from '../common/IdleCard.vue';
import CategoryTabs from '../common/CategoryTabs.vue';
import { Location } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const router = useRouter();
const route = useRoute();

const idleList = ref([]);
const listLoading = ref(false);
const currentPage = ref(1);
const searchValue = ref('');
const totalItem = ref(1);
const labelName = ref('0');

const normalizeIdleList = (list) => {
    if (!Array.isArray(list)) {
        return [];
    }
    for (let i = 0; i < list.length; i++) {
        list[i].timeStr = list[i].releaseTime.substring(0, 10) + " " + list[i].releaseTime.substring(11, 19);
        let pictureList = JSON.parse(list[i].pictureList);
        list[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
    }
    return list;
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

const findIdleTiem = (page, findValue, label) => {
    listLoading.value = true;
    // 分类 + 关键词组合搜索：先按分类获取较大结果集，再本地关键词过滤和分页
    if (label && String(label) !== '0') {
        $api.findIdleTiemByLable({
            idleLabel: label,
            page: 1,
            nums: 500
        }).then(res => {
            let list = normalizeIdleList(res.data.list || []);
            list = list.filter(item => matchKeyword(item, findValue));
            totalItem.value = list.length;
            const begin = (page - 1) * 8;
            idleList.value = list.slice(begin, begin + 8);
        }).catch(e => {
            console.log(e);
        }).finally(() => {
            listLoading.value = false;
        });
        return;
    }

    // 全部分类时使用后端关键词分页
    $api.findIdleTiem({
        page: page,
        nums: 8,
        findValue: findValue
    }).then(res => {
        let list = normalizeIdleList(res.data.list || []);
        idleList.value = list;
        totalItem.value = res.data.count;
    }).catch(e => {
        console.log(e);
    }).finally(() => {
        listLoading.value = false;
    });
};

const handleCurrentChange = (val) => {
    currentPage.value = val;
    findIdleTiem(val, searchValue.value, labelName.value);
    router.replace({query: {page: val, searchValue: searchValue.value, labelName: labelName.value}});
};

const handleLabelChange = (newLabel) => {
    labelName.value = String(newLabel);
    currentPage.value = 1;
    findIdleTiem(1, searchValue.value, labelName.value);
    router.replace({query: {page: 1, searchValue: searchValue.value, labelName: labelName.value}});
};

const toDetails = (idle) => {
    router.push({path: '/details', query: {id: idle.id}});
};

// 监听路由变化
watch(route, (to, from) => {
    const page = parseInt(to.query.page) > 0 ? parseInt(to.query.page) : 1;
    const nextSearchValue = to.query.searchValue || '';
    const nextLabelName = to.query.labelName ? String(to.query.labelName) : '0';
    currentPage.value = page;
    searchValue.value = nextSearchValue;
    labelName.value = nextLabelName;
    findIdleTiem(page, nextSearchValue, nextLabelName);
});

onMounted(() => {
    const page = parseInt(route.query.page) > 0 ? parseInt(route.query.page) : 1;
    const nextSearchValue = route.query.searchValue || '';
    const nextLabelName = route.query.labelName ? String(route.query.labelName) : '0';
    currentPage.value = page;
    searchValue.value = nextSearchValue;
    labelName.value = nextLabelName;
    findIdleTiem(page, nextSearchValue, nextLabelName);
});
</script>

<style scoped>
    .tabs-container {
        background: transparent;
        padding: 0 20px;
        margin-bottom: 4px;
    }

    .fenye {
        display: flex;
        justify-content: center;
        height: 44px;
        margin-top: -58px;
        align-items: center;
    }
</style>

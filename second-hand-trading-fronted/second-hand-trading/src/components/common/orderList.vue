<template>
    <div class="main-border">
        <div class="header-wrapper">
            <div class="tabs-and-button">
                <div class="custom-tabs">
                    <div class="tab-item" :class="{active: true}">
                        订单管理
                        <div class="active-bar"></div>
                    </div>
                </div>
            </div>
            
            <div class="header-actions">
                <div class="search-box">
                    <el-input 
                        placeholder="订单号" 
                        v-model="searchOrderNumber" 
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-input 
                        placeholder="闲置名称" 
                        v-model="searchGoodsName" 
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-select
                        v-model="amountRangePreset"
                        placeholder="金额区间"
                        @change="handleAmountPresetChange"
                        clearable
                        size="default"
                        class="search-input">
                        <el-option label="0 - 50元" value="0-50"></el-option>
                        <el-option label="50 - 100元" value="50-100"></el-option>
                        <el-option label="100 - 200元" value="100-200"></el-option>
                        <el-option label="200 - 500元" value="200-500"></el-option>
                        <el-option label="500元以上" value="500-999999"></el-option>
                    </el-select>
                    <el-input 
                        placeholder="买家" 
                        v-model="searchBuyer" 
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-input 
                        placeholder="卖家" 
                        v-model="searchSeller" 
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-select
                        v-model="searchOrderStatus"
                        placeholder="订单状态"
                        clearable
                        size="default"
                        class="search-input">
                        <el-option
                            v-for="item in orderStatusOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                        </el-option>
                    </el-select>
                    <el-select
                        v-model="searchPaymentStatus"
                        placeholder="支付状态"
                        clearable
                        size="default"
                        class="search-input">
                        <el-option
                            v-for="item in paymentStatusOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                        </el-option>
                    </el-select>
                    <el-date-picker
                        v-model="searchDateRange"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="创建开始时间"
                        end-placeholder="创建结束时间"
                        size="default"
                        style="width: 280px !important; max-width: 280px !important;"
                        class="date-picker">
                    </el-date-picker>
                    <el-button type="primary" class="search-btn" @click="searchIdle">搜索</el-button>
                    <el-button type="primary" class="search-btn reset-btn" @click="resetSearch">重置</el-button>
                </div>
            </div>
        </div>

        <div class="table-container">
            <el-table
                :data="Order"
                :header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
                style="width: 100%;"
                max-height="calc(100vh - 250px)"
                >
                
                <el-table-column
                    prop="orderNumber"
                    label="订单号"
                    min-width="180"
                    align="center">
                </el-table-column>

                <el-table-column
                    label="闲置名称"
                    min-width="150"
                    show-overflow-tooltip
                    align="center">
                    <template #default="scope">
                        <span v-if="scope.row.idleItem">{{ scope.row.idleItem.idleName }}</span>
                        <span v-else class="text-gray">商品已删除</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="闲置图片"
                    width="100"
                    align="center">
                    <template #default="scope">
                        <el-image 
                            class="goods-img"
                            v-if="scope.row.idleItem"
                            :src="getParams(scope.row.idleItem.pictureList)" 
                            :preview-src-list="getAllImages(scope.row.idleItem.pictureList)"
                            fit="cover"
                            :preview-teleported="true">
                            <template #error>
                                <div class="image-slot">
                                    <el-icon><Picture /></el-icon>
                                </div>
                            </template>
                        </el-image>
                        <span v-else class="text-gray">已删</span>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="orderPrice"
                    label="金额"
                    width="100"
                    align="center">
                    <template #default="scope">
                        <span class="goods-price">¥{{ scope.row.orderPrice }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="买家"
                    width="120"
                    align="center">
                    <template #default="scope">
                        <span class="person-name">{{ scope.row.user ? scope.row.user.nickname : '未知' }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="卖家"
                    width="120"
                    align="center">
                    <template #default="scope">
                        <span class="person-name">{{ scope.row.idleItem && scope.row.idleItem.user ? scope.row.idleItem.user.nickname : '未知' }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    prop="detailAddress"
                    label="详细地址"
                    min-width="220"
                    show-overflow-tooltip
                    align="center">
                    <template #default="scope">
                        <span class="address-text">{{ scope.row.detailAddress || '未填写' }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="订单状态"
                    width="100"
                    align="center">
                    <template #default="scope">
                        <el-tag size="small" :type="getOrderStatusType(scope.row.orderStatus)" class="status-tag">
                            {{orderStatus[scope.row.orderStatus]}}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column
                    label="支付状态"
                    width="100"
                    align="center">
                    <template #default="scope">
                        <span class="payment-text" :class="{'paid': scope.row.paymentStatus === 1}">
                            {{paymentStatus[scope.row.paymentStatus]}}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="createTime"
                    label="创建时间"
                    width="180"
                    align="center">
                </el-table-column>

            </el-table>
        </div>

        <div class="pagination-container">
            <el-pagination
                @current-change="handleCurrentChange"
                :current-page="nowPage"
                :page-size="8"
                background
                layout="prev, pager, next"
                :total="total">
            </el-pagination>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { Picture } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 获取全局属性
const { proxy } = getCurrentInstance();
const $api = proxy.$api;

// 响应式数据
const nowPage = ref(1);
const total = ref(0);
const paymentStatus = ref(['未支付', '已支付']);
const orderStatus = ref(['待付款', '待发货', '待收货', '已完成', '已取消']);
const Order = ref([]);
const searchOrderNumber = ref('');
const searchGoodsName = ref('');
const searchAmount = ref('');
const searchMinAmount = ref(null);
const searchMaxAmount = ref(null);
const amountRangePreset = ref('');
const searchBuyer = ref('');
const searchSeller = ref('');
const searchOrderStatus = ref('');
const searchPaymentStatus = ref('');
const searchDateRange = ref([]);
// 订单状态选项
const orderStatusOptions = ref([
    { value: 0, label: '待付款' },
    { value: 1, label: '待发货' },
    { value: 2, label: '待收货' },
    { value: 3, label: '已完成' },
    { value: 4, label: '已取消' }
]);

// 支付状态选项
const paymentStatusOptions = ref([
    { value: 0, label: '未支付' },
    { value: 1, label: '已支付' }
]);

// 方法
const getParams = (params) => {
    if (params) {
        try {
            let list = JSON.parse(params);
            if (list.length > 0) {
                return list[0];
            }
        } catch (e) {
            return '';
        }
    }
    return '';
};

const getAllImages = (params) => {
    if (params) {
        try {
            return JSON.parse(params);
        } catch (e) {
            return [];
        }
    }
    return [];
};

const getOrderStatusType = (status) => {
    // '待付款','待发货','待收货','已完成','已取消'
    const types = ['warning', 'primary', 'primary', 'success', 'info'];
    return types[status] || 'info';
};

// 处理金额区间预设值选择
const handleAmountPresetChange = (value) => {
    if (value) {
        const [min, max] = value.split('-').map(Number);
        searchMinAmount.value = min;
        searchMaxAmount.value = max;
    } else {
        searchMinAmount.value = null;
        searchMaxAmount.value = null;
    }
};

const getOrder = () => {
    $api.getOrderList({
        page: nowPage.value,
        nums:8
    }).then(res => {
        if(res.status_code==1){
            Order.value = res.data.list;
            total.value = res.data.count;
        }else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

const handleCurrentChange = (val) => {
    nowPage.value = val;
    getOrder();
};

const searchIdle = () => {
    let searchParams = {};
    
    // 添加多字段搜索参数，只添加有值的字段
    if (searchOrderNumber.value && searchOrderNumber.value.trim() !== '') {
        searchParams.orderNumber = searchOrderNumber.value.trim();
    }
    if (searchGoodsName.value && searchGoodsName.value.trim() !== '') {
        searchParams.idleName = searchGoodsName.value.trim();
    }
    // 金额区间搜索
    if (searchMinAmount.value !== null && searchMaxAmount.value !== null) {
        searchParams.minPrice = searchMinAmount.value;
        searchParams.maxPrice = searchMaxAmount.value;
    }
    if (searchBuyer.value && searchBuyer.value.trim() !== '') {
        searchParams.buyerNickname = searchBuyer.value.trim();
    }
    if (searchSeller.value && searchSeller.value.trim() !== '') {
        searchParams.sellerNickname = searchSeller.value.trim();
    }
    if (searchOrderStatus.value !== '') {
        searchParams.orderStatus = searchOrderStatus.value;
    }
    if (searchPaymentStatus.value !== '') {
        searchParams.paymentStatus = searchPaymentStatus.value;
    }
    if (searchDateRange.value && searchDateRange.value.length === 2 && searchDateRange.value[0] && searchDateRange.value[1]) {
        // 格式化日期为 YYYY-MM-DD 格式
        const formatDate = (date) => {
            const d = new Date(date);
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };
        searchParams.startTime = formatDate(searchDateRange.value[0]);
        searchParams.endTime = formatDate(searchDateRange.value[1]);
    }
    
    $api.queryOrder({
        ...searchParams,
        page: nowPage.value,
        nums: 8
    }).then(res => {
        console.log(res);
        if (res.status_code == 1) {
            Order.value = res.data.list;
            total.value = res.data.count;
        } else{
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

// 重置搜索条件
const resetSearch = () => {
    searchOrderNumber.value = '';
    searchGoodsName.value = '';
    searchAmount.value = '';
    searchMinAmount.value = null;
    searchMaxAmount.value = null;
    amountRangePreset.value = '';
    searchBuyer.value = '';
    searchSeller.value = '';
    searchOrderStatus.value = '';
    searchPaymentStatus.value = '';
    searchDateRange.value = [];
    nowPage.value = 1;
    // 重置后加载默认数据
    getOrder();
};

// 组件挂载时加载数据
onMounted(() => {
    getOrder();
});
</script>

<style scoped>
    .main-border{
        background-color: #FFFFFF;
        padding: 24px 32px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
        min-height: 600px;
    }

    .header-wrapper {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        margin-bottom: 24px;
    }

    .tabs-and-button {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        margin-bottom: 16px;
    }

    /* Tab 样式 */
    .custom-tabs {
        border-bottom: none !important;
        background: transparent;
        display: flex;
        align-items: center;
        gap: 12px;
        padding-bottom: 6px;
    }

    .page-title {
        font-size: 18px;
        font-weight: 600;
        color: #18191C;
    }
    
    .header-actions {
        display: flex;
        width: 100%;
        margin-top: 16px;
    }

    .search-box {
        display: flex;
        gap: 12px;
        align-items: center;
        flex-wrap: wrap;
    }

    .search-input {
        width: 160px;
    }

    .date-picker {
        width: 280px;
    }

    .search-btn {
        background-color: #FB7299;
        border-color: #FB7299;
        padding: 0 20px;
    }

    .search-btn:first-of-type {
        margin-left: auto;
    }

    .search-btn:hover {
        background-color: #FF88AA;
        border-color: #FF88AA;
    }

    .reset-btn {
        background-color: #FFFFFF;
        color: #FB7299;
        border-color: #FB7299;
        margin-left: 4px !important;
    }

    .reset-btn:hover {
        background-color: #FFF5F8;
        color: #FB7299;
        border-color: #FB7299;
    }

    /* 表格样式 */
    .table-container {
        margin-top: 10px;
    }

    :deep(.el-table) {
        border-radius: 8px;
        overflow: hidden;
    }

    :deep(.el-table th.el-table__cell) {
        height: 50px;
        background-color: #F6F7F8 !important;
    }

    :deep(.el-table td.el-table__cell) {
        padding: 16px 0;
    }

    /* 订单信息列 */
    .order-meta {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    .order-no {
        font-weight: 500;
        color: #18191C;
        font-size: 13px;
    }

    .order-time {
        font-size: 12px;
        color: #9499A0;
    }

    /* 商品信息列 */
    .goods-info {
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .goods-img {
        width: 60px;
        height: 60px;
        border-radius: 6px;
        border: 1px solid #E3E5E7;
        flex-shrink: 0;
    }

    .goods-detail {
        display: flex;
        flex-direction: column;
        gap: 6px;
    }

    .goods-name {
        font-size: 14px;
        color: #18191C;
        font-weight: 500;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    .goods-price {
        color: #FF6699;
        font-weight: 600;
        font-size: 15px;
    }

    /* 人员信息 */
    .people-info {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .person-row {
        display: flex;
        align-items: center;
        gap: 6px;
    }

    .person-name {
        font-size: 13px;
        color: #61666D;
    }

    /* 状态列 */
    .status-col {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
    }

    .status-tag {
        border-radius: 4px;
    }

    .payment-text {
        font-size: 12px;
        color: #9499A0;
    }

    .payment-text.paid {
        color: #67C23A;
    }

    .action-btn {
        font-weight: 500;
        padding: 0;
    }

    .action-btn.danger {
        color: #FF6699;
    }

    .action-btn.danger:hover {
        color: #FF4D80;
    }

    .text-gray {
        color: #9499A0;
        font-size: 13px;
    }

    /* 分页 */
    .pagination-container {
        display: flex;
        justify-content: center;
        margin-top: 30px;
    }

    :deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
        background-color: #FB7299;
        color: #FFF;
    }
</style>

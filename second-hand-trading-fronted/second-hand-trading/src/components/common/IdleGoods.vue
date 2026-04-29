<template>
	<div class="main-border">
		<div class="goods-list-header">
			<div class="tabs-and-button">
				<div class="custom-tabs">
					<div class="tab-item" :class="{active: mode === 3}" @click="handleSelect(3)">
						待审核
						<div class="active-bar" v-if="mode === 3"></div>
					</div>
					<div class="tab-item" :class="{active: mode === 1}" @click="handleSelect(1)">
						已上架
						<div class="active-bar" v-if="mode === 1"></div>
					</div>
					<div class="tab-item" :class="{active: mode === 2}" @click="handleSelect(2)">
						已下架
						<div class="active-bar" v-if="mode === 2"></div>
					</div>
				</div>
			</div>
			
			<div class="header-actions">
				<div class="search-box">
					<el-input 
						placeholder="闲置名称" 
						v-model="searchName" 
						@keyup.enter="searchIdle"
						clearable
						size="default"
						class="search-input">
					</el-input>
					<el-select
						v-model="priceRangePreset"
						placeholder="价格区间"
						@change="handlePricePresetChange"
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
						placeholder="地址" 
						v-model="searchPlace" 
						@keyup.enter="searchIdle"
						clearable
						size="default"
						class="search-input">
					</el-input>
					<el-input 
						placeholder="发布用户" 
						v-model="searchUser" 
						@keyup.enter="searchIdle"
						clearable
						size="default"
						class="search-input">
					</el-input>
					<el-date-picker
						v-model="searchDateRange"
						type="daterange"
						range-separator="至"
						start-placeholder="发布开始时间"
						end-placeholder="发布结束时间"
						size="default"
						style="width: 280px !important; max-width: 280px !important;"
						class="date-picker">
					</el-date-picker>
					<el-select
						v-model="searchLabel"
						placeholder="选择分类"
						clearable
						filterable
						size="default"
						class="search-input">
						<el-option
							v-for="item in labelOptions"
							:key="item.value"
							:label="item.label"
							:value="item.value">
						</el-option>
					</el-select>
					<el-button type="primary" class="search-btn" @click="searchIdle">搜索</el-button>
					<el-button type="primary" class="search-btn reset-btn" @click="resetSearch">重置</el-button>
				</div>
			</div>
		</div>

		<div class="table-container">
			<el-table v-if="mode == 1"
								key="online-goods-table"
								:data="onlineGoods"
								:header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
								style="width: 100%;"
								max-height="calc(100vh - 250px)">
				<el-table-column
					prop="idleName"
					label="闲置名称"
					min-width="150"
					show-overflow-tooltip
					align="center">
				</el-table-column>
				<el-table-column
					label="闲置图片"
					width="100"
					align="center">
					<template #default="scope">
						<el-image 
							class="goods-img"
							:src="getParams(scope.row.pictureList)" 
							:preview-src-list="getAllImages(scope.row.pictureList)"
							fit="cover"
							:preview-teleported="true">
							<template #error>
								<div class="image-slot">
									<el-icon><Picture /></el-icon>
								</div>
							</template>
						</el-image>
					</template>
				</el-table-column>
				<el-table-column
					prop="idlePrice"
					label="价格"
					width="100"
					align="center">
					<template #default="scope">
						<span class="goods-price">¥{{ scope.row.idlePrice }}</span>
					</template>
				</el-table-column>
				<el-table-column
					label="分类"
					width="100"
					align="center">
					<template #default="scope">
						<el-tag size="small" effect="plain" class="custom-tag">{{ getLabelName(scope.row.idleLabel) }}</el-tag>
					</template>
				</el-table-column>
				<el-table-column
					prop="idlePlace"
					label="地址"
					width="180"
					align="center">
					<template #default="scope">
						<span class="text-gray">{{ scope.row.idlePlace }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="user.nickname"
					label="发布用户"
					width="120"
					align="center">
					<template #default="scope">
						<span class="username-text text-gray">{{ scope.row.user.nickname }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="releaseTime"
					label="发布日期"
					width="180"
					align="center">
					<template #default="scope">
						<span class="date-text">{{ scope.row.releaseTime }}</span>
					</template>
				</el-table-column>
                <IdleActionColumn :mode="1" />
			</el-table>

			<el-table v-if="mode == 2"
								key="offline-goods-table"
								:data="OfflineGoods"
								:header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
								style="width: 100%;"
								max-height="calc(100vh - 250px)">
				<el-table-column
					prop="idleName"
					label="闲置名称"
					min-width="150"
					show-overflow-tooltip
					align="center">
					<template #default="scope">
						<span class="text-gray">{{ scope.row.idleName }}</span>
					</template>
				</el-table-column>
				<el-table-column
					label="闲置图片"
					width="100"
					align="center">
					<template #default="scope">
						<el-image 
							class="goods-img grayscale"
							:src="getParams(scope.row.pictureList)" 
							:preview-src-list="getAllImages(scope.row.pictureList)"
							fit="cover"
							:preview-teleported="true">
						</el-image>
					</template>
				</el-table-column>
				<el-table-column
					prop="idlePrice"
					label="价格"
					width="100"
					align="center">
					<template #default="scope">
						<span class="goods-price text-gray">¥{{ scope.row.idlePrice }}</span>
					</template>
				</el-table-column>
				<el-table-column
					label="分类"
					width="100"
					align="center">
					<template #default="scope">
						<el-tag size="small" type="info" effect="plain">{{ getLabelName(scope.row.idleLabel) }}</el-tag>
					</template>
				</el-table-column>
				<el-table-column
					prop="idlePlace"
					label="地址"
					width="180"
					align="center">
					<template #default="scope">
						<span class="text-gray">{{ scope.row.idlePlace }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="user.nickname"
					label="发布用户"
					width="120"
					align="center">
					<template #default="scope">
						<span class="username-text text-gray">{{ scope.row.user.nickname }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="releaseTime"
					label="发布日期"
					width="180"
					align="center">
					<template #default="scope">
						<span class="date-text">{{ scope.row.releaseTime }}</span>
					</template>
				</el-table-column>
                <IdleActionColumn :mode="2" />
			</el-table>

			<!-- 待审核闲置表格 -->
			<el-table v-if="mode == 3"
								key="pending-goods-table"
								:data="pendingGoods"
								:header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
								style="width: 100%;"
								max-height="calc(100vh - 250px)">
				<el-table-column
					prop="idleName"
					label="闲置名称"
					min-width="150"
					show-overflow-tooltip
					align="center">
				</el-table-column>
				<el-table-column
					label="闲置图片"
					width="100"
					align="center">
					<template #default="scope">
						<el-image 
							class="goods-img"
							:src="getParams(scope.row.pictureList)" 
							:preview-src-list="getAllImages(scope.row.pictureList)"
							fit="cover"
							:preview-teleported="true">
							<template #error>
								<div class="image-slot">
									<el-icon><Picture /></el-icon>
								</div>
							</template>
						</el-image>
					</template>
				</el-table-column>
				<el-table-column
					prop="idlePrice"
					label="价格"
					width="100"
					align="center">
					<template #default="scope">
						<span class="goods-price">¥{{ scope.row.idlePrice }}</span>
					</template>
				</el-table-column>
				<el-table-column
					label="分类"
					width="100"
					align="center">
					<template #default="scope">
						<el-tag size="small" type="warning" effect="plain">{{ getLabelName(scope.row.idleLabel) }}</el-tag>
					</template>
				</el-table-column>
				<el-table-column
					prop="idlePlace"
					label="地址"
					width="180"
					align="center">
					<template #default="scope">
						<span class="text-gray">{{ scope.row.idlePlace }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="user.nickname"
					label="发布用户"
					width="120"
					align="center">
					<template #default="scope">
						<span class="username-text text-gray">{{ scope.row.user.nickname }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="releaseTime"
					label="发布日期"
					width="180"
					align="center">
					<template #default="scope">
						<span class="date-text">{{ scope.row.releaseTime }}</span>
					</template>
				</el-table-column>
                <IdleActionColumn :mode="3" @approve="approveGoods" @reject="rejectGoods" />
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { IDLE_CATEGORY_OPTIONS, getIdleCategoryName } from '@/constants/idle-category';
import IdleActionColumn from './IdleActionColumn.vue';

// 获取全局属性
const { proxy } = getCurrentInstance();
const $api = proxy.$api;

// 响应式数据
const mode = ref(3); // 默认显示待审核 (1-已上架, 2-已下架, 3-待审核)
const nowPage = ref(1);
const total = ref(0);
const onlineGoods = ref([]);
const OfflineGoods = ref([]);
const pendingGoods = ref([]); // 待审核闲置
const searchName = ref('');
const searchMinPrice = ref(null);
const searchMaxPrice = ref(null);
const priceRangePreset = ref('');
const searchLabel = ref('');
const searchPlace = ref('');
const searchUser = ref('');
const searchDateRange = ref([]);
// 状态映射: UI模式(mode) -> 实际数据库状态(status)
const modeToStatusMap = {
    1: 1,  // 已上架 -> 状态1
    2: 4,  // 已下架 -> 状态4
    3: 3   // 待审核 -> 状态3
};
const status = ref(3); // 实际查询状态值，与mode保持一致除非mode是2

// 分类选项
const labelOptions = ref(IDLE_CATEGORY_OPTIONS);

// 方法
const getParams = (params) => {
    if (params) {
        let list = JSON.parse(params);
        if (list.length > 0) {
            return list[0];
        }
    }
    return '';
};

const getAllImages = (params) => {
    if (params) {
        return JSON.parse(params);
    }
    return [];
};

const getLabelName = (label) => {
    return getIdleCategoryName(label);
};

// 处理价格预设选项
const handlePricePresetChange = (value) => {
    if (value && value !== '') {
        // 选择预设区间
        const [min, max] = value.split('-').map(Number);
        searchMinPrice.value = min;
        searchMaxPrice.value = max;
    } else {
        // 清空
        searchMinPrice.value = null;
        searchMaxPrice.value = null;
    }
};

const searchIdle = () => {
    let searchParams = {};
    
    // 添加多字段搜索参数，只添加有值的字段
    if (searchName.value && searchName.value.trim() !== '') {
        searchParams.idleName = searchName.value.trim();
    }
    if (searchMinPrice.value !== null && searchMinPrice.value !== '') {
        searchParams.minPrice = searchMinPrice.value;
    }
    if (searchMaxPrice.value !== null && searchMaxPrice.value !== '') {
        searchParams.maxPrice = searchMaxPrice.value;
    }
    if (searchLabel.value) {
        searchParams.idleLabel = searchLabel.value;
    }
    if (searchPlace.value && searchPlace.value.trim() !== '') {
        searchParams.idlePlace = searchPlace.value.trim();
    }
    if (searchUser.value && searchUser.value.trim() !== '') {
        searchParams.userNickname = searchUser.value.trim();
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
    
    $api.queryIdle({
        ...searchParams,
        page: nowPage.value,
        nums: 8,
        status: status.value
    }).then(res => {
        console.log(res);
        if (res.status_code == 1) {
            if (res.data.list && res.data.list.length > 0) {
                // 有数据，根据当前tab模式赋值，不改变tab状态
                if(mode.value == 1){
                    onlineGoods.value = res.data.list;
                    total.value = res.data.count;
                } else if(mode.value == 2) { // 已下架状态
                    OfflineGoods.value = res.data.list;
                    total.value = res.data.count;
                } else if(mode.value == 3) {
                    pendingGoods.value = res.data.list;
                    total.value = res.data.count;
                }
            } else {
                // 没有数据，清空列表
                if (mode.value == 1) {
                    onlineGoods.value = [];
                } else if (mode.value == 2) {  // 已下架状态
                    OfflineGoods.value = [];
                } else if (mode.value == 3) {
                    pendingGoods.value = [];
                }
                total.value = 0;
            }
        } else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

// 重置搜索条件
const resetSearch = () => {
    searchName.value = '';
    searchMinPrice.value = null;
    searchMaxPrice.value = null;
    priceRangePreset.value = '';
    searchLabel.value = '';
    searchPlace.value = '';
    searchUser.value = '';
    searchDateRange.value = [];
    nowPage.value = 1;
    // 重置后加载当前tab的数据
    if (mode.value == 1) {
        getOnlineGoods();
    } else if (mode.value == 2) {
        getOfflineGoods();
    } else if (mode.value == 3) {
        getPendingGoods();
    }
};

const handleCurrentChange = (val) => {
    nowPage.value = val;
    // 保持搜索条件不变，重新加载当前tab的数据
    if (mode.value == 1) {
        getOnlineGoods();
    }
    if (mode.value == 2) {  // 已下架状态
        getOfflineGoods();
    }
    if (mode.value == 3) {
        getPendingGoods();
    }
};

const handleSelect = (val) => {
    if (mode.value !== val) {
        mode.value = val;
        status.value = modeToStatusMap[val]; // 使用映射获取实际状态值
        nowPage.value = 1;
        // 切换tab时清除搜索条件
        searchName.value = '';
        searchMinPrice.value = null;
        searchMaxPrice.value = null;
        priceRangePreset.value = '';
        searchLabel.value = '';
        searchPlace.value = '';
        searchUser.value = '';
        searchDateRange.value = [];
        if (val == 1) {
            getOnlineGoods();
        }
        if (val == 2) { // 已下架
            getOfflineGoods();
        }
        if (val == 3) {
            getPendingGoods();
        }
    }
};

const getOnlineGoods = () => {
    $api.queryIdle({
        status: 1,
        page: nowPage.value,
        nums: 8
    }).then(res => {
        if (res.status_code == 1) {
            onlineGoods.value = res.data.list;
            total.value = res.data.count;
        } else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

const getOfflineGoods = () => {
    $api.queryIdle({
        status: status.value, // 使用当前状态值
        page: nowPage.value,
        nums: 8
    }).then(res => {
        if (res.status_code == 1) {
            OfflineGoods.value = res.data.list;
            total.value = res.data.count;
        } else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

const getPendingGoods = () => {
    $api.queryIdle({
        status: 3,
        page: nowPage.value,
        nums: 8
    }).then(res => {
        if (res.status_code == 1) {
            pendingGoods.value = res.data.list;
            total.value = res.data.count;
        } else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

// 审核通过
const approveGoods = (i) => {
    ElMessageBox.confirm(
        '通过审核后，该闲置将立即上架并对所有用户可见，确认通过吗？',
        '审核通过确认',
        {
            confirmButtonText: '通过审核',
            cancelButtonText: '取消',
            type: 'success',
            customClass: 'custom-message-box',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            lockScroll: false
        }
    ).then(() => {
        $api.updateGoods({
            id: pendingGoods.value[i].id,
            status: 1
        }).then(res => {
            if (res.status_code == 1) {
                ElMessage.success('审核通过，闲置已上架');
                getPendingGoods();
            } else {
                ElMessage.error(res.msg);
            }
        }).catch(e => {
            console.log(e);
        });
    }).catch(() => {});
};

// 驳回审核
const rejectGoods = (i) => {
    // 快捷选项
    const quickReasons = [
        '图片不清晰或与闲置不符',
        '价格设置异常或不合理',
        '描述内容违规或不实',
        '闲置信息不完整',
        '疑似违禁品或侵权闲置'
    ];
    
    // 构建 HTML 内容
    const htmlContent = `
        <div style="margin-bottom: 12px; color: #666; font-size: 13px;">快捷选择驳回理由（可直接点击）：</div>
        <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px;">
            ${quickReasons.map((reason, index) => 
                `<button 
                    type="button" 
                    class="quick-reason-btn" 
                    data-reason="${reason}"
                    style="
                        padding: 6px 12px;
                        border: 1px solid #E3E5E7;
                        border-radius: 4px;
                        background: #F6F7F8;
                        color: #61666D;
                        font-size: 12px;
                        cursor: pointer;
                        transition: all 0.2s;
                    "
                    onmouseover="this.style.borderColor='#FB7299'; this.style.color='#FB7299'; this.style.background='#FFF5F8';"
                    onmouseout="this.style.borderColor='#E3E5E7'; this.style.color='#61666D'; this.style.background='#F6F7F8';"
                >${reason}</button>`
            ).join('')}
        </div>
        <div style="color: #666; font-size: 13px; margin-bottom: 8px;">或自定义驳回理由：</div>
    `;
    
    ElMessageBox.prompt(
        htmlContent,
        '驳回审核',
        {
            confirmButtonText: '确认驳回',
            cancelButtonText: '取消',
            inputPlaceholder: '请输入驳回理由（如：图片不清晰、价格异常、描述违规等）',
            inputType: 'textarea',
            dangerouslyUseHTMLString: true,
            inputValidator: (value) => {
                if (!value || value.trim() === '') {
                    return '请输入驳回理由';
                }
                if (value.trim().length < 5) {
                    return '驳回理由至少5个字';
                }
                return true;
            },
            customClass: 'custom-message-box',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            beforeClose: (action, instance, done) => {
                if (action === 'confirm') {
                    done();
                } else {
                    done();
                }
            },
            lockScroll: false
        }
    ).then(({ value }) => {
        $api.updateGoods({
            id: pendingGoods.value[i].id,
            status: 2,
            rejectReason: value // 传递驳回理由
        }).then(res => {
            if (res.status_code == 1) {
                ElMessage.success(`已驳回，闲置已下架\n驳回理由：${value}`);
                getPendingGoods();
            } else {
                ElMessage.error(res.msg);
            }
        }).catch(e => {
            console.log(e);
        });
    }).catch(() => {});
    
    // 添加快捷按钮点击事件
    setTimeout(() => {
        const btns = document.querySelectorAll('.quick-reason-btn');
        const input = document.querySelector('.el-message-box__input textarea');
        btns.forEach(btn => {
            btn.addEventListener('click', () => {
                if (input) {
                    input.value = btn.getAttribute('data-reason');
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                }
            });
        });
    }, 100);
};

// 组件挂载时加载数据
onMounted(() => {
    getPendingGoods(); // 默认显示待审核
    status.value = 3; // 设置默认状态为待审核
});
</script>

<style scoped>
    .main-border {
        background-color: #FFFFFF;
        padding: 24px 32px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
        min-height: 600px;
    }

    .goods-list-header {
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

    .custom-tabs {
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .tab-item {
        position: relative;
        padding: 8px 16px;
        border-radius: 20px;
        color: #61666D;
        font-size: 16px;
        line-height: 24px;
        cursor: pointer;
        transition: all 0.2s;
    }

    .tab-item:hover {
        color: #FB7299;
    }

    .tab-item.active {
        color: #FB7299;
        font-weight: 600;
    }

    .active-bar {
        position: absolute;
        left: 12px;
        right: 12px;
        bottom: -6px;
        height: 2px;
        background-color: #FB7299;
        border-radius: 2px;
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

    /* 价格区间样式（使用Element Plus InputNumber） */
    .price-range-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .price-input {
        width: 100px;
    }

    .price-separator {
        color: #9499A0;
        font-size: 14px;
        white-space: nowrap;
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

    /* 表格样式美化 */
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

    :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
        background: #FAFAFA;
    }

    /* 闲置信息列样式 */
    .goods-info {
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .goods-img {
        width: 64px;
        height: 64px;
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

    .meta-info {
        display: flex;
        flex-direction: column;
        gap: 6px;
        align-items: flex-start;
    }

    .custom-tag {
        border-radius: 4px;
        background-color: #F1F2F3;
        border-color: #E3E5E7;
        color: #61666D;
    }

    .location-text {
        font-size: 12px;
        color: #9499A0;
    }

    .user-cell {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .user-avatar-small {
        border: 1px solid #E3E5E7;
    }

    .username-text {
        font-size: 13px;
        color: #61666D;
    }

    .date-text {
        color: #9499A0;
        font-size: 13px;
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

    .action-btn.success {
        color: #67C23A;
    }

    .action-btn.success:hover {
        color: #85ce61;
    }

    /* 灰色状态（下架） */
    .grayscale {
        filter: grayscale(100%);
        opacity: 0.6;
    }

    .text-gray {
        color: #9499A0 !important;
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

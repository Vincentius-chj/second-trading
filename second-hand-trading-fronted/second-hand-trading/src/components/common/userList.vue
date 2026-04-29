<template>
    <div class="main-border">
        <div class="header-wrapper">
            <div class="tabs-and-button">
                <div class="custom-tabs">
                    <div class="tab-item" :class="{active: mode === 1}" @click="handleSelect(1)">
                        正常用户
                        <div class="active-bar" v-if="mode === 1"></div>
                    </div>
                    <div class="tab-item" :class="{active: mode === 2}" @click="handleSelect(2)">
                        违规用户
                        <div class="active-bar" v-if="mode === 2"></div>
                    </div>
                    <div class="tab-item" :class="{active: mode === 3}" @click="handleSelect(3)">
                        管理员
                        <div class="active-bar" v-if="mode === 3"></div>
                    </div>
                </div>
                <div v-show="mode == 3" class="add-admin-box">
                    <el-button type="primary" class="add-btn" @click="adminRegVisible = true">
                        <el-icon><Plus /></el-icon> 添加管理员
                    </el-button>
                </div>
            </div>
            
            <div class="header-actions">
                <div v-show="mode != 3" class="search-box">
                    <el-input 
                        v-model="searchId" 
                        placeholder="ID"
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-input 
                        v-model="searchNickname" 
                        placeholder="用户昵称"
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-input 
                        v-model="searchAccount" 
                        placeholder="用户账号"
                        @keyup.enter="searchIdle"
                        clearable
                        size="default"
                        class="search-input">
                    </el-input>
                    <el-date-picker
                        v-model="searchSignInTimeRange"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="注册开始时间"
                        end-placeholder="注册结束时间"
                        size="default"
                        style="width: 280px !important; max-width: 280px !important;"
                        class="date-picker">
                    </el-date-picker>
                </div>
                <div v-show="mode != 3" class="button-group">
                    <el-button type="primary" class="search-btn" @click="searchIdle">搜索</el-button>
                    <el-button type="primary" class="search-btn reset-btn" @click="resetSearch">重置</el-button>
                </div>
            </div>
        </div>

        <!-- 添加管理员弹窗 -->
        <el-dialog
            title="添加管理员"
            v-model="adminRegVisible"
            width="400px"
            custom-class="custom-dialog">
            <div class="form-item">
                <div class="label">管理员名称</div>
                <el-input v-model="adminName" maxlength="8" placeholder="请输入名称" clearable></el-input>
            </div>
            <div class="form-item">
                <div class="label">管理员账户</div>
                <el-input v-model="adminAccount" minlength="8" maxlength="10" placeholder="请输入账户" clearable></el-input>
            </div>
            <div class="form-item">
                <div class="label">密码</div>
                <el-input v-model="adminPassword" minlength="8" placeholder="请输入密码" show-password></el-input>
            </div>
            <div class="form-item">
                <div class="label">确认密码</div>
                <el-input v-model="adminRePassword" minlength="10" placeholder="请再次输入密码" show-password></el-input>
            </div>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="adminRegVisible = false">取消</el-button>
                    <el-button type="primary" @click="regAdmin">确定添加</el-button>
                </span>
            </template>
        </el-dialog>

        <div class="table-container">
            <!-- 正常用户表格 -->
            <el-table v-if="mode == 1"
                key="normal-user-table"
                :data="userData || []"
                :header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
                style="width: 100%;"
                max-height="calc(100vh - 250px)">
                
                <el-table-column
                    prop="id"
                    label="ID"
                    width="120"
                    align="center">
                </el-table-column>

                <el-table-column
                    prop="nickname"
                    label="用户昵称"
                    min-width="150"
                    align="center">
                    <template #default="scope">
                        <span class="user-nickname">{{ scope.row.nickname }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="头像"
                    width="120"
                    align="center">
                    <template #default="scope">
                        <el-image 
                            :src="scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                            :preview-src-list="[scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png']"
                            class="user-avatar"
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
                    prop="accountNumber"
                    label="用户账号"
                    min-width="150"
                    align="center">
                    <template #default="scope">
                        <span class="user-account">{{ scope.row.accountNumber }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="注册时间"
                    width="200"
                    align="center">
                    <template #default="scope">
                        <span class="time-text">{{ scope.row.signInTime }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="状态"
                    width="120"
                    align="center">
                    <template #default>
                        <el-tag size="small" type="success" effect="plain" class="status-tag">正常</el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                        <el-button
                            class="action-btn danger"
                            size="small"
                            type="text"
                            @click="sealUser(scope.$index)">
                            封禁账号
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 违规用户表格 -->
            <el-table v-if="mode == 2"
                key="bad-user-table"
                :data="badUserData || []"
                :header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
                style="width: 100%;"
                max-height="calc(100vh - 250px)">
                
                <el-table-column
                    prop="id"
                    label="ID"
                    width="120"
                    align="center">
                </el-table-column>

                <el-table-column
                    prop="nickname"
                    label="用户昵称"
                    min-width="150"
                    align="center">
                    <template #default="scope">
                        <span class="user-nickname text-gray">{{ scope.row.nickname }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="头像"
                    width="120"
                    align="center">
                    <template #default="scope">
                        <el-image 
                            :src="scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                            :preview-src-list="[scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png']"
                            class="user-avatar grayscale"
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
                    prop="accountNumber"
                    label="用户账号"
                    min-width="150"
                    align="center">
                    <template #default="scope">
                        <span class="user-account text-gray">{{ scope.row.accountNumber }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="注册时间"
                    width="200"
                    align="center">
                    <template #default="scope">
                        <span class="time-text">{{ scope.row.signInTime }}</span>
                    </template>
                </el-table-column>

                <el-table-column
                    label="状态"
                    width="120"
                    align="center">
                    <template #default>
                        <el-tag size="small" type="danger" effect="plain" class="status-tag">封禁中</el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                        <el-button
                            class="action-btn success"
                            size="small"
                            type="text"
                            @click="unsealUser(scope.$index)">
                            解除封禁
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 管理员表格 -->
            <el-table v-if="mode == 3"
                key="admin-user-table"
                :data="userManage || []"
                :header-cell-style="{background:'#FAFAFA',color:'#61666D',fontWeight:'600'}"
                style="width: 100%;"
                max-height="calc(100vh - 250px)">
                
                <el-table-column
                    prop="id"
                    label="ID"
                    width="100"
                    align="center">
                </el-table-column>

                <el-table-column label="头像" width="100" align="center">
                    <template #default="scope">
                        <el-image 
                            :src="scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                            :preview-src-list="[scope.row.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png']"
                            class="user-avatar"
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
                    prop="adminName"
                    label="管理员昵称"
                    min-width="140"
                    align="center">
                    <template #default="scope">
                        <div class="user-nickname">{{ scope.row.adminName }}</div>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="accountNumber"
                    label="管理员账号"
                    min-width="110"
                    align="center">
                    <template #default="scope">
                        <div class="user-account">{{ scope.row.accountNumber }}</div>
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="120" align="center" fixed="right">
                    <template #default>
                        <span class="text-gray" style="font-size: 12px;">暂无操作</span>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <div class="pagination-container">
            <el-pagination
                @current-change="handleCurrentChange"
                :current-page="nowPage"
                :page-size="7"
                background
                layout="prev, pager, next"
                :total="total">
            </el-pagination>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { Plus, Picture } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 获取全局属性
const { proxy } = getCurrentInstance();
const $api = proxy.$api;

// 响应式数据
const mode = ref(1);
const nowPage = ref(1);
const total = ref(63);
const adminRegVisible = ref(false);
const adminAccount = ref('');
const adminPassword = ref('');
const adminRePassword = ref('');
const adminName = ref('');
const userData = ref([]);
const badUserData = ref([]);
const userManage = ref([]);
const searchId = ref('');
const searchNickname = ref('');
const searchAccount = ref('');
const searchSignInTimeRange = ref([]);

// 方法
const handleCurrentChange = (val) => {
    nowPage.value = val;
    if(mode.value == 1){
        getUserData();
    }
    if(mode.value == 2){
        getBadUserData();
    }
    if(mode.value == 3){
        getUserManage();
    }
};

const handleSelect = (val) => {
    if(mode.value !== val){
        mode.value = val;
        if(val == 1){
            nowPage.value = 1;
            getUserData();
        }
        if(val == 2){
            nowPage.value = 1;
            getBadUserData();
        }
        if(val == 3){
            nowPage.value = 1;
            getUserManage();
        }
    }
};

const getUserData = () => {
    //正常普通用户
    $api.queryUser({
        mode: 1,
        page: nowPage.value,
        nums:8,
    }).then(res => {
        if(res.status_code==1){
            userData.value = res.data.list;
            total.value = res.data.count;
        }else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

const getBadUserData = () => {
    //违规用户
    $api.queryUser({
        mode: 2,
        page: nowPage.value,
        nums:8,
    }).then(res => {
        if(res.status_code==1){
            badUserData.value = res.data.list;
            total.value = res.data.count;
        }else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

const getUserManage = () => {
    //管理员
    $api.queryUser({
        mode: 3,
        page: nowPage.value,
        nums:8,
    }).then(res => {
        if(res.status_code==1){
            userManage.value = res.data.list;
            total.value = res.data.count;
        }else {
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

const sealUser = (i) => {
    ElMessageBox.confirm(
        '封禁后，该用户将无法登录和使用平台功能，确认要封禁吗？',
        '封禁用户确认',
        {
            confirmButtonText: '确认封禁',
            cancelButtonText: '取消',
            type: 'warning',
            customClass: 'custom-message-box',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            lockScroll: false
        }
    ).then(() => {
        console.log(userData.value[i].id);
        $api.updateUserStatus({
            id: userData.value[i].id,
            status:1
        }).then(res => {
            if(res.status_code==1){
                ElMessage.success('封禁成功');
                getUserData();
            }else {
                ElMessage.error(res.msg);
            }
        }).catch(e => {
            console.log(e);
        });
    }).catch(() => {});
};

const unsealUser = (i) => {
    ElMessageBox.confirm(
        '解封后，该用户将恢复正常使用平台，确认要解封吗？',
        '解封用户确认',
        {
            confirmButtonText: '确认解封',
            cancelButtonText: '取消',
            type: 'success',
            customClass: 'custom-message-box',
            distinguishCancelAndClose: true,
            closeOnClickModal: false,
            lockScroll: false
        }
    ).then(() => {
        $api.updateUserStatus({
            id: badUserData.value[i].id,
            status:0
        }).then(res => {
            if(res.status_code==1){
                ElMessage.success('解封成功');
                getBadUserData();
            }else {
                ElMessage.error(res.msg);
            }
        }).catch(e => {
            console.log(e);
        });
    }).catch(() => {});
};

const regAdmin = () => {
    if(adminPassword.value == adminRePassword.value){
        $api.regAdministrator({
            adminName: adminName.value,
            accountNumber: adminAccount.value,
            adminPassword: adminPassword.value,
        }).then(res => {
            if(res.status_code==1){
                total.value = total.value+1;
                nowPage.value= Math.ceil(total.value/8);
                console.log(nowPage.value);
                getUserManage();
                ElMessage.success("添加成功");
                adminRegVisible.value = false;
            }else {
                ElMessage.error(res.msg);
            }
        }).catch(e => {
            console.log(e);
            ElMessage.error("添加失败，账号重复或网络异常");
        });
    }
    else {
        ElMessage.error("两次输入的密码不一致");
    }
};

const searchIdle = () => {
    let searchParams = {};
    
    // 添加多字段搜索参数，只添加有值的字段
    if (searchId.value && searchId.value.trim() !== '') {
        searchParams.id = searchId.value.trim();
    }
    if (searchNickname.value && searchNickname.value.trim() !== '') {
        searchParams.nickname = searchNickname.value.trim();
    }
    if (searchAccount.value && searchAccount.value.trim() !== '') {
        searchParams.accountNumber = searchAccount.value.trim();
    }
    if (searchSignInTimeRange.value && searchSignInTimeRange.value.length === 2 && searchSignInTimeRange.value[0] && searchSignInTimeRange.value[1]) {
        // 格式化日期为 YYYY-MM-DD 格式
        const formatDate = (date) => {
            const d = new Date(date);
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        };
        searchParams.startTime = formatDate(searchSignInTimeRange.value[0]);
        searchParams.endTime = formatDate(searchSignInTimeRange.value[1]);
    }
    
    $api.queryUser({
        ...searchParams,
        mode: mode.value,
        page: nowPage.value,
        nums: 8,
    }).then(res => {
        if (res.status_code == 1) {
            if(mode.value == 1){
                userData.value = res.data.list;
                total.value = res.data.count;
            }else if(mode.value == 2){
                badUserData.value = res.data.list;
                total.value = res.data.count;
            }else {
                userManage.value = res.data.list;
                total.value = res.data.count;
            }
        } else{
            ElMessage.error(res.msg);
        }
    }).catch(e => {
        console.log(e);
    });
};

// 重置搜索条件
const resetSearch = () => {
    searchId.value = '';
    searchNickname.value = '';
    searchAccount.value = '';
    searchSignInTimeRange.value = [];
    nowPage.value = 1;
    // 重置后加载默认数据
    if (mode.value == 1) {
        getUserData();
    } else if (mode.value == 2) {
        getBadUserData();
    } else {
        getUserManage();
    }
};

// 组件挂载时加载初始数据
onMounted(() => {
    getUserData();
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
        gap: 12px;
    }

    .search-box {
        display: flex;
        gap: 12px;
        align-items: center;
        flex-wrap: wrap;
        flex: 1;
    }

    .button-group {
        display: flex;
        gap: 4px;
        align-items: center;
    }

    .search-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .search-label {
        color: #606266;
        font-size: 14px;
        white-space: nowrap;
        min-width: 60px;
        text-align: right;
    }

    .search-input {
        width: 160px;
    }

    .date-picker {
        width: 280px !important;
        max-width: 280px !important;
    }

    :deep(.date-picker.el-date-editor) {
        width: 280px !important;
    }

    :deep(.date-picker .el-range-editor) {
        width: 280px !important;
    }

    :deep(.el-date-editor.el-input__wrapper) {
        width: 280px !important;
    }

    .search-btn {
        background-color: #FB7299;
        border-color: #FB7299;
        padding: 0 20px;
    }

    .search-btn:hover {
        background-color: #FF88AA;
        border-color: #FF88AA;
    }

    .reset-btn {
        background-color: #FFFFFF;
        color: #FB7299;
        border-color: #FB7299;
    }

    .reset-btn:hover {
        background-color: #FFF5F8;
        color: #FB7299;
        border-color: #FB7299;
    }

    .add-btn {
        background-color: #FB7299;
        border-color: #FB7299;
        border-radius: 18px;
    }

    .add-btn:hover {
        background-color: #FF88AA;
        border-color: #FF88AA;
    }
    
    /* 弹窗样式 */
    .form-item {
        margin-bottom: 16px;
    }

    .label {
        font-size: 14px;
        color: #61666D;
        margin-bottom: 8px;
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

    /* 用户信息列 */
    .user-info-cell {
        display: flex;
        align-items: center;
        gap: 12px;
    }

    .user-avatar {
        width: 48px;
        height: 48px;
        border-radius: 6px;
        border: 1px solid #E3E5E7;
        flex-shrink: 0;
        cursor: pointer;
    }

    .image-slot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        background-color: #f5f7fa;
        color: #909399;
        font-size: 24px;
    }

    .user-detail {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    .user-nickname {
        font-size: 14px;
        color: #18191C;
        font-weight: 500;
    }

    .user-account {
        font-size: 14px;
        color: #9499A0;
    }

    .time-text {
        color: #9499A0;
        font-size: 13px;
    }

    .status-tag {
        border-radius: 4px;
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

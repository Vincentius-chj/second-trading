<template>
	<div class="bilibili-theme">
		<el-container class="main-container">
			<el-header class="bili-header">
				<div class="header-content">
					<div class="app-logo">
						<el-icon class="logo-icon"><Setting /></el-icon>
						<span class="logo-text">后台管理中心</span>
					</div>
					<div class="user-info">
						<el-avatar :size="32" class="admin-avatar" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
						<span class="admin-name">{{admin.nickname}}</span>
						<div class="divider"></div>
						<el-button class="add-admin-btn" type="text" @click="goAddAdminPage">新增管理员</el-button>
						<div class="divider"></div>
						<el-button class="logout-btn" type="text" @click="logout">退出</el-button>
					</div>
				</div>
			</el-header>
			<el-container class="body-container">
				<el-aside width="220px" class="bili-aside">
					<div class="menu-wrapper">
						<el-menu
							default-active="1"
							class="el-menu-vertical-demo"
							@select="handleSelect"
							text-color="#61666D"
							active-text-color="#FB7299">
							<el-menu-item index="1">
								<el-icon><Goods /></el-icon>
								<span>闲置管理</span>
							</el-menu-item>
							<el-menu-item index="2">
								<el-icon><List /></el-icon>
								<span>订单管理</span>
							</el-menu-item>
							<el-menu-item index="3">
								<el-icon><User /></el-icon>
								<span>用户管理</span>
							</el-menu-item>
						</el-menu>
					</div>
				</el-aside>
				<el-main class="bili-main">
					<div class="main-content-wrapper">
						<transition name="fade-transform" mode="out-in">
							<div :key="mode" class="content-card">
								<IdleGoods v-if="mode == 1" key="admin-idle-goods"></IdleGoods>
								<orderList v-if="mode == 2" key="admin-order-list"></orderList>
								<userList v-if="mode == 3" key="admin-user-list"></userList>
							</div>
						</transition>

						<!-- 新增管理员改为内嵌表单（非弹窗） -->
						<div v-if="addAdminDialogVisible" class="add-admin-panel custom-dialog">
							<div class="add-admin-header">
								<div class="add-admin-title">添加管理员</div>
								<el-button type="text" class="close-btn" @click="addAdminDialogVisible = false">✕</el-button>
							</div>
							<div class="add-admin-body">
								<div class="form-item">
									<div class="label">管理员名称</div>
									<el-input v-model="addAdminForm.adminName" maxlength="8" placeholder="请输入名称" clearable></el-input>
								</div>
								<div class="form-item">
									<div class="label">管理员账户</div>
									<el-input v-model="addAdminForm.accountNumber" minlength="8" maxlength="10" placeholder="请输入账户" clearable></el-input>
								</div>
								<div class="form-item">
									<div class="label">密码</div>
									<el-input v-model="addAdminForm.adminPassword" minlength="8" placeholder="请输入密码" show-password></el-input>
								</div>
							</div>
							<div class="add-admin-footer dialog-footer">
								<el-button @click="addAdminDialogVisible = false">取消</el-button>
								<el-button type="primary" :loading="addAdminLoading" @click="submitAddAdmin">确定添加</el-button>
							</div>
						</div>
					</div>
				</el-main>
			</el-container>
		</el-container>
	</div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { Goods, List, User, Setting } from '@element-plus/icons-vue';
import AppFoot from '../common/AppFoot.vue';
import IdleGoods from '../common/IdleGoods.vue';
import orderList from '../common/orderList.vue';
import userList from '../common/userList.vue';

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const $sta = proxy.$sta;
const router = useRouter();

const mode = ref(1);
const admin = ref({
    nickname: '管理员'
});

const logout = async () => {
    try {
        await ElMessageBox.confirm(
            '确定要退出登录吗？',
            '退出确认',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        );
        
        $api.loginOut({}).then(res => {
            if (res.status_code === 1) {
                $sta.isLogin = false;
                $sta.adminName = '';
                // 清除本地存储的管理员状态
                localStorage.removeItem('adminState');
                router.push({path: '/login-admin'});
            }
        }).catch(e => {
            console.log(e);
        });
    } catch (error) {
        // 用户取消了退出操作
        console.log('用户取消退出登录');
    }
};

// 新增管理员弹窗相关
const addAdminDialogVisible = ref(false);
const addAdminLoading = ref(false);
const addAdminForm = ref({
	accountNumber: '',
	adminPassword: '',
	adminName: ''
});

const openAddAdminDialog = () => {
	addAdminForm.value = { accountNumber: '', adminPassword: '', adminName: '' };
	addAdminDialogVisible.value = false;
};

const goAddAdminPage = () => {
	router.push({ path: '/admin/add' });
};

const submitAddAdmin = () => {
	if (!addAdminForm.value.accountNumber || !addAdminForm.value.adminPassword) {
		ElMessage.warning('请输入管理员账号和密码');
		return;
	}
	addAdminLoading.value = true;
	$api.regAdministrator(addAdminForm.value).then(res => {
		if (res && res.status_code === 1) {
			ElMessage.success('新增管理员成功');
			addAdminDialogVisible.value = false;
		} else {
			ElMessage.error((res && (res.msg || res.message)) || '新增失败');
		}
	}).catch(err => {
		console.error('regAdministrator error:', err);
		let serverMsg = null;
		if (err && err.msg) serverMsg = err.msg;
		if (!serverMsg && err && err.response && err.response.data) serverMsg = err.response.data.msg || err.response.data.message;
		if (serverMsg) ElMessage.error(serverMsg);
		else ElMessage.error('新增管理员失败，请稍后重试');
	}).finally(() => {
		addAdminLoading.value = false;
	});
};

const handleSelect = (val) => {
    if (mode.value !== val) {
        mode.value = val;
    }
};

onMounted(() => {
    // 优先使用sta中的管理员名称，如果为空则尝试从localStorage获取
    admin.value.nickname = $sta.adminName || '管理员';
});
</script>

<style scoped>
	.bilibili-theme {
		background-color: #F4F5F7;
		min-height: 100vh;
		font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
		color: #18191C;
	}

	.main-container {
		height: 100vh;
		display: flex;
		flex-direction: column;
		overflow: hidden;
	}

	.bili-header {
		background: #FFFFFF;
		box-shadow: 0 2px 4px rgba(0,0,0,0.05);
		padding: 0;
		height: 60px !important;
		z-index: 100;
		position: relative;
	}

	.header-content {
		max-width: 100%;
		height: 100%;
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 0 24px;
	}

	.app-logo {
		display: flex;
		align-items: center;
		gap: 12px;
		font-size: 18px;
		font-weight: 600;
		color: #18191C;
		cursor: pointer;
		transition: opacity 0.2s;
	}
	
	.app-logo:hover {
		opacity: 0.8;
	}
	
	.logo-icon {
		font-size: 22px;
		color: #FB7299;
	}

	.user-info {
		display: flex;
		align-items: center;
		gap: 12px;
	}

	.admin-avatar {
		border: 1px solid #E3E5E7;
	}

	.admin-name {
		font-size: 14px;
		color: #18191C;
		font-weight: 500;
	}

	.divider {
		width: 1px;
		height: 14px;
		background-color: #E3E5E7;
		margin: 0 4px;
	}

	.logout-btn {
		color: #61666D;
		font-size: 14px;
		padding: 0;
	}

	.logout-btn:hover {
		color: #FB7299;
	}

	.body-container {
		flex: 1;
		display: flex;
		overflow: hidden; /* 防止双滚动条 */
	}

	.bili-aside {
		background: #FFFFFF;
		display: flex;
		flex-direction: column;
		padding-top: 16px;
		user-select: none;
	}

	.menu-wrapper {
		flex: 1;
		overflow-y: auto;
	}

	.el-menu-vertical-demo {
		border-right: none;
	}

	/* 菜单项样式重构 */
	:deep(.el-menu-item) {
		font-size: 14px;
		height: 46px;
		line-height: 46px;
		margin: 4px 12px;
		border-radius: 6px;
		padding-left: 16px !important; /* 覆盖内联样式 */
		display: flex;
		align-items: center;
		transition: all 0.2s;
	}

	:deep(.el-menu-item i) {
		font-size: 18px;
		margin-right: 10px;
		color: #A0A4A9;
		transition: color 0.2s;
	}

	:deep(.el-menu-item:hover) {
		background-color: #E3E5E7;
		color: #18191C !important;
	}
	
	:deep(.el-menu-item:hover i) {
		color: #18191C;
	}

	:deep(.el-menu-item.is-active) {
		background-color: #EBF2FF !important; /* B站后台常用淡蓝色背景 */
		background-color: #FFECF1 !important; /* 改为淡粉色背景 */
		color: #FB7299 !important;
		font-weight: 500;
	}
	
	:deep(.el-menu-item.is-active i) {
		color: #FB7299;
	}

	.bili-main {
		background-color: #F4F5F7;
		padding: 20px;
		overflow-y: auto;
	}

	.main-content-wrapper {
		max-width: 1200px;
		margin: 0 auto;
		min-height: 100%;
	}
	
	.content-card {
		background: transparent;
		border-radius: 8px;
		min-height: 100%;
	}

	/* 页面切换动画 */
	.fade-transform-enter-active,
	.fade-transform-leave-active {
		transition: all 0.3s;
	}

	.fade-transform-enter-from {
		opacity: 0;
		transform: translateX(-10px);
	}

	.fade-transform-leave-to {
		opacity: 0;
		transform: translateX(10px);
	}

	/* 弹窗样式（与 userList.vue 统一） */
	.form-item {
		margin-bottom: 16px;
	}

	.label {
		font-size: 14px;
		color: #61666D;
		margin-bottom: 8px;
	}

	.dialog-footer {
		display: flex;
		gap: 8px;
	}

	/* 使 el-dialog (custom-dialog) 风格与 ElMessageBox 一致 */
	:deep(.custom-dialog .el-dialog__header) {
		padding-bottom: 16px !important;
		border-bottom: none !important;
	}

	:deep(.custom-dialog .el-dialog__title) {
		font-size: 18px !important;
		font-weight: 600 !important;
		color: #18191C !important;
	}

	:deep(.custom-dialog .el-dialog__body) {
		padding: 16px 24px !important;
		font-size: 14px !important;
		color: #61666D !important;
		line-height: 1.6 !important;
	}

	:deep(.custom-dialog .el-dialog__footer) {
		padding-top: 20px !important;
		display: flex !important;
		gap: 12px !important;
		justify-content: flex-end !important;
	}

	:deep(.custom-dialog .el-dialog__footer .el-button) {
		min-width: 80px !important;
		height: 36px !important;
		border-radius: 6px !important;
		font-size: 14px !important;
		font-weight: 500 !important;
		transition: all 0.2s !important;
	}

	:deep(.custom-dialog .el-dialog__footer .el-button--primary) {
		background: linear-gradient(135deg, #FB7299 0%, #FF88AA 100%) !important;
		border: none !important;
		box-shadow: 0 2px 8px rgba(251, 114, 153, 0.3) !important;
		color: #fff !important;
	}

	:deep(.custom-dialog .el-dialog__footer .el-button--primary:hover) {
		transform: translateY(-1px) !important;
		box-shadow: 0 4px 12px rgba(251, 114, 153, 0.4) !important;
	}

	:deep(.custom-dialog .el-dialog__footer .el-button--default) {
		background: #FFFFFF !important;
		border: 1px solid #E3E5E7 !important;
		color: #61666D !important;
	}

	:deep(.custom-dialog .el-dialog__footer .el-button--default:hover) {
		background: #F6F7F8 !important;
		border-color: #FB7299 !important;
		color: #FB7299 !important;
	}

	/* 让对话框外观更接近 ElMessageBox（圆角、阴影、白色卡片） */
	:deep(.custom-dialog .el-dialog) {
		border-radius: 12px !important;
		padding: 24px !important;
		box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15) !important;
		border: none !important;
		background: #fff !important;
	}

	/* 输入框：白色背景、边框、统一高度 */
	:deep(.custom-dialog .el-input__wrapper) {
		height: 42px !important;
		border-radius: 6px !important;
		border: 1px solid #E3E5E7 !important;
		background: #FFFFFF !important;
		box-shadow: none !important;
		padding: 0 12px !important;
	}

	:deep(.custom-dialog .el-input__inner) {
		height: 100% !important;
		padding: 0 !important;
		background: transparent !important;
		line-height: 42px !important;
		color: #18191C !important;
	}

	/* 避免 el-input 在 dialog 中显示淡蓝背景（例如被某处选择样式影响） */
	:deep(.custom-dialog .el-input__inner:-webkit-autofill),
	:deep(.custom-dialog .el-input__inner) {
		background-color: transparent !important;
	}

	/* 内嵌新增管理员卡片样式 */
	.add-admin-panel {
		background: #fff;
		border-radius: 12px;
		box-shadow: 0 12px 32px rgba(0,0,0,0.08);
		padding: 18px 20px;
		margin-bottom: 20px;
	}

	.add-admin-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 8px;
	}

	.add-admin-title {
		font-size: 18px;
		font-weight: 600;
		color: #18191C;
	}

	.close-btn {
		color: #909399;
		font-size: 16px;
	}

	.add-admin-body { padding-top: 8px; }

	.add-admin-footer { padding-top: 12px; display:flex; justify-content:flex-end; }
</style>
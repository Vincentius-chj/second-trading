<template>
  <div class="add-admin-page">
    <div class="profile-section-wrap">
      <div class="profile-section-panel">
        <el-page-header class="address-container-back" @back="goBack" content="添加管理员"></el-page-header>
        <div class="address-container-add">
          <div class="address-container-add-title">新增管理员</div>
          <el-form class="admin-form" autocomplete="off">
            <input class="auto-fill-trap" type="text" name="fakeUser" autocomplete="username" />
            <input class="auto-fill-trap" type="password" name="fakePwd" autocomplete="current-password" />

            <div class="address-container-add-item">
              <el-input
                v-model="form.adminName"
                maxlength="8"
                placeholder="请输入管理员名称"
                clearable
                name="admin-display-name"
                autocomplete="off">
                <template #prepend><span class="required-mark">*</span>管理员名称</template>
              </el-input>
            </div>

            <div class="address-container-add-item">
              <el-input
                v-model="form.accountNumber"
                minlength="8"
                maxlength="20"
                placeholder="请输入管理员账号"
                clearable
                name="admin-account-new"
                autocomplete="new-password">
                <template #prepend><span class="required-mark">*</span>管理员账户</template>
              </el-input>
            </div>

            <div class="address-container-add-item">
              <el-input
                v-model="form.adminPassword"
                minlength="8"
                placeholder="请输入管理员密码"
                show-password
                name="admin-password-new"
                autocomplete="new-password">
                <template #prepend><span class="required-mark">*</span>密码</template>
              </el-input>
            </div>

            <div class="actions">
              <el-button class="cancel-btn" @click="goBack">返回</el-button>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="submit">创建账号</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const $sta = proxy.$sta;
const router = useRouter();

const loading = ref(false);
const form = ref({ adminName: '', accountNumber: '', adminPassword: '' });

const goBack = () => {
  router.push({ path: '/platform-admin' });
};

const submit = () => {
  if (!form.value.accountNumber || !form.value.adminPassword) {
    ElMessage.warning('请输入管理员账号和密码');
    return;
  }
  loading.value = true;
  $api.regAdministrator({
    adminName: form.value.adminName,
    accountNumber: form.value.accountNumber,
    adminPassword: form.value.adminPassword
  }).then(res => {
    if (res && res.status_code === 1) {
      ElMessage.success('新增管理员成功');
      router.push({ path: '/platform-admin' });
    } else {
      ElMessage.error(res && (res.msg || res.message) || '新增失败');
    }
  }).catch(err => {
    console.error('regAdministrator error:', err);
    const serverMsg = err && err.response && err.response.data && (err.response.data.msg || err.response.data.message);
    if (serverMsg) ElMessage.error(serverMsg);
    else ElMessage.error('新增管理员失败，请稍后重试');
  }).finally(() => {
    loading.value = false;
  });
};

onMounted(() => {
  // 简单权限检查：若未登录管理员则跳回管理员登录页
  if (!$sta || !$sta.isLogin) {
    const local = localStorage.getItem('adminState');
    if (!local) {
      router.replace({ path: '/login-admin' });
    }
  }
});
</script>

<style scoped>
.add-admin-page {
  display: flex;
  align-items: center;
  padding: 0 20px;
  max-width: 900px;
  margin: 0 auto;
  min-height: 100vh;
  background: #fff;
  box-sizing: border-box;
  overflow-x: hidden;
}

.profile-section-wrap {
  width: 100%;
  max-width: 770px;
  margin: 0 auto;
  box-sizing: border-box;
  background: #fff;
  padding: 20px 0 30px;
  border-radius: 12px;
  box-shadow: none;
}

.profile-section-panel {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.address-container-back {
  margin-bottom: 20px;
}

.address-container-add {
  padding: 30px;
  max-width: 100%;
  background: #f9fafc;
  border-radius: 8px;
  margin: 0 0 30px;
  border: 1px solid #e3e5e7;
  width: 100%;
  box-sizing: border-box;
}

.address-container-add-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #222;
  padding-left: 12px;
  border-left: 4px solid #00A1D6;
  line-height: 1;
}

.admin-form {
  width: 100%;
}

.address-container-add-item {
  margin-bottom: 24px;
}

.auto-fill-trap {
  position: absolute;
  opacity: 0;
  width: 1px;
  height: 1px;
  pointer-events: none;
  left: -9999px;
  top: -9999px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

:deep(.el-input__wrapper) {
  height: 32px !important;
  line-height: 32px !important;
  box-sizing: border-box;
  font-size: 14px;
  border-radius: 4px;
  background: #fff;
}

:deep(.el-input__wrapper:hover) {
  border-color: #cfd4db;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  box-shadow: none;
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: #18191c;
}

:deep(.el-input__inner::placeholder) {
  color: #b2b8c2;
}

:deep(input:-webkit-autofill),
:deep(input:-webkit-autofill:hover),
:deep(input:-webkit-autofill:focus),
:deep(input:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
  -webkit-text-fill-color: #18191c !important;
  transition: background-color 5000s ease-in-out 0s;
}

.address-container-add-item :deep(.el-input-group__prepend) {
  background-color: #f6f7f8;
  color: #222;
  padding: 0 16px;
  font-weight: 500;
  width: 120px !important;
  flex-shrink: 0;
  text-align: center;
  border-radius: 4px 0 0 4px;
}

.required-mark {
  color: #f56c6c;
  margin-right: 4px;
}

.cancel-btn {
  min-width: 88px;
  border-radius: 10px;
  border-color: #e1e5ec;
}

.submit-btn {
  min-width: 106px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #fb7299 0%, #ff8fb0 100%);
  box-shadow: 0 8px 16px rgba(251, 114, 153, 0.28);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #fc7ea4 0%, #ffa0bd 100%);
  transform: translateY(-1px);
}
</style>

<template>
    <div>
        <app-head :nickname-value="userInfo.nickname"
                  :avatarValue="userInfo.avatar"></app-head>
        <app-body>
            <div v-show="!editAddress && !editProfile && !editPassword">
                <!-- B站风格个人中心头部 -->
                <div class="bili-header">
                    <div class="bili-banner" :style="bannerStyle">
                        <div class="bili-banner-mask"></div>
                    </div>
                    <div class="bili-user-wrapper">
                        <div class="bili-user-info">
                            <div class="bili-avatar-box">
                                <el-upload
                                        :action="uploadAction"
                                        :with-credentials="true"
                                        :on-success="fileHandleSuccess"
                                        :file-list="imgFileList"
                                        :show-file-list="false"
                                        accept="image/*"
                                        class="avatar-uploader"
                                >
                                    <div class="avatar-container">
                                        <el-image
                                                class="bili-avatar"
                                                :src="userInfo.avatar"
                                                fit="cover">
                                        </el-image>
                                        <div class="avatar-hover-mask">
                                            <el-icon><Camera /></el-icon>
                                        </div>
                                    </div>
                                </el-upload>
                            </div>
                            <div class="bili-info-content">
                                <div class="bili-name-row">
                                    <div class="bili-nickname-wrapper">
                                        <div v-if="!isEditingNickname" class="nickname-display" @click="enableNicknameEdit">
                                            <span class="bili-nickname">{{userInfo.nickname}}</span>
                                            <el-button class="edit-nickname-btn" size="small" circle>
                                                <el-icon><Edit /></el-icon>
                                            </el-button>
                                        </div>
                                        <div v-else class="nickname-edit">
                                            <el-input 
                                                v-model="tempNickname" 
                                                maxlength="10"
                                                show-word-limit
                                                ref="nicknameInputRef"
                                                @blur="saveNickname"
                                                @keyup.enter="saveNickname"
                                                @keyup.esc="cancelNicknameEdit"
                                                clearable
                                                size="small"
                                                class="nickname-input"
                                            ></el-input>
                                        </div>
                                    </div>
                                </div>
                                <div class="bili-desc-row">
                                    <span class="bili-sign">{{userInfo.signInTime}} 加入平台</span>
                                </div>
                                <div class="bili-meta-row">
                                    <span class="bili-meta-chip">
                                        <el-icon><User /></el-icon>
                                        账号：{{ userInfo.accountNumber || '未设置' }}
                                    </span>
                                    <span class="bili-meta-chip">
                                        <el-icon><Clock /></el-icon>
                                        注册时间：{{ userInfo.signInTime || '-' }}
                                    </span>
                                </div>
                            </div>
                            <div class="bili-action-btns">
                                <el-button class="bili-btn is-main" @click="editAddress=true">地址管理</el-button>
                                <el-button class="bili-btn" @click="startEditPassword">修改密码</el-button>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="idle-container">
                    <el-tabs v-model="activeName" @tab-click="handleClick">
                        <el-tab-pane label="刚刚发布" name="1"></el-tab-pane>
                        <el-tab-pane label="审核未通过" name="2"></el-tab-pane>
                        <el-tab-pane label="已经下架" name="4"></el-tab-pane>
                        <el-tab-pane label="我的收藏" name="3"></el-tab-pane>
                        <el-tab-pane label="出售记录" name="5"></el-tab-pane>
                        <el-tab-pane label="购买记录" name="6"></el-tab-pane>
                    </el-tabs>
                    <div
                        class="idle-container-list"
                        v-loading="tabLoading"
                        element-loading-text="加载中..."
                        element-loading-background="rgba(255, 255, 255, 0.7)">
                        <div v-for="(item,index) in (dataList && Array.isArray(dataList[activeName-1]) ? dataList[activeName-1] : [])" :key="item.id || index" class="idle-container-list-item">
                            <div class="idle-container-list-item-detile" @click="(activeName==='2') ? null : toDetails(activeName,item)">
                                <el-image
                                        style="width: 100px; height: 100px;"
                                        :src="item && item.imgUrl ? getImageUrl(item.imgUrl) : ''"
                                        fit="cover">
                                    <template #error>
                                        <div class="image-slot">
                                            <el-icon><Picture /></el-icon>无图
                                        </div>
                                    </template>
                                </el-image>
                                <div class="idle-container-list-item-text">
                                    <div class="content-with-button">
                                        <div class="idle-item-content" :data-tab="activeName">
                                            <div class="top-content">
                                                <div class="idle-container-list-title">
                                                    {{item && item.idleName ? item.idleName : ''}}
                                                </div>
                                                <div v-if="activeName === '4'" class="idle-container-list-idle-details" v-html="item && item.idleDetails ? item.idleDetails : ''">
                                                </div>
                                                <div v-else-if="activeName === '2' && item && item.rejectReason" class="reject-reason-box">
                                                    <span class="reject-label">驳回理由：</span>
                                                    <span class="reject-text">{{ item.rejectReason }}</span>
                                                </div>
                                                <div v-else class="idle-container-list-idle-details" v-html="item && item.idleDetails ? item.idleDetails : ''">
                                                </div>
                                            </div>
                                            <div class="bottom-content">
                                                <div class="idle-prive">
                                                    <span class="price-symbol">¥</span>
                                                    <span class="price-number">{{item && item.idlePrice ? item.idlePrice : 0}}</span>
                                                    <span v-if="(activeName==='5'||activeName==='6') && item && item.orderStatus !== undefined" class="order-status-tag">
                                                        {{orderStatus[item.orderStatus]}}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="idle-item-foot">
                                            <div class="button-and-time">
                                                <el-button v-if="activeName==='2'" class="resubmit-btn" type="primary" plain 
                                                           @click.stop="handle(activeName,item,index)">
                                                <el-icon class="el-icon--left"><Refresh /></el-icon>
                                                {{handleName[activeName]}}
                                                </el-button>
                                                <el-button v-else-if="activeName!=='5'&&activeName!=='6'&&!(activeName==='1'&&item&&item.idleStatus===3)" type="danger" plain 
                                                           @click.stop="handle(activeName,item,index)">{{handleName[activeName]}}
                                                </el-button>
                                                <div class="idle-container-list-idle-time" style="margin-top: 8px;">{{item && item.timeStr ? item.timeStr : ''}}</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>










            </div>
                        
                    <!-- 更换背景功能已移除，背景固定为 aaa.png -->
            
            <!-- 修改密码界面 -->
            <div v-show="editPassword" class="address-container">
                <div class="profile-section-wrap">
                    <div class="profile-section-panel">
                        <el-page-header class="address-container-back" @back="cancelEditPassword"
                                        content="修改密码"></el-page-header>
                        <div class="address-container-add">
                            <div class="address-container-add-title">修改密码</div>
                            <el-form ref="profileFormRef" :model="passwordForm" :rules="passwordRules" label-width="0" style="max-width: 800px;">
                                <el-form-item class="address-container-add-item" prop="oldPassword">
                                    <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码">
                                        <template #prepend><span style="color: #f56c6c; margin-right: 4px;">*</span>原密码</template>
                                    </el-input>
                                </el-form-item>
                                <el-form-item class="address-container-add-item" prop="newPassword">
                                    <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码">
                                        <template #prepend><span style="color: #f56c6c; margin-right: 4px;">*</span>新密码</template>
                                    </el-input>
                                </el-form-item>
                                <el-form-item class="address-container-add-item" prop="confirmPassword">
                                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码">
                                        <template #prepend><span style="color: #f56c6c; margin-right: 4px;">*</span>确认新密码</template>
                                    </el-input>
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="savePassword">保存</el-button>
                                    <el-button @click="cancelEditPassword">取消</el-button>
                                </el-form-item>
                            </el-form>
                        </div>
                    </div>
                </div>
            </div>
                        
            <div v-show="editAddress" class="address-container">
                <div class="profile-section-wrap">
                    <div class="profile-section-panel">
                        <el-page-header class="address-container-back" @back="editAddress=false"
                                        content="地址信息"></el-page-header>
                        <div class="address-container-add">
                            <div class="address-container-add-title">新增地址</div>
                            <form @submit.prevent="saveAddress">
                                <div class="address-container-add-item">
                                    <el-input placeholder="请输入姓名" v-model="addressInfo.consigneeName" maxlength="10"
                                              show-word-limit
                                              name="name"
                                              autocomplete="name">
                                        <template #prepend><span style="color: #f56c6c; margin-right: 4px;">*</span>姓名</template>
                                    </el-input>
                                </div>
                                <div class="address-container-add-item">
                                    <el-input placeholder="请输入手机号" v-model="addressInfo.consigneePhone"
                                              @input="filterPhoneNumber" maxlength="11" show-word-limit
                                              name="tel"
                                              autocomplete="tel">
                                        <template #prepend><span style="color: #f56c6c; margin-right: 4px;">*</span>手机号</template>
                                    </el-input>
                                </div>

                                <div class="address-container-add-item">
                                    <el-input placeholder="请输入详细地址" v-model="addressInfo.detailAddress"
                                              maxlength="50" show-word-limit
                                              name="address"
                                              autocomplete="street-address">
                                        <template #prepend><span style="color: #f56c6c; margin-right: 4px;">*</span>详细地址</template>
                                    </el-input>
                                </div>
                                <el-checkbox v-model="addressInfo.defaultFlag">设置为默认地址</el-checkbox>
                                <el-button style="margin-left: 20px;" native-type="button" @click="saveAddress">保存</el-button>
                            </form>
                        </div>
                        
                        <div class="address-container-list">
                            <div class="address-list-title">已有地址信息</div>
                            <el-table
                                    stripe
                                    :data="addressData && Array.isArray(addressData) ? addressData : []"
                                    style="width: 100%"
                                    v-if="addressData && Array.isArray(addressData)">
                        <el-table-column
                                prop="consigneeName"
                            label="姓名">
                        </el-table-column>
                        <el-table-column
                                prop="consigneePhone"
                            label="手机号">
                        </el-table-column>
                        <el-table-column
                                prop="detailAddressText"
                            label="地址"
                            width="200"
                            min-width="200">
                        </el-table-column>
                        <el-table-column label="操作" width="180" min-width="180">
                            <template #default="scope">
                                <el-button
                                        v-if="scope && scope.row"
                                        size="small"
                                        @click="handleEdit(scope.$index, scope.row)">编辑
                                </el-button>
                                <el-button
                                    v-if="scope && scope.row"
                                    size="small"
                                    type="danger"
                                    @click="handleDelete(scope.$index, scope.row)">删除
                                </el-button>
                            </template>
                        </el-table-column>
                        <el-table-column label="是否默认地址">
                            <template #default="scope">
                                <el-button v-if="scope && scope.row && scope.row.defaultFlag !== undefined && !scope.row.defaultFlag"
                                           size="small"
                                           @click="handleSetDefault(scope.$index, scope.row)">设为默认
                                </el-button>
                                <div v-else-if="scope && scope.row" style="padding-left: 10px;color: #409EFF;">{{scope.row.defaultAddress || ''}}
                                </div>
                            </template>
                        </el-table-column>
                            </el-table>
                        </div>
                    </div>
                </div>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, getCurrentInstance, nextTick, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Picture, Location, Camera, User, Setting, WarningFilled, Clock, DocumentChecked, Refresh } from '@element-plus/icons-vue';
import AppHead from '../common/AppHeader.vue';
import AppBody from '../common/AppPageBody.vue';
import AppFoot from '../common/AppFoot.vue';
import request from '@/utils/request';
import { useUserStore } from '../../stores/user';

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const router = useRouter();
const route = useRoute();

// Pinia Store
const userStore = useUserStore();

const profileFormRef = ref();
const imgFileList = ref([]);
const backgroundFileInput = ref(null);
const addressInfo = ref({
    consigneeName: '',
    consigneePhone: '',
    detailAddress: '',
    defaultFlag: false
});

const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
});
const passwordRules = {
    oldPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码长度在6-16位之间', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码长度在6-16位之间', trigger: 'blur' },
        { validator: validatePassword, trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
    ]
};

// 密码验证规则
const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入新密码'));
    } else if (value === passwordForm.value.oldPassword) {
        callback(new Error('新密码不能与原密码相同'));
    } else {
        // 验证密码长度
        if (value.length < 6 || value.length > 16) {
            callback(new Error('密码长度应在6-16位之间'));
        } else {
            if (passwordForm.value.confirmPassword !== '') {
                // 触发确认密码的校验
                if (profileFormRef.value) {
                    profileFormRef.value.validateField('confirmPassword');
                }
            }
            callback();
        }
    }
};

const validateConfirmPassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入新密码'));
    } else if (value !== passwordForm.value.newPassword) {
        callback(new Error('两次输入的密码不一致'));
    } else {
        callback();
    }
};

const getButtonText = (tabName) => {
    const buttonTextMap = {
        '1': '下架',    // 已上架 - 下架商品
        '2': '编辑',    // 审核未通过 - 编辑商品
        '3': '移除',    // 收藏 - 从收藏移除
        '4': '删除',    // 已下架 - 永久删除
        '5': '查看',    // 出售记录 - 查看订单
        '6': '查看'     // 购买记录 - 查看订单
    };
    return buttonTextMap[tabName] || '操作';
};

const activeName = ref('1');
const handleName = ref({'1': '下架', '2': '重新提交', '3': '移除', '4': '删除'});
const dataList = ref([
    [], // 0: 已上架(status=1)
    [], // 1: 审核未通过(status=2)
    [], // 2: 收藏
    [], // 3: 已下架(status=4)
    [], // 4: 出售记录
    [], // 5: 购买记录
]);
const orderStatus = ref(['待付款', '待发货', '待收货', '已完成', '已取消']);
const tabLoading = ref(false);
const userInfoDialogVisible = ref(false);
const notUserNicknameEdit = ref(true);
const userPasswordEdit = ref(false);
const editAddress = ref(false);
const editProfile = ref(false);
const editPassword = ref(false);
const isEditingNickname = ref(false);
const tempNickname = ref('');
const nicknameInputRef = ref(null);
const profileFormChanged = ref(false); // 跟踪资料表单是否改变
const originalNickname = ref(''); // 存储原始昵称
const originalPassword = ref({}); // 存储原始密码状态（虽然不实际存储密码，但用于跟踪变化）
const DEFAULT_BANNER_BACKGROUND = '/aaa.png';
const userInfo = ref({
    accountNumber: "",
    avatar: "",
    nickname: "",
    backgroundImg: "",
    signInTime: "",
});
const addressData = ref([]);

const bannerBackgroundImage = computed(() => {
    return userInfo.value.backgroundImg || DEFAULT_BANNER_BACKGROUND;
});

const bannerStyle = computed(() => {
    const backgroundValue = bannerBackgroundImage.value;
    const isGradientBackground = typeof backgroundValue === 'string' && backgroundValue.includes('gradient(');
    if (isGradientBackground) {
        return {
            background: backgroundValue
        };
    }
    return {
        backgroundImage: `url(${backgroundValue})`,
        backgroundSize: '100% auto',
        backgroundPosition: 'center top',
        backgroundRepeat: 'no-repeat',
        backgroundColor: 'transparent'
    };
});

// 计算上传地址，使用 request.js 中的 baseURL
const uploadAction = computed(() => {
    return `${request.defaults.baseURL}/upload`;
});

const safeParsePictureList = (pictureListRaw) => {
    try {
        const parsed = JSON.parse(pictureListRaw || '[]');
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        return [];
    }
};

const formatTimeStr = (timeValue) => {
    if (!timeValue || typeof timeValue !== 'string') {
        return '';
    }
    if (timeValue.length >= 19) {
        return timeValue.substring(0, 10) + ' ' + timeValue.substring(11, 19);
    }
    return timeValue;
};

const getUserInfo = () => {
    if (!userStore.getUserNickname) {
        $api.getUserInfo().then(res => {
            if (res.status_code === 1) {
                if (res.data) {
                    res.data.signInTime = res.data.signInTime.substring(0, 10);
                    console.log(res.data);
                    userStore.login(res.data);
                    userInfo.value = userStore.userInfo;
                }
            }
        });
    } else {
        userInfo.value = userStore.userInfo;
        originalNickname.value = userInfo.value.nickname; // 保存原始昵称
        console.log(userInfo.value);
    }
};

const getMyFavorite = () => {
    return $api.getMyFavorite().then(res=>{
        console.log('getMyFavorite', res);
        if (res.status_code === 1){
            const favoriteList = [];
            if (res.data && Array.isArray(res.data)) {
                for (let i = 0; i < res.data.length; i++) {
                    const current = res.data[i] || {};
                    const idleItem = current.idleItem || {};
                    const pictureList = safeParsePictureList(idleItem.pictureList);
                    favoriteList.push({
                        favoriteId: current.id,
                        id: idleItem.id,
                        imgUrl: pictureList.length > 0 ? getImageUrl(pictureList[0]) : '',
                        idleName: idleItem.idleName || '',
                        idleDetails: idleItem.idleDetails || '',
                        timeStr: formatTimeStr(current.createTime),
                        idlePrice: idleItem.idlePrice || 0
                    });
                }
            }
            dataList.value[2] = favoriteList;
        }
    }).catch(err => {
        console.log(err);
    });
};

const getMySoldIdle = () => {
    return $api.getMySoldIdle().then(res=>{
        if (res.status_code === 1){
            console.log('getMySoldIdle', res.data);
            const soldList = [];
            if (res.data && Array.isArray(res.data)) {
                for (let i = 0; i < res.data.length; i++) {
                    const current = res.data[i] || {};
                    const idleItem = current.idleItem || {};
                    const pictureList = safeParsePictureList(idleItem.pictureList);
                    soldList.push({
                        id: current.id,
                        imgUrl: pictureList.length > 0 ? getImageUrl(pictureList[0]) : '',
                        idleName: idleItem.idleName || '',
                        idleDetails: idleItem.idleDetails || '',
                        timeStr: formatTimeStr(current.createTime),
                        idlePrice: current.orderPrice || 0,
                        orderStatus: current.orderStatus
                    });
                }
            }
            dataList.value[4] = soldList;
        }
    }).catch(err => {
        console.log(err);
    });
};

const getMyOrder = () => {
    return $api.getMyOrder().then(res=>{
        if (res.status_code === 1){
            console.log('getMyOrder', res.data);
            const orderList = [];
            if (res.data && Array.isArray(res.data)) {
                for (let i = 0; i < res.data.length; i++) {
                    const current = res.data[i] || {};
                    const idleItem = current.idleItem || {};
                    const pictureList = safeParsePictureList(idleItem.pictureList);
                    orderList.push({
                        id: current.id,
                        imgUrl: pictureList.length > 0 ? getImageUrl(pictureList[0]) : '',
                        idleName: idleItem.idleName || '',
                        idleDetails: idleItem.idleDetails || '',
                        timeStr: formatTimeStr(current.createTime),
                        idlePrice: current.orderPrice || 0,
                        orderStatus: current.orderStatus
                    });
                }
            }
            dataList.value[5] = orderList;
        }
    }).catch(err => {
        console.log(err);
    });
};

const getIdleItemData = () => {
    return $api.getAllIdleItem().then(res => {
        console.log(res);
        if (res.status_code === 1) {
            const publishedList = [];
            const rejectedList = [];
            const offlineList = [];
            if (res.data && Array.isArray(res.data)) {
                for (let i = 0; i < res.data.length; i++) {
                    const current = res.data[i] || {};
                    const pictureList = safeParsePictureList(current.pictureList);
                    const normalizedItem = {
                        ...current,
                        timeStr: formatTimeStr(current.releaseTime),
                        imgUrl: pictureList.length > 0 ? getImageUrl(pictureList[0]) : ''
                    };
                    if (current.idleStatus === 1 || current.idleStatus === 3) {
                        publishedList.push(normalizedItem); // 刚刚发布（含待审核与已上架）
                    } else if (current.idleStatus === 2) {
                        rejectedList.push(normalizedItem); // 审核未通过
                    } else if (current.idleStatus === 4) {
                        offlineList.push(normalizedItem); // 已下架
                    }
                }
            }
            dataList.value[0] = publishedList;
            dataList.value[1] = rejectedList;
            dataList.value[3] = offlineList;
        }
    }).catch(err => {
        console.log(err);
    });
};

const loadTabDataByTab = (tabName) => {
    tabLoading.value = true;
    let requestPromise = Promise.resolve();
    if (tabName === '1' || tabName === '2' || tabName === '4') {
        requestPromise = getIdleItemData();
    } else if (tabName === '3') {
        requestPromise = getMyFavorite();
    } else if (tabName === '5') {
        requestPromise = getMySoldIdle();
    } else if (tabName === '6') {
        requestPromise = getMyOrder();
    }
    requestPromise.finally(() => {
        tabLoading.value = false;
    });
};

const getAddressData = () => {
    $api.getAddress().then(res => {
        if (res.status_code === 1) {
            let data = res.data;
            if (data && Array.isArray(data)) {
                for (let i = 0; i < data.length; i++) {
                    data[i].detailAddressText = data[i].detailAddress || '';
                    data[i].defaultAddress = data[i].defaultFlag ? '默认地址' : '设为默认';
                }
                console.log(data);
                addressData.value = data;
            } else {
                addressData.value = [];
            }
        }
    });
};

const handleClick = (tab, event) => {
    console.log('switch tab:', tab?.props?.name || activeName.value);
};

watch(activeName, (newTab) => {
    loadTabDataByTab(String(newTab));
});

const profileRules = {
    nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 10, message: '昵称长度必须在2-10个字符之间', trigger: 'blur' },
        { min: 2, max: 10, message: '昵称长度必须在2-10个字符之间', trigger: 'change' }
    ]
};

const checkProfileFormChange = () => {
    // 检查昵称是否与原始值不同
    profileFormChanged.value = userInfo.value.nickname !== originalNickname.value;
    // 触发表单校验
    if (profileFormRef.value) {
        profileFormRef.value.validateField('nickname');
    }
};

const saveProfileInfo = () => {
    // 只保存昵称
    if (profileFormChanged.value) { // 使用变化状态判断是否修改
        // 使用Element Plus的表单校验
        if (profileFormRef.value) {
            profileFormRef.value.validate((valid) => {
                if (valid) {
                    $api.updateUserPublicInfo({
                        nickname: userInfo.value.nickname
                    }).then(res => {
                        console.log(res);
                        if (res.status_code === 1) {
                            userStore.updateUserInfo({ nickname: userInfo.value.nickname });
                            ElMessage.success('修改成功');
                            editProfile.value = false;
                            profileFormChanged.value = false; // 重置变化状态
                        } else {
                            ElMessage.error(res.msg || '修改失败');
                        }
                    });
                } else {
                    ElMessage.error('请按要求填写昵称');
                }
            });
        }
    } else {
        ElMessage.info('未做任何修改');
        editProfile.value = false; // 如果没有修改，也关闭编辑模式
    }
};

const startEditProfile = () => {
    editProfile.value = true;
    notUserNicknameEdit.value = false;
    userPasswordEdit.value = false;
    editPassword.value = false;
    // 初始化原始昵称并设置表单变化状态
    originalNickname.value = userInfo.value.nickname;
    profileFormChanged.value = false;
    // 确保编辑状态被正确设置
    console.log("进入编辑模式");
};

const finishEditProfile = () => {
    editProfile.value = false;
    notUserNicknameEdit.value = true;
    userPasswordEdit.value = false;
    editPassword.value = false;
    profileFormChanged.value = false; // 确保退出编辑时重置状态
};

// 昵称编辑相关方法
const enableNicknameEdit = () => {
    isEditingNickname.value = true;
    tempNickname.value = userInfo.value.nickname;
    // 等待DOM更新后再聚焦输入框
    nextTick(() => {
        if (nicknameInputRef.value) {
            nicknameInputRef.value.focus();
        }
    });
};

const saveNickname = async () => {
    if (!tempNickname.value.trim()) {
        ElMessage.warning('昵称不能为空');
        isEditingNickname.value = false;
        return;
    }
    
    if (tempNickname.value === userInfo.value.nickname) {
        isEditingNickname.value = false;
        return;
    }
    
    try {
        const res = await $api.updateUserPublicInfo({
            nickname: tempNickname.value
        });
        
        if (res.status_code === 1) {
            userInfo.value.nickname = tempNickname.value;
            userStore.updateUserInfo({ nickname: tempNickname.value });
            ElMessage.success('昵称修改成功');
        } else {
            ElMessage.error(res.msg || '昵称修改失败');
            // 恢复原始昵称
            tempNickname.value = userInfo.value.nickname;
        }
    } catch (err) {
        console.log(err);
        ElMessage.error('网络异常！');
        // 恢复原始昵称
        tempNickname.value = userInfo.value.nickname;
    } finally {
        isEditingNickname.value = false;
    }
};

const cancelNicknameEdit = () => {
    tempNickname.value = userInfo.value.nickname;
    isEditingNickname.value = false;
};

// 更换背景功能已移除，相关方法已删除

const handleEdit = (index, row) => {
    console.log(index, row);
    addressInfo.value = JSON.parse(JSON.stringify(row));
};

const handleDelete = (index, row) => {
    console.log(index, row);
    ElMessageBox.confirm('是否确定删除该地址?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        lockScroll: false
    }).then(() => {
        $api.deleteAddress(row).then(res => {
            if (res.status_code === 1) {
                ElMessage.success('删除成功！');
                addressData.value.splice(index, 1);
                if (row.defaultFlag && addressData.value.length > 0) {
                    addressData.value[0].defaultFlag = true;
                    addressData.value[0].defaultAddress = '默认地址';
                    update({
                        id: addressData.value[0].id,
                        defaultFlag: true
                    });
                }
            } else {
                ElMessage.error('系统异常，删除失败！');
            }
        }).catch(() => {
            ElMessage.error('网络异常！');
        });
    }).catch(() => {
    });
};

const handleSetDefault = (index, row) => {
    console.log(index, row);
    row.defaultFlag = true;
    update(row);
};

const toDetails = (activeNameValue, item) => {
    // 保存当前tab到sessionStorage
    sessionStorage.setItem('meActiveTab', activeNameValue);
    
    if (activeNameValue === '5' || activeNameValue === '6') {
        router.push({path: '/order', query: {id: item.id}});
    } else {
        const query = { id: item.id };
        if (activeNameValue === '3') {
            try {
                const cache = JSON.parse(localStorage.getItem('idleBuyCountMap') || '{}');
                const preferredCount = Number(cache[String(item.id)]);
                if (Number.isInteger(preferredCount) && preferredCount > 0) {
                    query.buyCount = preferredCount;
                }
            } catch (e) {
                // ignore parse error and fallback to default quantity
            }
        }
        router.push({path: '/details', query});
    }
};

const handle = (activeNameValue, item, index) => {
    console.log(activeNameValue, item, index);
    
    // tab1: 已上架 - 下架
    if(activeNameValue === '1'){
        ElMessageBox.confirm('确认要下架该商品吗？', '下架确认', {
            confirmButtonText: '确认下架',
            cancelButtonText: '取消',
            type: 'warning',
            lockScroll: false
        }).then(() => {
            $api.updateIdleItem({
                id: item.id,
                idleStatus: 4 // 下架状态
            }).then(res=>{
                console.log(res);
                if(res.status_code === 1){
                    dataList.value[0].splice(index, 1);
                    item.idleStatus = 4;
                    dataList.value[3].unshift(item);
                    ElMessage.success('已下架');
                }else {
                    ElMessage.error(res.msg);
                }
            });
        }).catch(() => {});
    }
    // tab2: 审核未通过 - 重新编辑
    else if(activeNameValue === '2'){
        // 跳转到发布页面，带上商品ID进行编辑
        router.push({path: '/release', query: {id: item.id}});
    }
    // tab3: 收藏 - 取消收藏
    else if(activeNameValue === '3'){
        ElMessageBox.confirm('确认要移除该商品吗？', '移除确认', {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'warning',
            lockScroll: false
        }).then(() => {
            $api.deleteFavorite({
                id: item.favoriteId
            }).then(res=>{
                console.log(res);
                if(res.status_code === 1){
                    ElMessage.success('已移除');
                    dataList.value[2].splice(index, 1);
                }else {
                    ElMessage.error(res.msg);
                }
            }).catch(e=>{});
        }).catch(() => {});
    }
    // tab4: 已下架 - 永久删除
    else if(activeNameValue === '4'){
        ElMessageBox.confirm('删除后将无法恢复，确认要永久删除吗？', '删除确认', {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'error',
            lockScroll: false
        }).then(() => {
            $api.updateIdleItem({
                id: item.id,
                idleStatus: 0
            }).then(res=>{
                console.log(res);
                if(res.status_code === 1){
                    dataList.value[3].splice(index, 1);
                    ElMessage.success('已删除');
                }else {
                    ElMessage.error(res.msg);
                }
            });
        }).catch(() => {});
    }
};

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

const fileHandleSuccess = (response, file, fileList) => {
    console.log("file:", response, file, fileList);
    let imgUrl = response.data;
    // 处理图片URL格式
    imgUrl = getImageUrl(imgUrl);
    imgFileList.value = [];
    $api.updateUserPublicInfo({
        avatar: imgUrl
    }).then(res => {
        console.log(res);
        if (res.status_code === 1) {
            userInfo.value.avatar = imgUrl;
            userStore.updateUserInfo({ avatar: imgUrl });
            ElMessage.success('头像更换成功');
        } else {
            ElMessage.error(res.msg || '头像更换失败');
        }
    }).catch(() => {
        ElMessage.error('头像更换失败');
    });
};

const update = (data) => {
    $api.updateAddress(data).then(res => {
        if (res.status_code === 1) {
            getAddressData();
            ElMessage.success('修改成功！');
        } else {
            ElMessage.error('系统异常，修改失败！');
        }
    }).catch(() => {
        ElMessage.error('网络异常！');
    });
};

// 开始修改密码
const startEditPassword = () => {
    editPassword.value = true;
    notUserNicknameEdit.value = false;
    userPasswordEdit.value = false;
    editProfile.value = false;
};

// 保存密码修改
const savePassword = () => {
    // 使用Element Plus的表单校验
    if (profileFormRef.value) {
        profileFormRef.value.validate((valid) => {
            if (valid) {
                // 再次检查：新密码不能与旧密码相同
                if (passwordForm.value.oldPassword === passwordForm.value.newPassword) {
                    ElMessage.error('新密码不能与旧密码相同，请重新输入');
                    return;
                }
                
                // 再次检查：新密码必须与确认密码相同
                if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
                    ElMessage.error('两次输入的密码不一致，请重新输入');
                    return;
                }
                
                $api.updatePassword({
                    oldPassword: passwordForm.value.oldPassword,
                    newPassword: passwordForm.value.newPassword
                }).then(res => {
                    console.log(res);
                    if (res.status_code === 1) {
                        ElMessage.success('密码修改成功！');
                        // 重置表单
                        passwordForm.value = {
                            oldPassword: '',
                            newPassword: '',
                            confirmPassword: ''
                        };
                        editPassword.value = false;
                    } else {
                        ElMessage.error(res.msg || '修改失败');
                    }
                }).catch(err => {
                    console.log(err);
                    ElMessage.error('网络异常！');
                });
            } else {
                ElMessage.error('请按要求填写密码信息');
            }
        });
    }
};

// 取消修改密码
const cancelEditPassword = () => {
    editPassword.value = false;
    notUserNicknameEdit.value = true;
    userPasswordEdit.value = false;
    passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    };
};

const saveAddress = () => {
    // 表单校验
    if (!addressInfo.value.consigneeName) {
        ElMessage.error('请输入收货人姓名');
        return;
    }
    if (!addressInfo.value.consigneePhone) {
        ElMessage.error('请输入收货人手机号');
        return;
    }
    // 转换成字符串并去空格，确保类型安全
    const phoneStr = String(addressInfo.value.consigneePhone).trim();
    if (phoneStr.length !== 11) {
        ElMessage.error('手机号格式不正确');
        return;
    }
    if (!addressInfo.value.detailAddress) {
        ElMessage.error('请输入详细地址');
        return;
    }

    if (addressInfo.value.id) {
        console.log('update:', addressInfo.value);
        update(addressInfo.value);
        addressInfo.value = {
            consigneeName: '',
            consigneePhone: '',
            detailAddress: '',
            defaultFlag: false
        };
    } else {
        if (addressData.value.length >= 5) {
            ElMessage.error('已达到最大地址数量！');
        } else {
            console.log(addressInfo.value);
            $api.addAddress(addressInfo.value).then(res => {
                if (res.status_code === 1) {
                    getAddressData();
                    ElMessage.success('新增成功！');
                    addressInfo.value = {
                        consigneeName: '',
                        consigneePhone: '',
                        detailAddress: '',
                        defaultFlag: false
                    };
                    // 如果是从订单页面跳转过来的，保存地址后自动返回订单页面
                    const returnToOrder = sessionStorage.getItem('returnToOrder');
                    if (returnToOrder) {
                        sessionStorage.removeItem('returnToOrder');
                        router.push({ path: '/order', query: { id: returnToOrder } });
                        return;
                    }
                    // 如果是从发布页面跳转过来的，保存地址后自动返回发布页面
                    const releaseFormData = sessionStorage.getItem('releaseFormData');
                    if (releaseFormData) {
                        // 不删除 releaseFormData，让发布页面自己处理
                        router.push({ path: '/release' });
                        return;
                    }
                } else {
                    ElMessage.error('系统异常，新增失败！');
                }
            }).catch(e => {
                ElMessage.error('网络异常！');
            });
        }
    }
};

// 过滤手机号，只保留数字。兼容 Element Plus 的 @input（接收字符串）和原生事件对象
const filterPhoneNumber = (eventOrValue) => {
    let value = '';
    if (eventOrValue && eventOrValue.target && eventOrValue.target.value !== undefined) {
        // 原生事件对象
        value = String(eventOrValue.target.value || '');
    } else if (typeof eventOrValue === 'string' || typeof eventOrValue === 'number') {
        // Element Plus 的 input 回调会传入新的值
        value = String(eventOrValue);
    } else {
        value = '';
    }

    // 只保留数字字符
    value = value.replace(/[^\d]/g, '');
    // 限制长度为11位
    if (value.length > 11) {
        value = value.substring(0, 11);
    }
    // 更新值
    addressInfo.value.consigneePhone = value;
};

onMounted(() => {
    // 从 sessionStorage 恢复之前的tab
    const savedTab = sessionStorage.getItem('meActiveTab');
    if (savedTab) {
        activeName.value = savedTab;
        // 清除已使用的值
        sessionStorage.removeItem('meActiveTab');
    }
    
    // 检查路由参数，如果是从订单页面跳转过来新增地址，自动打开新增地址页面
    if (route.query.addAddress === 'true') {
        editAddress.value = true;
        // 清除路由参数，避免刷新页面时重复打开
        router.replace({ path: '/me', query: {} });
    }
    
    getUserInfo();
    getAddressData();
    loadTabDataByTab(String(activeName.value));
    
    // 背景固定为 /aaa.png，无需从用户信息加载自定义背景
    
    // 保持头部视觉稳定，不使用滚动视差，避免背景与信息区分离
});

// 已移除用户自定义背景功能，背景固定为 /aaa.png
</script>

<style scoped>
    /* 现代化个人中心头部 */
    .bili-header {
        position: relative;
        margin-bottom: 22px;
        background: #fff;
        border-radius: 14px;
        overflow: hidden;
        box-shadow: 0 8px 24px rgba(0,0,0,0.07);
    }

    .bili-banner {
        height: 220px;
        min-height: 220px;
        width: 100%;
        background-size: cover;
        background-position: center top;
        background-repeat: no-repeat;
        position: relative;
        overflow: hidden;
    }

    .bili-banner-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.5) 100%);
        z-index: 1;
    }

    .bili-banner > * {
        position: relative;
        z-index: 2;
    }

    .background-upload-btn {
        position: absolute;
        right: 20px;
        top: 18px;
        border-radius: 20px;
        border: 1px solid rgba(255, 255, 255, 0.5);
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
        backdrop-filter: blur(4px);
        font-weight: 500;
    }

    .background-upload-btn:hover {
        border-color: rgba(255, 255, 255, 0.8);
        background: rgba(255, 255, 255, 0.28);
        color: #fff;
    }

    .bili-user-wrapper {
        padding: 0;
        max-width: 100%;
        margin: 0;
        position: relative;
        z-index: 5;
    }

    .bili-user-info {
        display: flex;
        align-items: center;
        padding: 18px 20px;
        position: relative;
        gap: 16px;
        background: rgba(255, 255, 255, 0.94);
        border-radius: 0;
        border: 1px solid rgba(255, 255, 255, 0.7);
        box-shadow: 0 6px 18px rgba(15, 35, 95, 0.07);
    }

    .bili-avatar-box {
        margin-top: -12px;
        margin-right: 14px;
        position: relative;
        z-index: 10;
    }

    .avatar-container {
        position: relative;
        width: 92px;
        height: 92px;
        border: 3px solid #fff;
        border-radius: 50%;
        background: #fff;
        box-shadow: 0 6px 18px rgba(0,0,0,0.14);
        transition: all 0.3s ease;
    }
    
    .avatar-container:hover {
        transform: scale(1.05);
    }

    .bili-avatar {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        display: block;
    }

    .avatar-hover-mask {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.3));
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
        cursor: pointer;
        z-index: 1;
    }
    
    .avatar-hover-mask .el-icon {
        color: #fff;
        font-size: 24px;
    }

    .avatar-container:hover .avatar-hover-mask {
        opacity: 1;
    }

    .bili-info-content {
        flex: 1;
        padding: 8px 0;
        display: flex;
        flex-direction: column;
        justify-content: center;
        min-width: 0;
    }

    .bili-name-row {
        display: flex;
        align-items: center;
        margin-bottom: 6px;
    }

    .bili-nickname-wrapper {
        display: flex;
        align-items: center;
        height: auto;
    }
    
    .nickname-display {
        display: flex;
        align-items: center;
        cursor: pointer;
        position: relative;
    }
    
    .nickname-display:hover .bili-nickname {
        color: #409EFF;
    }
    
    .bili-nickname {
        font-size: 22px;
        font-weight: 600;
        color: #303133;
        margin-right: 10px;
        line-height: 1.2;
        display: flex;
        align-items: center;
        height: auto;
        position: relative;
        top: -1px;
    }
    
    .nickname-edit {
        display: flex;
        align-items: center;
    }
    
    .nickname-input {
        width: 150px;
    }
    
    .nickname-input :deep(.el-input__wrapper) {
        height: 32px;
        padding: 0 12px;
    }
    
    .edit-nickname-btn {
        width: 22px;
        height: 22px;
        min-height: 22px;
        padding: 3px !important;
        border-radius: 50%;
        border: 1px solid #dcdfe6;
        background: #fff;
        color: #909399;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s;
        flex-shrink: 0;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        position: relative;
        top: -1px;
    }
    
    .edit-nickname-btn:hover {
        background: #409EFF;
        color: #fff;
        border-color: #409EFF;
        transform: scale(1.1);
        box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
    }

    .bili-desc-row {
        display: flex;
        align-items: center;
    }

    .bili-sign {
        font-size: 13px;
        color: #909399;
        padding: 2px 0;
        margin-left: 0;
    }

    .bili-meta-row {
        margin-top: 8px;
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
    }

    .bili-meta-chip {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        background: #f5f7fb;
        color: #5f6b7c;
        border: 1px solid #e7ebf3;
        border-radius: 999px;
        font-size: 12px;
        line-height: 1;
        padding: 6px 10px;
        max-width: 100%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .bili-meta-chip .el-icon {
        font-size: 12px;
    }

    .bili-action-btns {
        margin-bottom: 0;
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-left: auto;
        align-items: center;
    }

    .bili-btn {
        min-width: 88px;
        height: 34px;
        border-radius: 18px;
        border-color: #dcdfe6;
        color: #606266;
        background-color: #fff;
        transition: all 0.3s;
        font-weight: 500;
    }

    .bili-btn.is-main {
        background-color: #409EFF;
        color: #fff;
        border-color: #409EFF;
    }

    .bili-btn.is-main:hover {
        background-color: #337ecc;
        border-color: #337ecc;
        color: #fff;
    }
    
    .bili-btn:hover {
        background-color: #e6e8f0;
        border-color: #c0c4cc;
        color: #409EFF;
    }

    @media (max-width: 1100px) {
        .bili-user-info {
            align-items: flex-start;
            flex-wrap: wrap;
        }

        .bili-action-btns {
            width: 100%;
            margin-left: 0;
            margin-top: 6px;
        }
    }

    /* Tabs 样式优化 - 现代化设计 */
    .idle-container {
        padding: 20px;
        max-width: 1200px;
        margin: -20px auto 50px;
        background: #fff;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        /* min-width: 1000px; */
    }

    :deep(.el-tabs__nav-wrap::after) {
        height: 1px;
        background-color: #ebeef5;
    }
    
    :deep(.el-tabs__item) {
        font-size: 15px;
        height: 48px;
        line-height: 48px;
        color: #606266;
        padding: 0 16px;
        transition: all 0.3s;
    }
    
    :deep(.el-tabs__item.is-active) {
        color: #409EFF;
        font-weight: 600;
    }
    
    :deep(.el-tabs__active-bar) {
        background-color: #409EFF;
        height: 3px;
        border-radius: 2px;
    }
    
    :deep(.el-tabs__nav-wrap.is-scrollable) {
        padding: 0 24px;
    }

    /* 卡片式列表项优化 */
    .idle-container-list {
        min-height: 500px;
        padding: 10px 0;
    }

    .idle-container-list-item {
        padding: 15px;
        border-radius: 8px;
        transition: all 0.3s ease;
        margin-bottom: 12px;
        border: 1px solid #eef1f5;
        background: #fff;
    }
    
    .idle-container-list-item:hover {
        box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        transform: translateY(-2px);
        border-color: #dcdfe6;
    }

    .idle-container-list-item-detile {
        display: flex;
        align-items: stretch;
        cursor: pointer;
    }
    
    .idle-container-list-item-detile .el-image {
        border-radius: 8px;
        flex-shrink: 0;
        border: 1px solid #ebeef5;
    }

    .idle-container-list-item-text {
        margin-left: 16px;
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 100px;
        height: auto;
        justify-content: stretch;
    }

    .idle-container-list-title {
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
        margin-bottom: 6px;
        line-height: 1.4;
        line-clamp: 2;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    
    .idle-container-list-title:hover {
        color: #00A1D6;
    }

    .idle-container-list-idle-details {
        font-size: 13px;
        color: #606266;
        margin: 0;
        margin-top: 3px;
        line-clamp: 2;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        line-height: 1.4;
    }

    .price-symbol {
        font-size: 14px;
        font-weight: 600;
        margin-right: 1px;
        line-height: 1;
    }

    .price-number {
        font-size: 18px;
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.2px;
        line-height: 1;
    }

    .order-status-tag {
        display: inline-flex;
        align-items: center;
        margin-left: 8px;
        font-size: 11px;
        line-height: 1;
        padding: 2px 6px;
        border-radius: 999px;
        font-weight: 500;
        border: 1px solid currentColor;
        white-space: nowrap;
    }

    /* 只针对商品信息区做分类排版，不改颜色，不加背景 */
    .idle-item-content[data-tab='1'] .idle-container-list-title {
        font-size: 16px;
        line-height: 1.35;
        margin-bottom: 4px;
        line-clamp: 2;
    }

    .idle-item-content[data-tab='1'] .idle-container-list-idle-details {
        line-height: 1.5;
        line-clamp: 2;
        -webkit-line-clamp: 2;
    }

    .idle-item-content[data-tab='2'] .idle-container-list-title {
        font-size: 15px;
        font-weight: 700;
        line-height: 1.35;
        line-clamp: 1;
        -webkit-line-clamp: 1;
        margin-bottom: 3px;
    }

    .idle-item-content[data-tab='2'] .reject-reason-box {
        margin: 0;
        margin-top: 3px;
        padding: 0;
        border-left: none;
        padding-left: 0;
    }

    .idle-item-content[data-tab='2'] .reject-text {
        line-height: 1.5;
        line-clamp: 2;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
    }

    .idle-item-content[data-tab='3'] .idle-container-list-title {
        line-clamp: 2;
        -webkit-line-clamp: 2;
        line-height: 1.35;
        margin-bottom: 3px;
    }

    .idle-item-content[data-tab='3'] .idle-container-list-idle-details {
        line-clamp: 2;
        -webkit-line-clamp: 2;
        line-height: 1.5;
    }

    .idle-item-content[data-tab='4'] .idle-container-list-title {
        font-size: 15px;
        font-weight: 600;
        line-height: 1.35;
        line-clamp: 1;
        -webkit-line-clamp: 1;
    }

    .idle-item-content[data-tab='4'] .idle-container-list-idle-details {
        line-clamp: 2;
        -webkit-line-clamp: 2;
        line-height: 1.5;
    }

    .idle-item-content[data-tab='5'] .idle-container-list-title,
    .idle-item-content[data-tab='6'] .idle-container-list-title {
        line-clamp: 1;
        -webkit-line-clamp: 1;
        margin-bottom: 3px;
        font-size: 15px;
        line-height: 1.35;
    }

    .idle-item-content[data-tab='5'] .idle-container-list-idle-details,
    .idle-item-content[data-tab='6'] .idle-container-list-idle-details {
        line-clamp: 1;
        -webkit-line-clamp: 1;
        margin-bottom: 0;
        line-height: 1.45;
    }

    .idle-item-content[data-tab='5'] .order-status-tag,
    .idle-item-content[data-tab='6'] .order-status-tag {
        vertical-align: baseline;
    }

    .idle-container-list-idle-time {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
        margin-left: -10px;
    }

    .idle-prive {
        font-size: 18px;
        color: #FB7299; /* B站粉 */
        font-weight: 700;
        margin: 0;
        display: flex;
        align-items: center;
    }

    .content-with-button {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        min-height: 100px;
        height: auto;
        position: relative;
    }
    
    .idle-item-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 100px;
        height: auto;
        margin-right: 120px; /* 为按钮预留空间 */
        min-width: 0;
        gap: 8px;
    }
    
    .idle-item-foot {
        flex-shrink: 0;
        position: absolute;
        right: 0;
        top: 0;
        height: 100%;
        display: flex;
        align-items: flex-end;
        justify-content: flex-end;
        padding-top: 8px;
        padding-bottom: 8px;
    }
    
    .button-and-time {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
    }
    
    .top-content {
        flex: 1 1 auto;
        display: flex;
        flex-direction: column;
        gap: 8px;
        justify-content: flex-start;
        min-width: 0;
    }
    
    .bottom-content {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        margin-top: auto;
        padding-top: 0;
    }

    /* 马上回理由样式优化 */
    .reject-reason-box {
        font-size: 12px;
        line-height: 1.4;
        margin: 2px 0 8px 0;  /* 减少上边距，使位置稍微往上移动 */
    }

    .reject-label {
        color: #f56c6c;
        font-weight: 600;
        margin-right: 4px;
    }

    .reject-text {
        color: #f56c6c;
    }

/* 移除无用的样式 */
    /* .setting-item ... (已移除) */
    /* .setting-label ... (已移除) */
    /* .setting-content ... (已移除) */
    /* .setting-text ... (已移除) */
    /* .setting-action ... (已移除) */
    /* .action-btn ... (已移除) */
    /* .pwd-edit-box ... (已移除) */
    /* .pwd-btn-group ... (已移除) */
    
    .confirm-btn {
        width: 120px;
    }

    /* 统一所有模拟输入框组内的 input 样式 */
    :deep(.custom-profile-input .el-input__wrapper) {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
        box-shadow: 0 0 0 1px #dcdfe6 inset !important;
        border: none !important;
        padding: 0 15px;
        height: 32px !important;
        line-height: 32px !important;
    }

    :deep(.custom-profile-input .el-input__wrapper:hover) {
        box-shadow: 0 0 0 1px #c0c4cc inset !important;
    }
    
    :deep(.custom-profile-input .el-input__wrapper.is-focus) {
        box-shadow: 0 0 0 1px #409EFF inset !important;
        z-index: 1;
    }

    .address-container {
        padding: 20px;
        max-width: 1280px;
        margin: 0 auto;
        background: #fff;
        box-sizing: border-box;
        overflow-x: hidden;
    }

    .profile-section-wrap {
        width: 100%;
        max-width: 1000px;
        margin: 0 auto;
        box-sizing: border-box;
        background: #fff;
        padding: 20px 0 30px;
        border-radius: 12px;
        box-shadow: none;
    }

    .profile-section-panel {
        width: 80%;
        max-width: 800px;
        margin: 0 auto;
        box-sizing: border-box;
    }

    .section-center {
        width: 100%;
        box-sizing: border-box;
        margin-left: auto;
        margin-right: auto;
    }

    .section-wide {
        max-width: 1000px;
    }

    .section-narrow {
        max-width: 800px;
    }

    /* 之前保留的地址相关样式 */
    .address-container-back {
        margin-bottom: 20px;
    }
    .address-container-add-title { font-size: 16px; font-weight: bold; margin-bottom: 20px; color: #333; }
    .address-container-add-item { margin-bottom: 24px; } /* 增加间距 */
    
    .address-container-add { 
        padding: 30px;
        max-width: 100%;
        background: #f9fafc; /* 浅灰背景 */
        border-radius: 8px;
        margin: 0 0 30px;
        border: 1px solid #e3e5e7;
        width: 100%;
        box-sizing: border-box;
    }

    .address-container-list {
        width: 100%;
        margin: 0;
    }

    .address-container-list :deep(.el-table) {
        width: 100% !important;
    }

    .address-list-title {
        color: #409EFF;
        font-size: 15px;
        margin-bottom: 20px;
    }
    
    .address-container-add-title { 
        font-size: 18px; 
        font-weight: 600; 
        margin-bottom: 24px; 
        color: #222; 
        padding-left: 12px;
        border-left: 4px solid #00A1D6; /* B站蓝左边框 */
        line-height: 1;
    }

    /* 强制统一收货地址区域所有输入框的高度和对齐 */
    .address-container-add-item :deep(.el-input__wrapper),
    .address-container-add-item :deep(.el-input-group__prepend),
    .custom-prepend,
    :deep(.custom-cascader .el-input__wrapper) {
        height: 32px !important;
        line-height: 32px !important;
        box-sizing: border-box;
        font-size: 14px;
    }

    /* 优化输入框组样式 - B站风格 */
    :deep(.el-input-group__prepend) {
        background-color: #f6f7f8;
        color: #222;
        padding: 0 16px;
        font-weight: 500;
        width: 120px !important; /* 统一增加宽度到 120px */
        flex-shrink: 0; /* 防止压缩 */
        text-align: center;
        border: 1px solid #dcdfe6; /* 恢复完整边框 */
        border-right: none; /* 移除右边框，依赖 input 的左边框作为分割线 */
        box-shadow: none;
        justify-content: center; /* 确保 Flex 布局下居中 */
    }
    
    /* 优化级联选择器样式 */
    :deep(.el-cascader .el-input__wrapper) {
        box-shadow: none !important;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        padding: 0 15px;
    }
    
    :deep(.el-cascader .el-input__wrapper:hover) {
        border-color: #c0c4cc;
    }
    
    :deep(.el-cascader .el-input__wrapper.is-focus) {
        border-color: #409EFF;
    }

    /* 模拟 input-group 布局 - B站风格 */
    .input-group-simulate {
        display: flex;
        align-items: stretch;
    }

    .custom-prepend {
        background-color: #f6f7f8;
        color: #222;
        padding: 0 16px;
        font-weight: 500;
        width: 120px !important; /* 强制统一宽度 */
        flex-shrink: 0; /* 防止压缩 */
        text-align: center;
        border: 1px solid #dcdfe6; /* 恢复完整边框 */
        border-right: none;
        border-radius: 4px 0 0 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        white-space: nowrap;
        font-size: 14px;
        box-sizing: border-box;
    }

    /* 修正级联选择器在模拟组中的样式 */
    :deep(.custom-cascader .el-input__wrapper) {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
        box-shadow: 0 0 0 1px #dcdfe6 inset !important; /* 使用 box-shadow 模拟边框，与 Element Plus 统一 */
        border: none !important;
        padding: 0 15px;
    }
    
    :deep(.custom-cascader .el-input__wrapper:hover) {
        box-shadow: 0 0 0 1px #c0c4cc inset !important;
    }
    
    :deep(.custom-cascader .el-input__wrapper.is-focus) {
        box-shadow: 0 0 0 1px #409EFF inset !important;
        z-index: 1; /* 确保 focus 时浮在 prepend 上层 */
    }

    .demonstration {
        color: #606266;
        font-size: 14px;
        margin-bottom: 8px;
        display: block;
        font-weight: 500;
    }

    /* 优化级联选择器下拉面板样式 */
    :deep(.el-cascader__dropdown) {
        border: none;
        box-shadow: 0 4px 16px rgba(0,0,0,0.1);
        border-radius: 8px;
    }

    :deep(.el-cascader-menu) {
        min-width: 100px;
        border-right: 1px solid #f4f5f7;
    }

    :deep(.el-cascader-node) {
        height: 40px;
        line-height: 40px;
        padding: 0 20px;
    }

    :deep(.el-cascader-node.in-active-path), 
    :deep(.el-cascader-node.is-active) {
        color: #00A1D6;
        font-weight: 500;
    }
    
    :deep(.el-cascader-node:not(.is-disabled):hover), 
    :deep(.el-cascader-node:not(.is-disabled):focus) {
        background-color: #f1f9ff;
        color: #00A1D6;
    }

    /* 适配浏览器自动填充样式 - 覆盖默认的黄色背景 */
    :deep(input:-webkit-autofill),
    :deep(input:-webkit-autofill:hover),
    :deep(input:-webkit-autofill:focus),
    :deep(input:-webkit-autofill:active) {
        -webkit-box-shadow: 0 0 0 1000px #fff inset !important;
        -webkit-text-fill-color: #606266 !important;
        transition: background-color 5000s ease-in-out 0s;
    }

    /* 驳回理由样式 */
    .reject-reason-box {
        display: flex;
        align-items: flex-start;
        padding: 2px 0;
        margin: 2px 0;
        font-size: 13px;
        line-height: 1.6;
    }

    .reject-label {
        color: #F56C6C;
        font-weight: 600;
        margin-right: 4px;
        flex-shrink: 0;
    }

    .reject-text {
        color: #666;
        word-break: break-all;
        flex: 1;
    }

    /* 操作按钮样式优化 */
    :deep(.resubmit-btn) {
        border-color: #409EFF !important;
        color: #409EFF !important;
        background: #ecf5ff !important;
        border-radius: 4px !important;
        padding: 6px 12px !important;
        font-size: 12px !important;
        transition: all 0.3s !important;
    }
    
    :deep(.resubmit-btn:hover) {
        background: #409EFF;
        color: #fff;
        border-color: #409EFF;
    }
    
    :deep(.el-button--danger.is-plain) {
        border-color: #f56c6c !important;
        color: #f56c6c !important;
        background: #fef0f0 !important;
        border-radius: 4px !important;
        padding: 6px 12px !important;
        font-size: 12px !important;
        transition: all 0.3s !important;
    }
    
    :deep(.el-button--danger.is-plain:hover) {
        background: #f56c6c;
        color: #fff;
        border-color: #f56c6c;
    }


</style>

<style>
    /* 全局样式 - 用于覆盖 Element Plus 弹出层样式 */
    .narrow-cascader-popper .el-cascader-menu {
        min-width: 140px !important;
    }
    
    .narrow-cascader-popper .el-cascader-menu__wrap {
        height: 200px;
    }

    .narrow-cascader-popper .el-cascader-node {
        padding: 0 10px !important;
        justify-content: flex-start !important;
    }
</style>

<style scoped>
.form-item-no-margin :deep(.el-form-item__content) {
    margin-top: 2px !important;
}
.form-item-no-margin :deep(.el-form-item__error) {
    margin-top: 2px !important;
    display: block !important;
    text-align: left !important;
    width: 100% !important;
    font-size: 12px !important;
    padding-left: 122px !important;
}

.profile-edit-container {
    padding: 20px;
    max-width: 1280px;
    margin: 0 auto;
    background: #fff;
}

.profile-container-back {
    margin-bottom: 20px;
}

.profile-container-form {
    padding: 30px;
    max-width: 800px;
    background: #f9fafc;
    border-radius: 8px;
    margin-bottom: 30px;
    border: 1px solid #e3e5e7;
}

.profile-container-form-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 24px;
    color: #222;
    padding-left: 12px;
    border-left: 4px solid #00A1D6;
    line-height: 1;
}

.profile-edit-no-button {
    padding-bottom: 10px !important;
    min-height: auto !important;
    margin-bottom: 0 !important;
}

.background-container {
    padding: 20px;
    max-width: 1280px;
    margin: 0 auto;
    background: #fff;
}

.background-container-back {
    margin-bottom: 20px;
}

.background-container-form {
    padding: 30px;
    max-width: 800px;
    background: #f9fafc;
    border-radius: 8px;
    margin-bottom: 30px;
    border: 1px solid #e3e5e7;
}

.background-container-form-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 24px;
    color: #222;
    padding-left: 12px;
    border-left: 4px solid #00A1D6;
    line-height: 1;
}

.background-options {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
    margin-bottom: 20px;
    align-items: stretch;
}

.background-option {
    height: 80px;
    border-radius: 8px;
    padding: 12px;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.3s;
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 80px;
}

.background-option:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.background-option.selected {
    border-color: #409EFF;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.background-name {
    color: #fff;
    font-weight: 500;
    text-shadow: 0 1px 2px rgba(0,0,0,0.5);
    z-index: 1;
}

.selected-mark {
    position: absolute;
    top: 6px;
    right: 6px;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: #409EFF;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
}

.background-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-start;
}

.password-container {
    padding: 20px;
    max-width: 1280px;
    margin: 0 auto;
    background: #fff;
}

.password-container-back {
    margin-bottom: 20px;
}

.password-container-form {
    padding: 30px;
    max-width: 800px;
    background: #f9fafc;
    border-radius: 8px;
    margin-bottom: 30px;
    border: 1px solid #e3e5e7;
}

.password-container-form-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 24px;
    color: #222;
    padding-left: 12px;
    border-left: 4px solid #00A1D6;
    line-height: 1;
}
</style>
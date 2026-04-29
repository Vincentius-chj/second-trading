<template>
    <div class="details-container">
        <app-head></app-head>
        <div class="details-wrapper">
            <!-- 顶部核心区域：左图右信息 -->
            <div class="product-intro-card">
                <div class="gallery-section">
                    <div class="main-image-box">
                        <el-image 
                            class="main-image"
                            :src="currentImage || (idleItemInfo.pictureList && idleItemInfo.pictureList[0])"
                            :preview-src-list="idleItemInfo.pictureList"
                            fit="cover">
                        </el-image>
                    </div>
                    <div class="thumbnail-list">
                        <div 
                            v-for="(img, idx) in idleItemInfo.pictureList" 
                            :key="idx"
                            class="thumbnail-item"
                            :class="{active: (currentImage || idleItemInfo.pictureList[0]) === img}"
                            @click="currentImage = img">
                            <el-image :src="img" fit="cover"></el-image>
                        </div>
                    </div>
                </div>
                
                <div class="info-section">
                    <h1 class="product-title">{{idleItemInfo.idleName}}</h1>
                    <div class="product-meta">
                        <div class="price-row">
                            <span class="price-symbol">¥</span>
                            <span class="price-value">{{idleItemInfo.idlePrice}}</span>
                            <span class="status-badge" :class="{
                                'on-sale': idleItemInfo.idleStatus === 1,
                                'rejected': idleItemInfo.idleStatus === 2,
                                'offline': idleItemInfo.idleStatus === 4
                            }">
                                {{getStatusText(idleItemInfo.idleStatus)}}
                            </span>
                        </div>
                        
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="label">交易地点</span>
                                <span class="value">{{idleItemInfo.idlePlace}}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">当前库存</span>
                                <span class="value">{{idleItemInfo.idleStock || 0}}</span>
                            </div>
                            <div class="info-item">
                                <span class="label">发布时间</span>
                                <span class="value">{{idleItemInfo.releaseTime}}</span>
                            </div>
                        </div>
                    </div>

                    <div class="seller-card-mini">
                        <el-avatar :size="48" :src="idleItemInfo.user.avatar"></el-avatar>
                        <div class="seller-info">
                            <div class="seller-name">{{idleItemInfo.user.nickname}}</div>
                            <div class="seller-desc">{{idleItemInfo.user.signInTime.substring(0,10)}} 加入</div>
                        </div>
                        <el-button size="small" round plain class="contact-btn" @click="scrollToMessage">留言</el-button>
                    </div>

                    <div class="action-area">
                         <template v-if="!isMaster && idleItemInfo.idleStatus===1">
                            <el-input-number
                                v-model="buyCount"
                                :min="1"
                                :max="idleItemInfo.idleStock || 1"
                                :step="1"
                                size="large"
                                class="buy-count-input"
                            ></el-input-number>
                            <el-button type="primary" size="large" class="buy-now-btn" @click="buyButton(idleItemInfo)">立即购买</el-button>
                            <el-button size="large" class="add-fav-btn" :class="{active: isFavorite}" @click="favoriteButton(idleItemInfo)">
                                <i :class="isFavorite ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                                {{isFavorite ? '已收藏' : '想要'}}
                            </el-button>
                        </template>
                         <template v-if="isMaster">
                            <el-button v-if="idleItemInfo.idleStatus===1" type="warning" size="large" @click="changeStatus(idleItemInfo,4)">下架商品</el-button>
                            <el-button v-if="idleItemInfo.idleStatus===2" type="success" size="large" @click="changeStatus(idleItemInfo,3)">重新提交审核</el-button>
                            <el-button v-if="idleItemInfo.idleStatus===4" type="success" size="large" @click="changeStatus(idleItemInfo,3)">重新上架</el-button>
                        </template>
                    </div>
                </div>
            </div>

            <!-- 下方详情与留言 -->
            <div class="details-body">
                <div class="left-column">
                    <div class="section-card details-card">
                        <div class="card-header">商品详情</div>
                        <div class="card-content" v-html="idleItemInfo.idleDetails"></div>
                    </div>

                    <div class="section-card review-card">
                        <div class="card-header">
                            用户评价 <span class="header-count">{{reviewList.length}}</span>
                        </div>
                        <div v-if="reviewList.length > 0" class="review-list">
                            <div class="review-item" v-for="(review, index) in reviewList" :key="review.id || index">
                                <el-avatar :size="40" :src="review.fromU && review.fromU.avatar ? review.fromU.avatar : ''" class="message-avatar"></el-avatar>
                                <div class="message-body">
                                    <div class="review-head">
                                        <div>
                                            <span class="mes-nickname">{{review.fromU && review.fromU.nickname ? review.fromU.nickname : '匿名用户'}}</span>
                                            <span class="mes-time">{{formatTime(review.createTime)}}</span>
                                        </div>
                                        <el-rate :model-value="review.score || 0" disabled show-score text-color="#ff9900"></el-rate>
                                    </div>
                                    <div class="review-content">{{review.content}}</div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="empty-message">暂无评价</div>
                    </div>

                    <div class="section-card message-card" id="replyMessageLocation">
                        <div class="card-header">
                            留言咨询 <span class="header-count">{{messageList.length}}</span>
                        </div>
                        
                         <!-- 发送留言 -->
                        <div class="message-editor">
                            <div v-if="isReply" class="reply-tag">
                                回复 {{replyData.toUserNickname}}: {{replyData.toMessage}}
                                <el-icon class="close-reply" @click="cancelReply"><Close /></el-icon>
                            </div>
                            <el-input
                                    type="textarea"
                                    :rows="3"
                                    placeholder="对这件商品感兴趣？留言问问卖家吧..."
                                    v-model="messageContent"
                                    maxlength="200"
                                    show-word-limit
                                    resize="none">
                            </el-input>
                            <div class="editor-footer">
                                <el-button type="primary" size="medium" @click="sendMessage">发送留言</el-button>
                            </div>
                        </div>

                        <!-- 留言列表 -->
                        <div class="message-list">
                            <div v-for="(mes,index) in messageList" :key="index" class="message-item">
                                <el-avatar :size="40" :src="mes.fromU.avatar" class="message-avatar"></el-avatar>
                                <div class="message-body">
                                    <div class="message-header">
                                        <span class="mes-nickname">{{mes.fromU.nickname}}</span>
                                        <span class="mes-time">{{mes.createTime}}</span>
                                    </div>
                                    <div class="message-content">
                                        <span v-if="mes.toU.nickname" class="reply-target">
                                            @{{mes.toU.nickname}}
                                        </span>
                                        <span v-html="mes.content"></span>
                                    </div>
                                    <div class="message-actions">
                                        <span class="action-btn" @click="replyMessage(index)">回复</span>
                                    </div>
                                </div>
                            </div>
                            <div v-if="messageList.length === 0" class="empty-message">
                                暂无留言，快来抢沙发吧~
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="right-column">
                    <div class="section-card safety-card">
                        <div class="safety-title">交易安全贴士</div>
                        <div class="safety-item">1. 建议在平台内进行交易，切勿私下转账</div>
                        <div class="safety-item">2. 购买前请仔细核对商品信息</div>
                        <div class="safety-item">3. 收到商品后请及时确认收货</div>
                    </div>
                </div>
            </div>
        </div>
        <app-foot></app-foot>
    </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Location, Clock, Warning, StarFilled, Star, Close } from '@element-plus/icons-vue';
import AppHead from '../common/AppHeader.vue';
import AppBody from '../common/AppPageBody.vue';
import AppFoot from '../common/AppFoot.vue';
import { useUserStore } from '../../stores/user';

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const router = useRouter();
const route = useRoute();

// Pinia Store
const userStore = useUserStore();

const messageContent = ref('');
const toUser = ref(null);
const toMessage = ref(null);
const isReply = ref(false);
const replyData = ref({
    toUserNickname: '',
    toMessage: ''
});
const messageList = ref([]);
const reviewList = ref([]);
const idleItemInfo = ref({
    id: '',
    idleName: '',
    idleDetails: '',
    pictureList: [],
    idlePrice: 0,
    idleStock: 0,
    idlePlace: '',
    idleLabel: '',
    idleStatus: -1,
    userId: '',
    user: {
        avatar: '',
        nickname: '',
        signInTime: ''
    },
});
const isMaster = ref(false);
const isFavorite = ref(true);
const favoriteId = ref(0);
const currentImage = ref('');
const buyCount = ref(1);

const BUY_COUNT_CACHE_KEY = 'idleBuyCountMap';

const savePreferredBuyCount = (idleId, count) => {
    if (!idleId || !count || count <= 0) {
        return;
    }
    let cache = {};
    try {
        cache = JSON.parse(localStorage.getItem(BUY_COUNT_CACHE_KEY) || '{}');
    } catch (e) {
        cache = {};
    }
    cache[String(idleId)] = count;
    localStorage.setItem(BUY_COUNT_CACHE_KEY, JSON.stringify(cache));
};

const getPreferredBuyCount = (idleId) => {
    if (!idleId) {
        return null;
    }
    try {
        const cache = JSON.parse(localStorage.getItem(BUY_COUNT_CACHE_KEY) || '{}');
        const value = Number(cache[String(idleId)]);
        if (Number.isInteger(value) && value > 0) {
            return value;
        }
    } catch (e) {
        return null;
    }
    return null;
};

const goToLogin = () => {
    ElMessage.warning('请先登录');
    router.push('/login');
};

const ensureUserLogin = () => {
    if (userStore.getUserNickname || localStorage.getItem('userToken')) {
        return true;
    }
    goToLogin();
    return false;
};

const getAllIdleMessage = () => {
    $api.getAllIdleMessage({
        idleId: idleItemInfo.value.id
    }).then(res=>{
        console.log('getAllIdleMessage', res.data);
        if(res.status_code === 1){
            messageList.value = res.data;
        }
    }).catch(()=>{
    });
};

const getAllIdleReview = () => {
    $api.getIdleReview({
        idleId: idleItemInfo.value.id
    }).then(res => {
        if (res && res.status_code === 1 && Array.isArray(res.data)) {
            reviewList.value = res.data;
        } else {
            reviewList.value = [];
        }
    }).catch(() => {
        reviewList.value = [];
    });
};

const checkFavorite = () => {
    const hasSession = !!userStore.getUserNickname || !!localStorage.getItem('userToken');
    if (!hasSession) {
        isFavorite.value = false;
        favoriteId.value = 0;
        return;
    }
    $api.checkFavorite({
        idleId: idleItemInfo.value.id
    }).then(res=>{
        if(!res.data){
            isFavorite.value = false;
        }else {
            favoriteId.value = res.data;
        }
    }).catch(() => {
        isFavorite.value = false;
        favoriteId.value = 0;
    });
};

const getCookie = (cname) => {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name)===0) return c.substring(name.length,c.length);
    }
    return "";
};

const replyMessage = (index) => {
    $('html,body').animate({
        scrollTop: $("#replyMessageLocation").offset().top-100
    }, {duration: 500, easing: "swing"});
    isReply.value = true;
    replyData.value.toUserNickname = messageList.value[index].fromU.nickname;
    replyData.value.toMessage = messageList.value[index].content.substring(0,10)+(messageList.value[index].content.length>10?'...':'');
    toUser.value = messageList.value[index].userId;
    toMessage.value = messageList.value[index].id;
};

const changeStatus = (idle, status) => {
    let actionText = '';
    let confirmText = '';
    
    if(status === 3) {
        actionText = '重新上架';
        confirmText = '确认要重新上架该商品吗？上架后需要管理员审核通过才能展示。';
    } else if(status === 4) {
        actionText = '下架';
        confirmText = '确认要下架该商品吗？';
    }
    
    ElMessageBox.confirm(confirmText, actionText + '确认', {
        confirmButtonText: '确认' + actionText,
        cancelButtonText: '取消',
        type: 'warning',
        lockScroll: false
    }).then(() => {
        $api.updateIdleItem({
            id: idle.id,
            idleStatus: status
        }).then(res=>{
            console.log(res);
            if(res.status_code === 1){
                idleItemInfo.value.idleStatus = status;
                ElMessage.success(actionText + '成功！');
            }else {
                ElMessage.error(res.msg);
            }
        });
    }).catch(() => {
        // 用户取消操作
    });
};

const buyButton = (idleItemInfoData) => {
    if (!ensureUserLogin()) {
        return;
    }
    // 前端二次验证：防止用户购买自己发布的商品
    if (isMaster.value) {
        ElMessage.error('不能购买自己发布的商品！');
        return;
    }
    if (!idleItemInfoData.idleStock || idleItemInfoData.idleStock <= 0) {
        ElMessage.error('库存不足，商品暂不可购买');
        return;
    }
    if (buyCount.value <= 0 || buyCount.value > idleItemInfoData.idleStock) {
        ElMessage.error('购买数量超过库存');
        return;
    }
    
    $api.addOrder({
        idleId: idleItemInfoData.id,
        orderCount: buyCount.value,
        orderPrice: Number(idleItemInfoData.idlePrice) * buyCount.value,
    }).then(res=>{
        console.log(res);
        if(res && res.status_code === 1){
            router.push({path: '/order', query: {id: res.data.id}});
        }else {
            // 显示后端返回的具体错误信息
            const errorMsg = res ? res.msg : '请求失败，请稍后重试';
            // 如果是SELF_PURCHASE_ERROR，则显示特定提示
            if (errorMsg && (errorMsg.includes('不能购买自己发布的闲置物品') || errorMsg.includes('self purchase')) ) {
                ElMessage.error('不能购买自己发布的商品！');
            } else {
                ElMessage.error(errorMsg);
            }
        }
    }).catch(e=>{
        console.error("购买失败:", e);
        const errorMsg = e && (e.msg || e.message || '');
        if (errorMsg && (errorMsg.includes('请先登录') || errorMsg.includes('请重新登录') || errorMsg.includes('COOKIE_ERROR'))) {
            return;
        }
        ElMessage.error('购买请求失败，请检查登录状态或网络连接');
    });
};

const favoriteButton = (idleItemInfoData) => {
    if (!ensureUserLogin()) {
        return;
    }
    if(isFavorite.value){
        $api.deleteFavorite({
            id: favoriteId.value
        }).then(res=>{
            console.log(res);
            if(res && res.status_code === 1){
                ElMessage.success('已取消收藏！');
                isFavorite.value = false;
            }else {
                ElMessage.error(res ? res.msg : '请求失败');
            }
        }).catch(e=>{
            console.error("取消收藏失败:", e);
            const errorMsg = e && (e.msg || e.message || '');
            if (errorMsg && (errorMsg.includes('请先登录') || errorMsg.includes('请重新登录') || errorMsg.includes('COOKIE_ERROR'))) {
                return;
            }
            ElMessage.error('操作失败，请重试');
        });
    }else {
        $api.addFavorite({
            idleId: idleItemInfoData.id
        }).then(res=>{
            console.log(res);
            if(res && res.status_code === 1){
                savePreferredBuyCount(idleItemInfoData.id, buyCount.value);
                ElMessage.success('已加入收藏！');
                isFavorite.value = true;
                favoriteId.value = res.data;
            }else {
                ElMessage.error(res ? res.msg : '加入收藏失败');
            }
        }).catch(e=>{
            console.error("加入收藏失败:", e);
            const errorMsg = e && (e.msg || e.message || '');
            if (errorMsg && (errorMsg.includes('请先登录') || errorMsg.includes('请重新登录') || errorMsg.includes('COOKIE_ERROR'))) {
                return;
            }
            ElMessage.error('操作失败，请检查登录状态');
        });
    }
};

const cancelReply = () => {
    isReply.value = false;
    toUser.value = idleItemInfo.value.userId;
    toMessage.value = null;
    replyData.value.toUserNickname = '';
    replyData.value.toMessage = '';
};

const sendMessage = () => {
    if (!ensureUserLogin()) {
        return;
    }
    let content = messageContent.value.trim();
    if(toUser.value == null){
        toUser.value = idleItemInfo.value.userId;
    }
    if(content){
        let contentList = content.split(/\r?\n/);
        let contenHtml = contentList[0];
        for(let i=1; i<contentList.length; i++){
            contenHtml += '<br>'+contentList[i];
        }
        $api.sendMessage({
            idleId: idleItemInfo.value.id,
            content: contenHtml,
            toUser: toUser.value,
            toMessage: toMessage.value
        }).then(res=>{
            if(res.status_code === 1){
                ElMessage.success('留言成功！');
                messageContent.value = '';
                cancelReply();
                getAllIdleMessage();
            }else {
                ElMessage.error("留言失败！"+res.msg);
            }
        }).catch(e=>{
            const errorMsg = e && (e.msg || e.message || '');
            if (errorMsg && (errorMsg.includes('请先登录') || errorMsg.includes('请重新登录') || errorMsg.includes('COOKIE_ERROR'))) {
                return;
            }
            ElMessage.error("留言失败！");
        });
    }else{
        ElMessage.error("留言为空！");
    }
};

const scrollToMessage = () => {
    $('html,body').animate({
        scrollTop: $("#replyMessageLocation").offset().top - 100
    }, {duration: 500, easing: "swing"});
};

// 获取状态文本
const getStatusText = (status) => {
    const statusMap = {
        0: '已删除',
        1: '在售',
        2: '审核未通过',
        3: '待审核',
        4: '已下架'
    };
    return statusMap[status] || '未知状态';
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

// 处理图片列表，确保所有图片URL都正确格式化
const processImageList = (imageList) => {
    if (!Array.isArray(imageList)) return [];
    return imageList.map(url => getImageUrl(url));
};

const formatTime = (time) => {
    if (!time) return '';
    const str = String(time);
    if (str.length >= 19) {
        return str.substring(0, 10) + ' ' + str.substring(11, 19);
    }
    return str;
};

onMounted(() => {
    let id = route.query.id;
    $api.getIdleItem({
        id: id
    }).then(res=>{
        console.log(res);
        if(res.data){
            let list = res.data.idleDetails.split(/\r?\n/);
            let str = '';
            for(let i=0; i<list.length; i++){
                str += '<p>'+list[i]+'</p>';
            }
            res.data.idleDetails = str;
            res.data.pictureList = processImageList(JSON.parse(res.data.pictureList));
            idleItemInfo.value = res.data;
            const routeBuyCount = Number(route.query.buyCount);
            const preferredCount = Number.isInteger(routeBuyCount) && routeBuyCount > 0
                ? routeBuyCount
                : getPreferredBuyCount(res.data.id);
            const maxStock = res.data.idleStock && res.data.idleStock > 0 ? res.data.idleStock : 1;
            if (preferredCount && preferredCount > 0) {
                buyCount.value = Math.min(preferredCount, maxStock);
            } else {
                buyCount.value = 1;
            }
            console.log(idleItemInfo.value);
            // 优先从 Pinia Store 获取用户ID
            let userId = userStore.getUserId;
            console.log('userid from pinia', userId);
            
            // 如果 Pinia Store 中没有用户ID，尝试从 Cookie 获取
            if (!userId) {
                userId = getCookie('shUserId');
                console.log('userid from cookie', userId);
            }
            
            // 只有已登录用户才尝试读取当前用户信息，避免公共详情页被跳到登录页
            if (userId && userId !== '') {
                if(userId == idleItemInfo.value.userId){
                    console.log('isMaster');
                    isMaster.value = true;
                }
            }
            checkFavorite();
            getAllIdleMessage();
            getAllIdleReview();
        }
        $('html,body').animate({
            scrollTop: 0
        }, {duration: 500, easing: "swing"});
    });
});

watch(buyCount, (newValue) => {
    if (!idleItemInfo.value || !idleItemInfo.value.id) {
        return;
    }
    if (Number.isInteger(newValue) && newValue > 0) {
        savePreferredBuyCount(idleItemInfo.value.id, newValue);
    }
});
</script>

<style scoped>
    .details-container {
        min-height: 100vh;
        background-color: #f4f5f7;
        padding-bottom: 50px;
    }

    .details-wrapper {
        width: 1120px;
        margin: 0 auto;
    }

    /* 顶部商品卡片 */
    .product-intro-card {
        background: #fff;
        border-radius: 4px;
        padding: 30px;
        display: flex;
        gap: 30px;
        margin-bottom: 20px;
        box-shadow: 0 0 0 1px #eee;
    }

    /* 左侧画廊 */
    .gallery-section {
        width: 400px;
        flex-shrink: 0;
    }

    .main-image-box {
        width: 100%;
        height: 400px;
        border-radius: 4px;
        margin-bottom: 10px;
        background: #f9f9f9; /* 浅灰色背景，图片未加载时占位 */
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        border: none; /* 移除边框，更简洁 */
    }

    .main-image {
        width: 100%;
        height: 100%;
        display: block; /* 消除img底部的间隙 */
    }
    
    /* 覆盖Element Plus Image组件的默认样式，让图片填满容器 */
    :deep(.el-image__inner) {
        width: 100%;
        height: 100%;
        object-fit: cover; /* 改为 cover，让图片填满容器 */
    }

    .thumbnail-list {
        display: flex;
        gap: 10px;
        overflow-x: auto;
        padding-bottom: 5px;
    }

    .thumbnail-item {
        width: 64px;
        height: 64px;
        border: 2px solid transparent;
        border-radius: 4px;
        cursor: pointer;
        opacity: 0.6;
        transition: all 0.2s;
    }
    
    .thumbnail-item.active, .thumbnail-item:hover {
        border-color: #00A1D6;
        opacity: 1;
    }

    .thumbnail-item .el-image {
        width: 100%;
        height: 100%;
        border-radius: 2px;
    }

    /* 右侧信息区 */
    .info-section {
        flex: 1;
        position: relative;
    }

    .product-title {
        font-size: 20px;
        font-weight: 500;
        color: #222;
        line-height: 28px;
        margin-bottom: 15px;
    }

    .product-meta {
        background: #f4f5f7;
        padding: 20px;
        border-radius: 4px;
        margin-bottom: 20px;
    }

    .price-row {
        display: flex;
        align-items: flex-end; /* 底部对齐 */
        margin-bottom: 20px;
        line-height: 1;
    }

    .price-symbol {
        font-size: 18px;
        color: #FF5000;
        font-weight: 600;
        margin-right: 4px;
        margin-bottom: 4px; /* 微调对齐 */
    }

    .price-value {
        font-size: 32px;
        color: #FF5000;
        font-weight: 600;
        font-family: Arial, sans-serif;
        margin-right: 15px;
        line-height: 1;
    }

    .status-badge {
        display: inline-block;
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;
        background: #f5f5f5;
        color: #999;
        border: 1px solid #e0e0e0;
        margin-bottom: 6px; /* 与价格底部对齐微调 */
    }
    
    .status-badge.on-sale {
        background: #e6f7ff;
        color: #00A1D6;
        border-color: #91d5ff;
    }
    
    .status-badge.rejected {
        background: #fff1f0;
        color: #F56C6C;
        border-color: #ffccc7;
    }
    
    .status-badge.offline {
        background: #f5f5f5;
        color: #999;
        border-color: #e0e0e0;
    }

    .info-grid {
        display: flex;
        flex-direction: column; /* 恢复为垂直排列 */
        gap: 12px;
    }

    .info-item {
        display: flex;
        align-items: center; /* 确保垂直居中对齐 */
        font-size: 14px;
        line-height: 1.5; /* 统一行高 */
    }

    .label {
        color: #999;
        width: 70px;
        margin-right: 20px; /* 增加间距 */
        text-align: justify;
        text-align-last: justify;
        flex-shrink: 0; /* 防止压缩 */
    }

    .value {
        color: #222;
        font-weight: 500;
        flex: 1; /* 占据剩余空间 */
        display: flex;
        align-items: center; /* 值内容垂直居中 */
    }

    /* 卖家小卡片 */
    .seller-card-mini {
        display: flex;
        align-items: center;
        padding: 15px 0;
        border-top: 1px solid #e5e9ef;
        border-bottom: 1px solid #e5e9ef;
        margin-bottom: 25px;
    }

    .seller-info {
        margin-left: 12px;
        flex: 1;
    }

    .seller-name {
        font-size: 14px;
        color: #222;
        font-weight: 600;
        margin-bottom: 4px;
    }

    .seller-desc {
        font-size: 12px;
        color: #999;
    }

    .contact-btn {
        color: #00A1D6;
        border-color: #00A1D6;
    }

    /* 底部操作区 */
    .action-area {
        display: flex;
        gap: 15px;
    }

    .buy-now-btn {
        width: 140px;
        height: 48px;
        font-size: 16px;
        background-color: #00A1D6;
        border-color: #00A1D6;
    }

    .buy-count-input {
        width: 140px;
    }
    
    .buy-now-btn:hover {
        background-color: #00b5e5;
        border-color: #00b5e5;
    }

    .add-fav-btn {
        width: 140px;
        height: 48px;
        font-size: 16px;
        color: #222;
        border-color: #e5e9ef;
    }

    .add-fav-btn.active {
        color: #FB7299;
        border-color: #FB7299;
        background: #fff0f6;
    }

    /* 详情主体 */
    .details-body {
        display: flex;
        gap: 20px;
    }

    .left-column {
        width: 840px;
    }

    .right-column {
        width: 260px;
    }

    .section-card {
        background: #fff;
        border-radius: 4px;
        margin-bottom: 20px;
        box-shadow: 0 0 0 1px #eee;
    }

    .card-header {
        height: 50px;
        line-height: 50px;
        padding: 0 20px;
        font-size: 16px;
        color: #222;
        border-bottom: 1px solid #e5e9ef;
    }

    .header-count {
        font-size: 12px;
        color: #999;
        margin-left: 5px;
    }

    .card-content {
        padding: 30px;
        min-height: 200px;
        font-size: 14px;
        line-height: 1.8;
        color: #222;
    }

    /* 留言区样式优化 */
    .message-card {
        padding-bottom: 20px;
    }

    .review-card {
        padding-bottom: 20px;
    }

    .review-list {
        padding: 10px 20px 0;
    }

    .review-item {
        display: flex;
        padding: 14px 0;
        border-bottom: 1px solid #e5e9ef;
    }

    .review-item:last-child {
        border-bottom: none;
    }

    .review-head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;
    }

    .review-content {
        font-size: 14px;
        color: #222;
        line-height: 1.7;
        margin-bottom: 4px;
    }

    .message-editor {
        padding: 20px;
        background: #f4f5f7;
        margin: 20px;
        border-radius: 4px;
    }

    .reply-tag {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 12px;
        color: #666;
        margin-bottom: 8px;
        background: #fff;
        padding: 2px 8px;
        border-radius: 2px;
        width: fit-content;
    }

    .close-reply {
        cursor: pointer;
    }

    .editor-footer {
        margin-top: 10px;
        text-align: right;
    }

    .message-list {
        padding: 0 20px;
    }

    .message-item {
        display: flex;
        padding: 20px 0;
        border-bottom: 1px solid #e5e9ef;
    }

    .message-item:last-child {
        border-bottom: none;
    }

    .message-avatar {
        margin-right: 15px;
        flex-shrink: 0;
    }

    .message-body {
        flex: 1;
    }

    .message-header {
        margin-bottom: 5px;
    }

    .mes-nickname {
        font-size: 13px;
        color: #6d757a;
        font-weight: 600;
        margin-right: 10px;
        cursor: pointer;
    }

    .mes-time {
        font-size: 12px;
        color: #999;
    }

    .message-content {
        font-size: 14px;
        color: #222;
        line-height: 20px;
    }

    .reply-target {
        color: #00A1D6;
        margin-right: 5px;
    }

    .message-actions {
        margin-top: 8px;
        font-size: 12px;
        color: #999;
    }

    .action-btn {
        cursor: pointer;
    }
    
    .action-btn:hover {
        color: #00A1D6;
    }

    .empty-message {
        text-align: center;
        padding: 40px 0;
        color: #999;
    }

    /* 安全贴士 */
    .safety-card {
        padding: 20px;
        background: #fff;
    }

    .safety-title {
        font-size: 16px;
        font-weight: 500;
        color: #222;
        margin-bottom: 15px;
    }

    .safety-item {
        font-size: 12px;
        color: #999;
        line-height: 1.6;
        margin-bottom: 8px;
    }
</style>
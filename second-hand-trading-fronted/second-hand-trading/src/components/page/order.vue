<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="order-page-container">
                <div class="idle-info-container" @click="toDetails(orderInfo.idleItem.id)">
                    <el-image
                            style="width: 150px; height: 150px;"
                            :src="orderInfo.idleItem.imgUrl"
                            fit="cover">
                        <template #error>
                            <div class="image-slot">
                                <el-icon><Picture /></el-icon>
                                <span>图片加载失败</span>
                            </div>
                        </template>
                    </el-image>
                    <div class="idle-info-title">{{orderInfo.userId==userId?'买到的':'卖出的'}}：{{orderInfo.idleItem.idleName}}</div>
                    <div class="idle-info-price">￥{{orderInfo.orderPrice}}（x{{orderInfo.orderCount || 1}}）</div>

                </div>
                <div class="address-container">
                    <template v-if="addressInfo.detailAddress">
                        <div class="order-info-container" @click.stop="selectAddressDialog" :style="orderInfo.userId==userId&&orderInfo.orderStatus===0?'cursor: pointer;':''">
                            <div class="order-info-title">地址信息</div>
                            <div class="order-info-grid">
                                <div class="order-info-row">
                                    <span class="address-label">收货人：</span>
                                    <span class="info-value">{{addressInfo.consigneeName}}</span>
                                </div>
                                <div class="order-info-row">
                                    <span class="address-label">收货人电话：</span>
                                    <span class="info-value">{{addressInfo.consigneePhone}}</span>
                                </div>
                                <div class="order-info-row">
                                    <span class="address-label">收货地址：</span>
                                    <span class="info-value">{{addressInfo.detailAddress}}</span>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template v-else>
                        <div class="address-empty">
                            <div class="address-empty-icon">
                                <el-icon><Location /></el-icon>
                            </div>
                            <div class="address-empty-content">
                                <div class="address-empty-title">您还没有收货地址</div>
                                <div class="address-empty-desc">请先添加收货地址以便完成订单</div>
                            </div>
                            <div class="address-empty-actions" @click.stop>
                                <el-button @click.stop="goToAddAddress" type="primary">新增收货地址</el-button>
                            </div>
                        </div>
                    </template>
                </div>
                <el-dialog
                        title="选择地址"
                        v-model="addressDialogVisible"
                        width="800px">
                    <div class="address-dialog-toolbar">
                        <div class="address-dialog-toolbar-tip">没有合适的地址？可以先新增一个再回来选择。</div>
                        <el-button type="primary" plain @click="goToAddAddress">新增收货地址</el-button>
                    </div>
                    <template v-if="addressData.length > 0">
                        <el-table
                                stripe
                                :data="addressData"
                                style="width: 100%">
                            <el-table-column
                                    prop="consigneeName"
                                    label="收货人姓名"
                                    width="120">
                            </el-table-column>
                            <el-table-column
                                    prop="consigneePhone"
                                    label="手机号"
                                    width="140">
                            </el-table-column>
                            <el-table-column
                                    prop="detailAddressText"
                                    label="地址">
                            </el-table-column>
                            <el-table-column label=" " width="120">
                                <template #default="scope">
                                    <el-button
                                            v-if="!isSelectedAddress(scope.row)"
                                            size="mini"
                                            @click="selectAddress(scope.$index, scope.row)">选择
                                    </el-button>
                                    <span v-else style="color: #999; display: inline-flex; align-items: center; justify-content: flex-start; height: 100%; padding-left: 8px;">已选中</span>
                                </template>
                            </el-table-column>
                        </el-table>
                    </template>
                    <div v-else class="address-dialog-empty">
                        <div class="address-dialog-empty-icon">
                            <el-icon><Location /></el-icon>
                        </div>
                        <div class="address-dialog-empty-text">您还没有收货地址</div>
                        <div class="address-dialog-empty-desc">请先添加收货地址以便选择</div>
                        <el-button type="primary" @click="goToAddAddress" style="margin-top: 20px;">前往个人中心新增收货地址</el-button>
                    </div>
                </el-dialog>
                <div class="address-container">
                    <div class="order-info-container">
                        <div class="order-info-title">订单信息（{{orderStatus[orderInfo.orderStatus]}}）</div>
                        
                        <div v-if="orderInfo.orderStatus === 0 && countdown" class="countdown-warning">
                            <el-icon style="margin-right: 5px;"><Clock /></el-icon>
                            请在 <span class="countdown-time">{{countdown}}</span> 内完成支付，否则订单将自动取消
                        </div>
                        
                        <div class="order-info-grid">
                            <div class="order-info-row">
                                <span class="info-label">订单编号</span>
                                <span class="info-value">{{orderInfo.orderNumber}}</span>
                            </div>
                            <div class="order-info-row">
                                <span class="info-label">支付状态</span>
                                <span class="info-value" :class="orderInfo.paymentStatus===0?'status-unpaid':'status-paid'">
                                    {{orderInfo.paymentStatus===0?'未支付':'已支付'}}
                                </span>
                            </div>
                            <div class="order-info-row" v-if="orderInfo.paymentWay">
                                <span class="info-label">支付方式</span>
                                <span class="info-value">{{orderInfo.paymentWay}}</span>
                            </div>
                            <div class="order-info-row">
                                <span class="info-label">购买数量</span>
                                <span class="info-value">{{orderInfo.orderCount || 1}}</span>
                            </div>
                            <div class="order-info-row">
                                <span class="info-label">创建时间</span>
                                <span class="info-value">{{orderInfo.createTime.substring(0, 10) + ' ' + orderInfo.createTime.substring(11, 19)}}</span>
                            </div>
                            <div class="order-info-row" v-if="orderInfo.paymentTime">
                                <span class="info-label">支付时间</span>
                                <span class="info-value">{{orderInfo.paymentTime.substring(0, 10) + ' ' + orderInfo.paymentTime.substring(11, 19)}}</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="menu">
                    <el-button v-if="userId==orderInfo.userId&&orderInfo.orderStatus===0" type="danger" plain @click="changeOrderStatus(4,orderInfo)">取消订单</el-button>
                    <el-button v-if="userId==orderInfo.userId&&orderInfo.orderStatus===0" type="primary" @click="changeOrderStatus(1,orderInfo)">立即支付</el-button>
                    <el-button v-if="userId==orderInfo.idleItem.userId&&orderInfo.orderStatus===1" type="primary" @click="changeOrderStatus(2,orderInfo)">发货</el-button>
                    <el-button v-if="userId==orderInfo.userId&&orderInfo.orderStatus===2" type="primary" @click="changeOrderStatus(3,orderInfo)">确认收货</el-button>
                    <el-button v-if="userId==orderInfo.userId&&orderInfo.orderStatus===3&&!orderReview" type="warning" @click="reviewDialogVisible=true">去评价</el-button>
                    <el-button v-if="userId==orderInfo.userId&&orderInfo.orderStatus===3&&orderReview" type="success" plain disabled>已评价</el-button>
                </div>

                <el-dialog title="订单评价" v-model="reviewDialogVisible" width="520px">
                    <el-form label-width="72px">
                        <el-form-item label="评分">
                            <el-rate v-model="reviewForm.score" :max="5"></el-rate>
                        </el-form-item>
                        <el-form-item label="内容">
                            <el-input
                                type="textarea"
                                v-model="reviewForm.content"
                                :rows="4"
                                maxlength="300"
                                show-word-limit
                                placeholder="请填写你对该商品和卖家的真实评价"
                            ></el-input>
                        </el-form-item>
                    </el-form>
                    <template #footer>
                        <el-button @click="reviewDialogVisible=false">取消</el-button>
                        <el-button type="primary" @click="submitReview">提交评价</el-button>
                    </template>
                </el-dialog>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Clock, Location } from '@element-plus/icons-vue';
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

const addressDialogVisible = ref(false);
const addressData = ref([]);
const orderStatus = ref(['待付款', '待发货', '待收货', '已完成', '已取消']);
const countdown = ref(''); // 倒计时显示
const countdownTimer = ref(null); // 倒计时定时器
const reviewDialogVisible = ref(false);
const orderReview = ref(null);
const reviewForm = ref({
    score: 5,
    content: ''
});
const orderInfo = ref({
    createTime: "",
    id: 0,
    idleId: 0,
    idleItem: {
        id: '',
        idleName: '',
        idleDetails: '',
        pictureList: [],
        idlePrice: 0,
        idlePlace: '',
        idleLabel: '',
        idleStatus: -1,
        userId: '',
    },
    orderNumber: "",
    orderPrice: 0,
    orderCount: 1,
    orderStatus: 0,
    paymentStatus: 0,
    paymentTime: "",
    paymentWay: "",
    userId: 0
});
const addressInfo = ref({
    id: '',
    update: false,
    consigneeName: '',
    consigneePhone: '',
    detailAddress: ''
});
const userId = ref('');

const getCookie = (cname) => {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i].trim();
        if (c.indexOf(name)===0) return c.substring(name.length,c.length);
    }
    return "0";
};

const toDetails = (id) => {
    router.replace({path: 'details', query: {id: id}});
};

const selectAddressDialog = () => {
    if(orderInfo.value.userId == userId.value && orderInfo.value.orderStatus === 0){
        addressDialogVisible.value = true;
        if(addressData.value.length === 0){
            getAddressData();
        }
    }
};

const goToAddAddress = () => {
    // 保存当前订单ID到sessionStorage，以便从个人中心返回时能继续操作
    sessionStorage.setItem('returnToOrder', route.query.id);
    // 跳转到个人中心并自动打开新增地址页面
    router.push({ path: '/me', query: { addAddress: 'true' } });
};

const getAddressData = () => {
    $api.getAddress().then(res => {
        if (res.status_code === 1) {
            let data = res.data;
            for (let i = 0; i < data.length; i++) {
                data[i].detailAddressText = (data[i].provinceName || '') + (data[i].cityName || '') + (data[i].regionName || '') + (data[i].detailAddress || '');
            }
            console.log(data);
            addressData.value = data;
            if(!addressInfo.value.update){
                for(let i=0; i<data.length; i++){
                    if(data[i].defaultFlag){
                        selectAddress(i, data[i]);
                    }
                }
            }
        }
    });
};

const isSelectedAddress = (row) => {
    // 判断当前行是否是已选中的地址
    return row.consigneeName === addressInfo.value.consigneeName && 
           row.consigneePhone === addressInfo.value.consigneePhone && 
           row.detailAddressText === addressInfo.value.detailAddress;
};

const selectAddress = (i, item) => {
    addressDialogVisible.value = false;
    console.log(item, addressInfo.value);
    addressInfo.value.consigneeName = item.consigneeName;
    addressInfo.value.consigneePhone = item.consigneePhone;
    addressInfo.value.detailAddress = item.detailAddressText;
    if(addressInfo.value.update){
        $api.updateOrderAddress({
            id: addressInfo.value.id,
            consigneeName: item.consigneeName || '',
            consigneePhone: item.consigneePhone || '',
            detailAddress: item.detailAddressText || ''
        });
    }else{
        $api.addOrderAddress({
            orderId: orderInfo.value.id,
            consigneeName: item.consigneeName || '',
            consigneePhone: item.consigneePhone || '',
            detailAddress: item.detailAddressText || ''
        }).then(res=>{
            if(res.status_code === 1){
                addressInfo.value.update = true;
                addressInfo.value.id = res.data.id;
            }else {
                ElMessage.error(res.msg);
            }
        });
    }
};

const changeOrderStatus = (orderStatusValue, orderInfoData) => {
    if (orderStatusValue === 1) {
        // 支付订单 - 只打开支付页面，不更新订单状态
        console.log('zhifu');
        if(!addressInfo.value.detailAddress){
            ElMessage.error('请选择地址！');
        }else{
            ElMessageBox.confirm('即将跳转到支付宝模拟支付页面，请在页面中确认支付。', '支付订单', {
                confirmButtonText: '去支付',
                cancelButtonText: '取消',
                type: 'warning',
                lockScroll: false
            }).then(() => {
                // 使用api接口获取支付页面内容
                $api.updateAlipay({
                    name: orderInfoData.id,
                    no: Math.random().toString(36).substr(2)
                }).then(res => {
                    // 创建一个新的窗口显示支付页面
                    const newWindow = window.open('', '_blank');
                    if (newWindow) {
                        newWindow.document.write(res);
                        newWindow.document.close();
                    } else {
                        // 如果弹出窗口被阻止，提示用户允许弹出窗口
                        alert('请允许弹出窗口以完成支付！');
                    }
                }).catch(err => {
                    console.error('获取支付页面失败:', err);
                    // 如果API调用失败，尝试直接跳转
                    window.location.href = `/alipay/pay?name=${orderInfoData.id}&no=${Math.random().toString(36).substr(2)}`;
                });
            }).catch(() => {
                // 用户取消支付
            });
        }
    } else if (orderStatusValue === 4) {
        // 取消订单
        ElMessageBox.confirm('确认要取消该订单吗？取消后商品将恢复上架。', '取消订单', {
            confirmButtonText: '确认取消',
            cancelButtonText: '不取消',
            type: 'warning',
            lockScroll: false
        }).then(() => {
            $api.cancelOrder({
                id: orderInfoData.id
            }).then(res => {
                if (res.status_code === 1) {
                    ElMessage.success('订单已取消，商品已恢复上架');
                    orderInfo.value.orderStatus = orderStatusValue;
                    // 清除倒计时
                    if (countdownTimer.value) {
                        clearInterval(countdownTimer.value);
                        countdown.value = '';
                    }
                } else {
                    ElMessage.error(res.msg || '取消订单失败');
                }
            }).catch(e => {
                console.error('取消订单失败:', e);
                ElMessage.error('取消订单失败，请稍后重试');
            });
        }).catch(() => {
            // 用户取消操作
        });
    } else {
        // 其他状态更新（发货、确认收货等）
        // 发货确认
        if (orderStatusValue === 2) {
            ElMessageBox.confirm('确认要为该订单发货吗？发货后买家将可以确认收货。', '发货确认', {
                confirmButtonText: '确认发货',
                cancelButtonText: '暂不发货',
                type: 'warning',
                lockScroll: false
            }).then(() => {
                updateOrderStatus(orderInfoData.id, orderStatusValue);
            }).catch(() => {
                // 用户取消操作
            });
        }
        // 确认收货确认
        else if (orderStatusValue === 3) {
            ElMessageBox.confirm('确认已收到货物吗？确认收货后将完成交易。', '收货确认', {
                confirmButtonText: '确认收货',
                cancelButtonText: '未收到',
                type: 'warning',
                lockScroll: false
            }).then(() => {
                updateOrderStatus(orderInfoData.id, orderStatusValue);
            }).catch(() => {
                // 用户取消操作
            });
        }
        // 其他操作
        else {
            updateOrderStatus(orderInfoData.id, orderStatusValue);
        }
    }
};

// 提取订单状态更新逻辑
const updateOrderStatus = (orderId, status) => {
    $api.updateOrder({
        id: orderId,
        orderStatus: status,
    }).then(res => {
        if (res.status_code === 1) {
            ElMessage.success('操作成功！');
            orderInfo.value.orderStatus = status;
            if (status === 3) {
                loadOrderReview(orderId);
            }
        }
    });
}

const loadOrderReview = (orderId) => {
    $api.getOrderReview({ orderId }).then(res => {
        if (res && res.status_code === 1) {
            orderReview.value = res.data || null;
        }
    }).catch(() => {
        orderReview.value = null;
    });
};

const submitReview = () => {
    if (!reviewForm.value.score || reviewForm.value.score < 1 || reviewForm.value.score > 5) {
        ElMessage.error('请先选择1到5星评分');
        return;
    }
    const content = (reviewForm.value.content || '').trim();
    if (!content) {
        ElMessage.error('请填写评价内容');
        return;
    }

    $api.addReview({
        orderId: orderInfo.value.id,
        score: reviewForm.value.score,
        content
    }).then(res => {
        if (res && res.status_code === 1) {
            ElMessage.success('评价成功');
            reviewDialogVisible.value = false;
            loadOrderReview(orderInfo.value.id);
        } else {
            ElMessage.error((res && res.msg) || '评价失败');
        }
    }).catch(() => {
        ElMessage.error('评价失败，请稍后重试');
    });
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
}

// 计算倒计时
const startCountdown = (createTime) => {
    // 15分钟 = 15 * 60 * 1000 毫秒
    const timeoutMinutes = 15;
    const timeoutMs = timeoutMinutes * 60 * 1000;
    
    const updateCountdown = () => {
        const now = new Date().getTime();
        const orderTime = new Date(createTime).getTime();
        const remaining = timeoutMs - (now - orderTime);
        
        if (remaining <= 0) {
            countdown.value = '';
            clearInterval(countdownTimer.value);
            ElMessage.warning('订单已超时，请刷新页面');
        } else {
            const minutes = Math.floor(remaining / 60000);
            const seconds = Math.floor((remaining % 60000) / 1000);
            countdown.value = `${minutes}分${seconds}秒`;
        }
    };
    
    updateCountdown();
    countdownTimer.value = setInterval(updateCountdown, 1000);
};

onMounted(() => {
    // 优先从 Pinia Store 获取用户ID
    userId.value = userStore.getUserId || getCookie('shUserId');
    console.log('userId', userId.value, getCookie('shUserId'), userStore.getUserId);
    let orderId = route.query.id;
    console.log(orderId);
    $api.getOrder({
        id: orderId
    }).then(res => {
        console.log(res);
        if (res.status_code === 1) {
            if (res.data.idleItem) {
                let imgList = JSON.parse(res.data.idleItem.pictureList);
                if (imgList.length > 0) {
                    res.data.idleItem.imgUrl = getImageUrl(imgList[0]);
                } else {
                    res.data.idleItem.imgUrl = '';
                }
            } else {
                res.data.idleItem = {
                    idleName: '',
                    imgUrl: ''
                };
            }
            orderInfo.value = res.data;
            loadOrderReview(orderInfo.value.id);
            
            // 如果订单是待支付状态，启动倒计时
            if (orderInfo.value.orderStatus === 0) {
                startCountdown(orderInfo.value.createTime);
            }
            
            $api.getOrderAddress({
                orderId: orderInfo.value.id
            }).then(res=>{
                if(res.data){
                    // 清理数据，避免NaN值
                    addressInfo.value.id = res.data.id || '';
                    addressInfo.value.update = true;
                    addressInfo.value.consigneeName = res.data.consigneeName || '';
                    addressInfo.value.consigneePhone = res.data.consigneePhone || '';
                    addressInfo.value.detailAddress = res.data.detailAddress || '';
                }else{
                    getAddressData();
                }
            });
        }
    });
});

// 订单状态轮询定时器
const orderPollingTimer = ref(null);

// 轮询订单支付状态
const pollOrderStatus = () => {
    // 每5秒检查一次订单状态
    orderPollingTimer.value = setInterval(() => {
        const orderId = route.query.id;
        $api.getOrder({
            id: orderId
        }).then(res => {
            if (res.status_code === 1) {
                const currentPaymentStatus = orderInfo.value.paymentStatus;
                const currentOrderStatus = orderInfo.value.orderStatus;
                
                // 更新订单信息，同时保留图片URL
                if (res.data.idleItem) {
                    // 保存原始图片URL，避免轮询时丢失
                    const preservedImgUrl = orderInfo.value.idleItem ? orderInfo.value.idleItem.imgUrl : '';
                    let imgList = JSON.parse(res.data.idleItem.pictureList);
                    if (imgList.length > 0) {
                        // 如果之前已经有图片URL，使用之前的，避免重新生成可能过期的链接
                        if (preservedImgUrl) {
                            res.data.idleItem.imgUrl = preservedImgUrl;
                        } else {
                            res.data.idleItem.imgUrl = getImageUrl(imgList[0]);
                        }
                    } else {
                        res.data.idleItem.imgUrl = '';
                    }
                }
                orderInfo.value = res.data;
                
                // 如果支付状态或订单状态发生了变化
                if (currentPaymentStatus !== res.data.paymentStatus || currentOrderStatus !== res.data.orderStatus) {
                    // 清除轮询定时器
                    if (orderPollingTimer.value) {
                        clearInterval(orderPollingTimer.value);
                        orderPollingTimer.value = null;
                    }
                    
                    // 显示状态变更消息
                    if (res.data.paymentStatus === 1) { // 已支付
                        ElMessage.success('订单支付成功！');
                    }
                    
                    // 重新启动倒计时（如果订单仍在待支付状态且未超时）
                    if (res.data.orderStatus === 0 && res.data.createTime) {
                        if (countdownTimer.value) {
                            clearInterval(countdownTimer.value);
                        }
                        startCountdown(res.data.createTime);
                    }
                }
            }
        }).catch(error => {
            console.error('轮询订单状态失败:', error);
        });
    }, 5000); // 每5秒轮询一次
};

// 初始化订单数据
// 组件销毁时清除定时器
onUnmounted(() => {
    if (countdownTimer.value) {
        clearInterval(countdownTimer.value);
    }
    if (orderPollingTimer.value) {
        clearInterval(orderPollingTimer.value);
    }
});
</script>

<style scoped>
    .order-page-container {
        min-height: 85vh;
    }

    .idle-info-container {
        width: 100%;
        display: flex;
        border-bottom: 20px solid #f6f6f6;
        padding: 20px;
        cursor: pointer;
    }

    .idle-info-title {
        font-size: 18px;
        font-weight: 600;
        max-width: 750px;
        margin-left: 10px;
    }

    .idle-info-price {
        font-size: 18px;
        color: red;
        margin-left: 10px;
    }

    .address-container {
        min-height: 60px;
        padding: 20px;
        background: #fff;
    }

    .address-title-row {
        display: flex;
        flex-direction: column;
        gap: 16px;
        margin-bottom: 8px;
        padding: 16px;
        background: #f9f9f9;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        transition: all 0.2s;
    }

    .address-info {
        display: flex;
        flex-direction: column;
        gap: 16px;
        line-height: 1.5;
    }

    .address-info span {
        display: flex;
        align-items: center;
    }

    .address-label {
        width: 90px;
        color: #666;
        font-size: 14px;
        flex-shrink: 0;
        font-weight: normal;
    }

    .address-detials {
        font-size: 14px;
        color: #606266;
        padding: 10px 12px;
        background: #fdfdfd;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        line-height: 1.6;
        align-self: flex-start;
    }

    .address-title-row:hover {
        cursor: pointer;
        border-color: #c6e2ff;
        box-shadow: 0 0 8px 0 rgba(102, 177, 255, 0.2);
    }

    .order-info-container:hover {
        border-color: #c6e2ff;
        box-shadow: 0 0 8px 0 rgba(102, 177, 255, 0.2);
    }

    .address-detials:hover {
        cursor: pointer;
        border-color: #c6e2ff;
    }

    .address-empty {
        display: flex;
        align-items: center;
        gap: 20px;
        padding: 20px;
        background: #f8f9fa;
        border-radius: 8px;
        border: 1px dashed #d9d9d9;
    }

    .address-empty-icon {
        font-size: 48px;
        color: #bfbfbf;
        flex-shrink: 0;
    }

    .address-empty-content {
        flex: 1;
    }

    .address-empty-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 6px;
    }

    .address-empty-desc {
        font-size: 14px;
        color: #666;
    }

    .address-empty-actions {
        display: flex;
        gap: 10px;
        flex-shrink: 0;
    }

    .address-dialog-toolbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        margin-bottom: 16px;
        padding: 12px 16px;
        background: linear-gradient(135deg, rgba(64, 158, 255, 0.08), rgba(64, 158, 255, 0.02));
        border: 1px solid rgba(64, 158, 255, 0.12);
        border-radius: 12px;
    }

    .address-dialog-toolbar-tip {
        color: #606266;
        font-size: 14px;
        line-height: 1.5;
    }

    .address-dialog-empty {
        text-align: center;
        padding: 60px 20px;
    }

    .address-dialog-empty-icon {
        font-size: 64px;
        color: #bfbfbf;
        margin-bottom: 16px;
    }

    .address-dialog-empty-text {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin-bottom: 8px;
    }

    .address-dialog-empty-desc {
        font-size: 14px;
        color: #666;
    }

    .order-info-container {
        display: flex;
        flex-direction: column;
        gap: 16px;
        margin-bottom: 8px;
        padding: 16px;
        background: #f9f9f9;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        transition: all 0.2s;
    }
    
    .order-info-title {
        font-size: 16px;
        font-weight: 600;
        color: #222;
        margin-bottom: 15px;
    }
    
    .order-info-grid {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
    
    .order-info-row {
        display: flex;
        align-items: center;
        line-height: 1.5;
        padding: 8px 12px;
        background: #f9f9f9;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        margin-bottom: 8px;
    }
    
    .info-label {
        width: 90px;
        color: #666;
        font-size: 14px;
        flex-shrink: 0;
    }
    
    .info-value {
        color: #222;
        font-size: 14px;
        flex: 1;
    }
    
    .status-unpaid {
        color: #ff4d4f;
        font-weight: 500;
    }
    
    .status-paid {
        color: #52c41a;
        font-weight: 500;
    }

    .countdown-warning {
        display: flex;
        align-items: center;
        background-color: #fff7e6;
        border: 1px solid #ffd591;
        border-radius: 4px;
        padding: 12px 16px;
        margin-bottom: 15px;
        color: #fa8c16;
        font-size: 14px;
    }

    .countdown-time {
        color: #ff4d4f;
        font-weight: 600;
        font-size: 16px;
        margin: 0 4px;
    }

    .order-info-item {
        margin: 10px 0;
        font-size: 14px;
        color: #444444;
    }

    .menu {
        padding: 20px;
        display: flex;
        gap: 12px;
        background: #fff;
        border-top: 1px solid #f0f0f0;
    }
</style>
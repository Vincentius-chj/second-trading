<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="release-idle-container">
                <div class="release-idle-container-title">发布闲置</div>
                <div class="release-idle-container-form">
                    <el-input placeholder="请输入闲置名称" v-model="idleItemInfo.idleName"
                              maxlength="10"
                              show-word-limit>
                    </el-input>
                    <el-input
                            class="release-idle-detiles-text"
                            type="textarea"
                            autosize
                            placeholder="请输入闲置的详细介绍"
                            v-model="idleItemInfo.idleDetails"
                            maxlength="1000"
                            show-word-limit>
                    </el-input>
                    <div class="form-row">
                        <div class="form-item">
                            <div class="release-tip">发货地址</div>
                            <template v-if="addressList.length > 0">
                                <div class="address-select-row">
                                    <el-select v-model="idleItemInfo.idlePlace" placeholder="请选择或新建地址" style="width: 100%;" filterable allow-create default-first-option>
                                        <el-option
                                            v-for="addr in addressList"
                                            :key="addr.id"
                                            :label="addr.detailAddressText || addr.detailAddress"
                                            :value="addr.detailAddressText || addr.detailAddress">
                                        </el-option>
                                    </el-select>
                                    <el-button
                                        type="primary"
                                        plain
                                        @click="goToAddAddress"
                                        class="address-quick-btn">
                                        新建地址
                                    </el-button>
                                </div>
                            </template>
                            <template v-else>
                                <div class="location-empty">
                                    <div class="location-empty-content">
                                        <el-icon class="location-empty-icon"><Location /></el-icon>
                                        <div class="location-empty-text">
                                            <div class="location-empty-title">您还没有保存的地址</div>
                                            <div class="location-empty-desc">请先添加发货地址后再发布</div>
                                        </div>
                                    </div>
                                    <div class="location-empty-actions">
                                        <el-input 
                                            v-model="idleItemInfo.idlePlace" 
                                            placeholder="请输入发货地址" 
                                            style="flex: 1;"
                                            clearable>
                                        </el-input>
                                        <el-button 
                                            type="primary" 
                                            plain 
                                            @click="goToAddAddress"
                                            style="margin-left: 10px;">
                                            新增地址
                                        </el-button>
                                    </div>
                                </div>
                            </template>
                        </div>
                        <div class="form-item">
                            <div class="release-tip">闲置类别</div>
                            <el-select v-model="idleItemInfo.idleLabel" placeholder="请选择类别" style="width: 100%;">
                                <el-option
                                        v-for="item in options2"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                </el-option>
                            </el-select>
                        </div>
                        <div class="form-item" v-show="idleItemInfo.idleLabel !== 5">
                            <div class="release-tip">价格</div>
                            <el-input-number v-model="idleItemInfo.idlePrice" :precision="2" :step="10" :min="0" :max="10000000" style="width: 100%;">
                            </el-input-number>
                        </div>
                        <div class="form-item" v-show="idleItemInfo.idleLabel !== 5">
                            <div class="release-tip">库存数量</div>
                            <el-input-number v-model="idleItemInfo.idleStock" :step="1" :min="1" :max="9999" style="width: 100%;">
                            </el-input-number>
                        </div>
                    </div>
                    <div class="release-idle-container-picture">
                        <div class="picture-header">
                            <div class="release-idle-container-picture-title">上传闲置照片</div>
                        </div>
                        <el-upload
                                :action="uploadAction"
                                :with-credentials="true"
                                :on-preview="fileHandlePreview"
                                :on-remove="fileHandleRemove"
                                :on-success="fileHandleSuccess"
                                :on-error="fileHandleError"
                                :show-file-list="false"
                                :limit="10"
                                :on-exceed="handleExceed"
                                accept="image/*"
                                drag
                                multiple>
                            <i class="el-icon-upload"></i>
                            <div class="el-upload__text">将图片拖到此处，或<em>点击上传</em></div>
                            <template #tip>
                                                        <div class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
                                                    </template>
                        </el-upload>
                        <div class="picture-list">
                            <div v-for="(img,index) in imgList" :key="index" class="picture-item">
                                <el-image 
                                    style="width: 100%; height: 100%;" 
                                    fit="cover"
                                    :src="getImageUrl(img)"
                                    :preview-src-list="imgList.map(i => getImageUrl(i))">
                                    <template #error>
                                        <div class="image-slot">
                                            <el-icon><Picture /></el-icon>
                                        </div>
                                    </template>
                                </el-image>
                                <div class="picture-mask">
                                    <el-icon class="icon-btn delete-btn" @click.stop="deleteImage(index)"><Delete /></el-icon>
                                </div>
                            </div>
                        </div>
                        <el-dialog v-model="imgDialogVisible">
                            <img width="100%" :src="dialogImageUrl" alt="">
                        </el-dialog>
                    </div>
                    <div style="display: flex;justify-content: center;margin-top: 30px;margin-bottom: 30px;">
                        <el-button type="primary" plain @click="releaseButton">确认发布</el-button>
                    </div>
                </div>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script setup>
import { ref, computed, getCurrentInstance, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Delete, Location, Picture } from '@element-plus/icons-vue';
import AppHead from '../common/AppHeader.vue';
import AppBody from '../common/AppPageBody.vue';
import AppFoot from '../common/AppFoot.vue';
import { IDLE_CATEGORY_OPTIONS } from '@/constants/idle-category';
import request from '@/utils/request';

const { proxy } = getCurrentInstance();
const $api = proxy.$api;
const router = useRouter();

// 计算上传地址，使用 request.js 中的 baseURL
const uploadAction = computed(() => {
    console.log("路径" + request.defaults.baseURL);
    return `${request.defaults.baseURL}/upload`;
});

const imgDialogVisible = ref(false);
const dialogImageUrl = ref('');
const showFileList = ref(true);
const addressList = ref([]); // 地址列表
const options2 = IDLE_CATEGORY_OPTIONS;
const imgList = ref([]);
const idleItemInfo = ref({
    idleName: '',
    idleDetails: '',
    pictureList: '',
    idlePrice: 0,
    idleStock: 1,
    idlePlace: '',
    idleLabel: ''
});

const fileHandleRemove = (file, fileList) => {
    console.log(file, fileList);
    // 确保imgList中包含要删除的图片URL
    if (file && file.response && file.response.data) {
        const index = imgList.value.indexOf(file.response.data);
        if (index !== -1) {
            imgList.value.splice(index, 1);
        }
    } else if (file && file.url) {
        // 如果file对象中有url属性
        const index = imgList.value.indexOf(file.url);
        if (index !== -1) {
            imgList.value.splice(index, 1);
        }
    }
};

const fileHandlePreview = (file) => {
    console.log(file);
    // 确保正确获取预览图片URL
    if (file && file.response && file.response.data) {
        dialogImageUrl.value = file.response.data;
    } else if (file && file.url) {
        dialogImageUrl.value = file.url;
    }
    imgDialogVisible.value = true;
};

const fileHandleSuccess = (response, file, fileList) => {
    console.log("file:", response, file, fileList);
    // 检查响应格式
    if (response && response.status_code === 1 && response.data) {
        imgList.value.push(getImageUrl(response.data));
    } else {
        ElMessage.error('图片上传失败：' + (response ? response.msg : '未知错误'));
    }
};

const fileHandleError = (err, file, fileList) => {
    console.error("上传失败:", err);
    ElMessage.error('图片上传失败，请重试！');
};

const releaseButton = () => {
    idleItemInfo.value.pictureList = JSON.stringify(imgList.value);
    console.log(idleItemInfo.value);
    if(idleItemInfo.value.idleLabel === 5){
        idleItemInfo.value.idleStock = 1;
    }
    if(idleItemInfo.value.idleStock <= 0){
        ElMessage.error('库存必须大于0');
        return;
    }

    if(idleItemInfo.value.idleName &&
        idleItemInfo.value.idleDetails &&
        idleItemInfo.value.idlePlace &&
        idleItemInfo.value.idleLabel &&
      (idleItemInfo.value.idlePrice || idleItemInfo.value.idleLabel === 5)){
        
        // 校验图片列表是否为空
        if (imgList.value.length === 0) {
            ElMessage.warning('请至少上传一张图片！');
            return;
        }

        // 判断是编辑还是新建
        if (idleItemInfo.value.id) {
            // 编辑模式：更新商品并重新提交审核
            idleItemInfo.value.idleStatus = 3; // 设置为待审核状态
            $api.updateIdleItem(idleItemInfo.value).then(res=>{
                if (res.status_code === 1) {
                    ElMessage.success('修改成功！已重新提交审核');
                    router.replace({path: '/me'});
                } else {
                    ElMessage.error('修改失败！'+res.msg);
                }
            }).catch(e=>{
                console.error("修改失败:", e);
                ElMessage.error('修改失败，请检查网络连接或联系管理员');
            });
        } else {
            // 新建模式：发布新商品
            $api.addIdleItem(idleItemInfo.value).then(res=>{
                if (res.status_code === 1) {
                    ElMessage.success('发布成功！请等待管理员审核后即可上架');
                    console.log(res.data);
                    router.replace({path: '/details', query: {id: res.data.id}});
                } else {
                    ElMessage.error('发布失败！'+res.msg);
                }
            }).catch(e=>{
                console.error("发布失败:", e);
                ElMessage.error('发布失败，请检查网络连接或联系管理员');
            });
        }
    }else {
        ElMessage.error('请填写完整信息！');
    }
};

const handleExceed = (files, fileList) => {
    ElMessage.warning(`限制10张图片，本次选择了 ${files.length} 张图，共选择了 ${files.length + fileList.length} 张图`);
};

const deleteImage = (index) => {
    imgList.value.splice(index, 1);
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

// 获取用户地址列表
const getAddressList = () => {
    $api.getAddress().then(res => {
        if (res.status_code === 1 && res.data && Array.isArray(res.data)) {
            // 处理地址数据，生成完整地址文本
            const data = res.data.map(addr => {
                addr.detailAddressText = (addr.provinceName || '') + (addr.cityName || '') + (addr.regionName || '') + (addr.detailAddress || '');
                return addr;
            });
            addressList.value = data;
        }
    }).catch(e => {
        console.error('获取地址失败:', e);
    });
};

// 跳转到个人中心新增地址
const goToAddAddress = () => {
    // 保存当前表单数据到 sessionStorage，以便从个人中心返回时能恢复
    sessionStorage.setItem('releaseFormData', JSON.stringify({
        idleName: idleItemInfo.value.idleName,
        idleDetails: idleItemInfo.value.idleDetails,
        idlePrice: idleItemInfo.value.idlePrice,
        idleStock: idleItemInfo.value.idleStock,
        idlePlace: idleItemInfo.value.idlePlace,
        idleLabel: idleItemInfo.value.idleLabel,
        pictureList: imgList.value,
        editId: router.currentRoute.value.query.id
    }));
    // 跳转到个人中心并自动打开新增地址页面
    router.push({ path: '/me', query: { addAddress: 'true' } });
};

onMounted(() => {
    getAddressList();
    
    // 检查是否有保存的表单数据（从个人中心返回时恢复）
    const savedFormData = sessionStorage.getItem('releaseFormData');
    if (savedFormData) {
        try {
            const formData = JSON.parse(savedFormData);
            idleItemInfo.value.idleName = formData.idleName || '';
            idleItemInfo.value.idleDetails = formData.idleDetails || '';
            idleItemInfo.value.idlePrice = formData.idlePrice || 0;
            idleItemInfo.value.idleStock = formData.idleStock || 1;
            idleItemInfo.value.idlePlace = formData.idlePlace || '';
            idleItemInfo.value.idleLabel = formData.idleLabel || '';
            if (formData.pictureList && Array.isArray(formData.pictureList)) {
                imgList.value = formData.pictureList.map(url => getImageUrl(url));
            }
            // 清除已使用的数据
            sessionStorage.removeItem('releaseFormData');
            // 重新获取地址列表（可能已经新增了地址）
            getAddressList();
        } catch (e) {
            console.error('恢复表单数据失败:', e);
        }
    }
    
    // 如果 URL 中有 id 参数，说明是编辑模式
    const editId = router.currentRoute.value.query.id;
    if (editId) {
        // 加载商品数据
        $api.getIdleItem({ id: editId }).then(res => {
            if (res.status_code === 1 && res.data) {
                const data = res.data;
                idleItemInfo.value = {
                    id: data.id,
                    idleName: data.idleName,
                    idleDetails: data.idleDetails,
                    idlePrice: data.idlePrice,
                    idleStock: data.idleStock || 1,
                    idlePlace: data.idlePlace,
                    idleLabel: data.idleLabel,
                    pictureList: data.pictureList
                };
                // 解析图片列表
                if (data.pictureList) {
                    const rawImgList = JSON.parse(data.pictureList);
                    imgList.value = rawImgList.map(url => getImageUrl(url));
                }
            }
        }).catch(e => {
            console.error('加载商品数据失败:', e);
            ElMessage.error('加载商品信息失败');
        });
    }
});
</script>

<style scoped>
    .release-idle-container {
        min-height: 85vh;
    }

    .release-idle-container-title {
        font-size: 18px;
        padding: 30px 0;
        font-weight: 600;
        width: 100%;
        text-align: center;
    }

    .release-idle-container-form {
        padding: 0 180px;
    }

    .release-idle-detiles-text {
        margin: 20px 0;
    }
    
    /* 优化后的表单布局 */
    .form-row {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        margin-bottom: 20px;
    }

    .form-item {
        flex: 1;
        min-width: 250px; /* 确保最小宽度，防止过窄 */
        display: flex;
        flex-direction: column;
    }

    .release-tip{
        color: #555555;
        font-size: 14px;
        margin-bottom: 8px; /* 标签和输入框之间的间距 */
        font-weight: 500;
        /* 移除原来的浮动样式 */
        float: none;
        height: auto;
        line-height: normal;
        padding-right: 0;
    }

    /* 覆盖原来的 .release-idle-place 样式影响 */
    .release-idle-place{
        margin-bottom: 0;
    }

    .location-empty {
        width: 100%;
    }

    .location-empty-content {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 6px;
        border: 1px dashed #d9d9d9;
        margin-bottom: 12px;
    }

    .location-empty-icon {
        font-size: 24px;
        color: #bfbfbf;
        flex-shrink: 0;
        margin-top: 2px;
    }

    .location-empty-text {
        flex: 1;
    }

    .location-empty-title {
        font-size: 14px;
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
    }

    .location-empty-desc {
        font-size: 12px;
        color: #666;
        line-height: 1.5;
    }

    .location-empty-actions {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .address-select-row {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .address-quick-btn {
        flex-shrink: 0;
        white-space: nowrap;
    }

    .release-idle-container-picture{
        margin: 20px 0;
    }
    .picture-header {
        margin-bottom: 10px;
    }
    .release-idle-container-picture-title{
        margin: 0;
        margin-right: 15px;
        color: #555555;
        font-size: 14px;
    }
    .picture-list {
        margin: 20px 0;
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
    }
    
    .picture-item {
        position: relative;
        width: 148px;
        height: 148px;
        border: 1px solid #c0ccda;
        border-radius: 6px;
        overflow: hidden;
        display: inline-block;
        box-sizing: border-box;
    }

    /* 强制遮罩层显示逻辑 */
    .picture-mask {
        position: absolute;
        width: 100%;
        height: 100%;
        left: 0;
        top: 0;
        cursor: pointer;
        text-align: center;
        color: #fff;
        opacity: 0;
        font-size: 20px;
        background-color: rgba(0,0,0,.5);
        transition: opacity .3s;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 15px; /* 图标间距 */
        z-index: 100; /* 提高层级 */
        pointer-events: none; /* 让点击事件穿透到下方的图片 */
    }

    /* 只要鼠标悬浮在 item 上，就显示遮罩 */
    .picture-item:hover .picture-mask {
        opacity: 1;
    }

    /* 按钮样式 */
    .icon-btn {
        display: block;
        font-size: 24px;
        color: #ffffff;
        pointer-events: auto; /* 恢复按钮的点击事件 */
        transition: color 0.2s;
    }
    
    .icon-btn:hover {
        color: #409EFF; /* 放大图标悬浮颜色 */
    }

    .delete-btn:hover {
        color: #f56c6c; /* 删除图标悬浮颜色 */
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
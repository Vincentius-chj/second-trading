export const IDLE_CATEGORY_OPTIONS = [
    { value: 1, label: '数码科技' },
    { value: 2, label: '生活用品' },
    { value: 3, label: '运动相关' },
    { value: 4, label: '图书笔记' },
    { value: 5, label: '公告展示' }
];

export const IDLE_CATEGORY_TABS = [
    { name: '0', label: '全部' },
    ...IDLE_CATEGORY_OPTIONS.map(item => ({
        name: String(item.value),
        label: item.label
    }))
];

export const IDLE_CATEGORY_NAME_MAP = {
    1: '数码科技',
    2: '生活用品',
    3: '运动相关',
    4: '图书笔记',
    5: '公告展示'
};

export const getIdleCategoryName = (value) => {
    const key = Number(value);
    return IDLE_CATEGORY_NAME_MAP[key] || '未知';
};

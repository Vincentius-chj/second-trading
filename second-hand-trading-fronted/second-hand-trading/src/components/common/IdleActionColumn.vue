<template>
	<el-table-column label="操作" width="144" align="center" fixed="right">
		<template #default="scope">
			<div class="action-cell">
				<template v-if="mode === 3">
					<div class="action-buttons">
						<el-button
							class="action-btn success"
							size="default"
							type="text"
							@click="$emit('approve', scope.$index)">
							通过审核
						</el-button>
						<el-button
							class="action-btn danger"
							size="default"
							type="text"
							@click="$emit('reject', scope.$index)">
							驳回
						</el-button>
					</div>
				</template>
				<template v-else>
					<span class="empty-action-text">暂无</span>
				</template>
			</div>
		</template>
	</el-table-column>
</template>

<script setup>
defineProps({
	mode: {
		type: Number,
		required: true
	}
});

defineEmits(['approve', 'reject']);
</script>

<style scoped>
.action-cell {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 44px;
	line-height: 1;
	gap: 8px;
}

.action-buttons {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10px;
	flex-wrap: wrap;
}

.action-btn {
	padding: 0;
	font-size: 13px;
	font-weight: 600;
	line-height: 1;
	min-height: 30px;
	transition: color 0.2s ease, transform 0.2s ease, opacity 0.2s ease;
}

.action-btn.success {
	color: #67c23a;
}

.action-btn.danger {
	color: #f56c6c;
}

.action-btn.success:hover,
.action-btn.success:focus {
	color: #85ce61;
	opacity: 0.88;
	transform: translateY(-1px);
}

.action-btn.danger:hover,
.action-btn.danger:focus {
	color: #f78989;
	opacity: 0.88;
	transform: translateY(-1px);
}

.empty-action-text {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-height: 30px;
	font-size: 13px;
	color: #a3a9b4;
}
</style>
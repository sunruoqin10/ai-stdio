<template>
  <div class="tree-node" :class="`level-${node.level || 1}`">
    <!-- 节点内容 -->
    <div class="node-content" @click="handleClick">
      <div class="node-name">{{ node.label || node.name }}</div>
      <div class="node-info">{{ node.employeeCount || 0 }}人</div>
    </div>

    <!-- 子节点 -->
    <div v-if="node.children && node.children.length > 0" class="children-container">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        @node-click="$emit('node-click', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  node: any
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'node-click': [node: any]
}>()

function handleClick(e: Event) {
  e.stopPropagation()
  emit('node-click', props.node)
}
</script>

<style scoped lang="scss">
.tree-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  padding: 0 10px;

  .node-content {
    padding: 12px 20px;
    background: #409EFF;
    color: #fff;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    min-width: 120px;
    text-align: center;
    z-index: 2;
    position: relative;

    &:hover {
      background: #67C23A;
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }

    .node-name {
      font-size: 14px;
      font-weight: bold;
      margin-bottom: 4px;
      white-space: nowrap;
    }

    .node-info {
      font-size: 12px;
      opacity: 0.9;
    }
  }

  // 不同层级的节点样式
  &.level-1 .node-content {
    background: #409EFF;
    font-size: 16px;
    padding: 16px 24px;
    min-width: 150px;
  }

  &.level-2 .node-content {
    background: #67C23A;
  }

  &.level-3 .node-content {
    background: #E6A23C;
  }

  &.level-4 .node-content {
    background: #F56C6C;
  }

  // 子节点容器
  .children-container {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: flex-start;
    position: relative;
    margin-top: 40px;
    padding-top: 20px;

    // 连接线 - 水平线
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%);
      width: calc(100% - 20px);
      height: 2px;
      background: #ccc;
      z-index: 1;
    }

    // 连接线 - 垂直线(从父节点到水平线)
    &::after {
      content: '';
      position: absolute;
      top: -20px;
      left: 50%;
      transform: translateX(-50%);
      width: 2px;
      height: 20px;
      background: #ccc;
      z-index: 1;
    }
  }

  // 为每个子节点添加垂直连接线
  .children-container .tree-node {
    &::before {
      content: '';
      position: absolute;
      top: -20px;
      left: 50%;
      transform: translateX(-50%);
      width: 2px;
      height: 20px;
      background: #ccc;
      z-index: 1;
    }

    &:first-child::before,
    &:last-child::before {
      // 第一个和最后一个子节点,垂直线从水平线开始
      top: 0;
    }
  }
}
</style>

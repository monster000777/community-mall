<template>
  <div class="ai-customer-service">
    <!-- 悬浮按钮 -->
    <div v-if="!isOpen" class="float-btn" @click="isOpen = true">
      <customer-service-outlined style="font-size: 24px; color: #fff"/>
      <span class="btn-text">团团导购</span>
    </div>

    <!-- 聊天窗口 -->
    <div v-else class="chat-window">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-left">
          <robot-outlined :class="{ 'talking-icon': isTalking }"/>
          <span style="margin-left: 8px">智能导购团团</span>
          <span v-if="isTalking" class="status-tag">播报中...</span>
        </div>
        <div class="header-right">
          <!-- 全局停止按钮：仅在播报时显示 -->
          <a-tooltip v-if="isTalking" title="停止当前播报">
            <div class="action-icon stop-global-btn" @click="stopTTS">
              <stop-outlined/>
            </div>
          </a-tooltip>

          <!-- 自动播放控制开关 -->
          <a-tooltip :title="isAutoPlay ? '点击关闭自动播报' : '点击开启自动播报'">
            <div class="action-icon" @click="isAutoPlay = !isAutoPlay">
              <sound-outlined v-if="isAutoPlay"/>
              <audio-muted-outlined v-else/>
            </div>
          </a-tooltip>
          <close-outlined class="close-icon" @click="isOpen = false"/>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-body" ref="chatBodyRef">
        <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
          <!-- 头像区域 -->
          <div class="avatar"
            :class="{ 'is-talking': isTalking && msg.type === 'ai' && index === messages.length - 1 }">
            <!-- 用户头像：同步 Pinia Store 中的头像 -->
            <template v-if="msg.type === 'user'">
              <img v-if="userAvatar" :src="userAvatar" class="avatar-img" alt="User"/>
              <user-outlined v-else/>
            </template>

            <!-- AI 头像：保持默认机器人图标不变 -->
            <template v-else>
              <robot-outlined/>
            </template>

            <!-- 语音播放时的波纹动画 -->
            <div v-if="isTalking && msg.type === 'ai' && index === messages.length - 1" class="wave-container">
              <span class="wave"></span>
              <span class="wave"></span>
            </div>
          </div>
          <div class="content-wrapper">
            <div class="content">
              {{ msg.content }}
            </div>
            <!-- 消息操作按钮 (仅限 AI 消息) -->
            <div v-if="msg.type === 'ai'" class="message-actions">
              <!-- 正在播报时显示停止按钮，否则显示播放按钮 -->
              <template v-if="isTalking && index === messages.length - 1">
                <a-tooltip title="停止播放">
                  <stop-outlined class="play-icon stop-icon-active" @click="stopTTS"/>
                </a-tooltip>
              </template>
              <template v-else>
                <a-tooltip title="播放语音">
                  <play-circle-outlined class="play-icon" @click="playTTS(msg.content)"/>
                </a-tooltip>
              </template>
            </div>
          </div>
        </div>
        <div v-if="loading" class="message-item ai">
          <div class="avatar">
            <robot-outlined/>
          </div>
          <div class="content loading">
            <loading-outlined/>
            正在查询库存...
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-footer">
        <a-input v-model:value="inputVal" placeholder="问问团团有没有红富士？" @pressEnter="sendMessage" :disabled="loading">
          <template #suffix>
            <send-outlined class="send-icon" @click="sendMessage"/>
          </template>
        </a-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, onUnmounted, onMounted, computed } from 'vue';
import {
  CustomerServiceOutlined,
  CloseOutlined,
  SendOutlined,
  UserOutlined,
  RobotOutlined,
  LoadingOutlined,
  SoundOutlined,
  AudioMutedOutlined,
  PlayCircleOutlined,
  StopOutlined
} from '@ant-design/icons-vue';
import { askAi } from '@/api/ai';
import { ttsPlayer, playTTS, stopTTS } from '@/utils/ttsPlayer'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue';

// --- 头像同步逻辑 ---
const userStore = useUserStore();
// 从 Pinia 的 userInfo 中实时获取用户头像
const userAvatar = computed(() => userStore.userInfo?.avatar);

const isOpen = ref(false);
const inputVal = ref('');
const loading = ref(false);
const chatBodyRef = ref(null);
const isTalking = ref(false); // 语音播放状态
// 自动播放控制：优先读取本地缓存
const savedAutoPlay = localStorage.getItem('ai_chat_autoplay');
// 默认为 true (如果本地没有存过)
const isAutoPlay = ref(savedAutoPlay === null ? true : savedAutoPlay === 'true');

// 监听变化并持久化
watch(isAutoPlay, (newVal) => {
  localStorage.setItem('ai_chat_autoplay', newVal);
});

const messages = ref([
  { type: 'ai', content: '您好！我是团团，很高兴为您服务。请问您想买点什么？' }
]);

// 初始化监听器
onMounted(() => {
  ttsPlayer.listen((status) => {
    isTalking.value = status;
  });
});

// 监听窗口关闭，自动停止语音播放
watch(isOpen, (newVal) => {
  if (!newVal) {
    stopTTS();
  }
});

// 组件卸载时停止播放
onUnmounted(() => {
  stopTTS();
});

const scrollToBottom = async () => {
  await nextTick();
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
  }
};

const sendMessage = async () => {
  const content = inputVal.value.trim();
  if (!content || loading.value) return;

  // 1. 添加用户消息
  messages.value.push({ type: 'user', content });
  inputVal.value = '';
  await scrollToBottom();

  // 2. 调用后端
  loading.value = true;
  stopTTS();

  try {
    const res = await askAi({ question: content });
    const responseData = (res && res.data && res.data.code) ? res.data : res;

    if (responseData && responseData.code === 200) {
      const answer = responseData.data;
      if (answer) {
        messages.value.push({ type: 'ai', content: answer });
        // 根据开关决定是否自动播放
        if (isAutoPlay.value) {
          playTTS(answer);
        }
      } else {
        messages.value.push({ type: 'ai', content: '（AI回复为空）' });
      }
    } else {
      const subMsg = responseData?.message || responseData?.msg || '未知错误';
      message.error(subMsg);
      messages.value.push({ type: 'ai', content: '抱歉，团团开小差了。' });
    }
  } catch (error) {
    message.error('网络请求失败');
    messages.value.push({ type: 'ai', content: '网络连接失败，请检查网络。' });
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};
</script>

<style scoped>
.ai-customer-service {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

/* 用户头像图片样式 */
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.float-btn {
  width: 60px;
  height: 60px;
  background-color: var(--primary-color);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  color: white;
  overflow: hidden;
}

.float-btn:hover {
  transform: scale(1.1);
  background-color: var(--primary-hover);
}

.btn-text {
  font-size: 10px;
  margin-top: 2px;
}

.chat-window {
  width: 350px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-header {
  height: 50px;
  background: var(--primary-color);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-icon {
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.action-icon:hover {
  opacity: 0.8;
}

.stop-global-btn {
  color: #ffccc7;
  animation: blink 2s infinite;
}

@keyframes blink {
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.6;
  }

  100% {
    opacity: 1;
  }
}

.status-tag {
  font-size: 12px;
  margin-left: 8px;
  font-weight: normal;
  opacity: 0.8;
}

.talking-icon {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.2);
    color: #fff;
  }

  100% {
    transform: scale(1);
  }
}

.chat-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  gap: 8px;
  max-width: 85%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  position: relative;
  width: 32px;
  height: 32px;
  background: #ddd;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

.message-item.ai .avatar {
  background: #e6f7ff;
  color: var(--primary-color);
}

.avatar.is-talking {
  border: 2px solid var(--primary-color);
}

/* 语音波纹 */
.wave-container {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  pointer-events: none;
}

.wave {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 1px solid var(--primary-color);
  border-radius: 50%;
  animation: wave-spread 2s infinite;
  opacity: 0;
}

.wave:nth-child(2) {
  animation-delay: 1s;
}

@keyframes wave-spread {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }

  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.content {
  background: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-item.user .content {
  background: var(--primary-color);
  color: #fff;
}

.message-actions {
  display: flex;
  justify-content: flex-start;
  padding-left: 2px;
}

.play-icon {
  font-size: 16px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.play-icon:hover {
  color: var(--primary-color);
}

.stop-icon-active {
  color: #ff4d4f;
}

.chat-footer {
  padding: 12px;
  border-top: 1px solid #eee;
  background: #fff;
}

.send-icon {
  cursor: pointer;
  color: var(--primary-color);
}

.send-icon:hover {
  color: var(--primary-hover);
}

/* 覆盖 Ant Design Input 样式 */
:deep(.ant-input-affix-wrapper:hover) {
  border-color: var(--primary-color);
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}
</style>

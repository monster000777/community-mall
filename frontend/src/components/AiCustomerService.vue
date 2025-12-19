<template>
    <div class="ai-customer-service">
        <!-- 悬浮按钮 -->
        <div v-if="!isOpen" class="float-btn" @click="isOpen = true">
            <customer-service-outlined style="font-size: 24px; color: #fff" />
            <span class="btn-text">团团导购</span>
        </div>

        <!-- 聊天窗口 -->
        <div v-else class="chat-window">
            <!-- 头部 -->
            <div class="chat-header">
                <div class="header-title">
                    <robot-outlined />
                    <span style="margin-left: 8px">智能导购团团</span>
                </div>
                <close-outlined class="close-icon" @click="isOpen = false" />
            </div>

            <!-- 消息列表 -->
            <div class="chat-body" ref="chatBodyRef">
                <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.type]">
                    <div class="avatar">
                        <user-outlined v-if="msg.type === 'user'" />
                        <robot-outlined v-else />
                    </div>
                    <div class="content">
                        {{ msg.content }}
                    </div>
                </div>
                <div v-if="loading" class="message-item ai">
                    <div class="avatar"><robot-outlined /></div>
                    <div class="content loading">
                        <loading-outlined /> 正在查询库存...
                    </div>
                </div>
            </div>

            <!-- 输入框 -->
            <div class="chat-footer">
                <a-input v-model:value="inputVal" placeholder="问问团团有没有红富士？" @pressEnter="sendMessage"
                    :disabled="loading">
                    <template #suffix>
                        <send-outlined class="send-icon" @click="sendMessage" />
                    </template>
                </a-input>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import {
    CustomerServiceOutlined,
    CloseOutlined,
    SendOutlined,
    UserOutlined,
    RobotOutlined,
    LoadingOutlined
} from '@ant-design/icons-vue';
import { askAi } from '@/api/ai';
import { message } from 'ant-design-vue';

const isOpen = ref(false); // 默认关闭
const inputVal = ref('');
const loading = ref(false);
const chatBodyRef = ref(null);

const messages = ref([
    { type: 'ai', content: '您好！我是团团，很高兴为您服务。请问您想买点什么？' }
]);

const scrollToBottom = async () => {
    await nextTick();
    if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
};

const sendMessage = async () => {
    const content = inputVal.value.trim();
    if (!content) return;

    // 1. 添加用户消息
    messages.value.push({ type: 'user', content });
    inputVal.value = '';
    await scrollToBottom();

    // 2. 调用后端
    loading.value = true;
    try {
        const res = await askAi({ question: content });
        console.log('AI Response:', res);

        const responseData = (res && res.data && res.data.code) ? res.data : res;

        if (responseData && responseData.code === 200) {
            const answer = responseData.data;
            if (answer) {
                messages.value.push({ type: 'ai', content: answer });
            } else {
                messages.value.push({ type: 'ai', content: '（AI回复为空）' });
            }
        } else {
            console.warn('Invalid Response:', responseData);
            const subMsg = responseData?.message || responseData?.msg || '未知错误';
            message.error(subMsg);
            messages.value.push({ type: 'ai', content: '抱歉，团团开小差了。(' + subMsg + ')' });
        }
    } catch (error) {
        console.error('Chat Error:', error);
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

/* 聊天窗口 */
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

.close-icon {
    cursor: pointer;
    opacity: 0.8;
}

.close-icon:hover {
    opacity: 1;
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

.message-item.ai {
    align-self: flex-start;
}

.avatar {
    width: 32px;
    height: 32px;
    background: #ddd;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-shrink: 0;
    color: #666;
}

.message-item.ai .avatar {
    background: #e6f7ff;
    color: var(--primary-color);
}

.message-item.user .avatar {
    background: var(--primary-color);
    color: #fff;
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

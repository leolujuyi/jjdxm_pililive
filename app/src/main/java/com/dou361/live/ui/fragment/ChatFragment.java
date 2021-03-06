package com.dou361.live.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.live.R;
import com.dou361.live.bean.AnchorBean;
import com.dou361.live.ui.config.StatusConfig;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.widget.EaseChatMessageList;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * ========================================
 * <p>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p>
 * 作 者：陈冠明
 * <p>
 * 个人网站：http://www.dou361.com
 * <p>
 * 版 本：1.0
 * <p>
 * 创建日期：2016/10/7 12:50
 * <p>
 * 描 述：聊天会话对象
 * <p>
 * <p>
 * 修订历史：
 * <p>
 * ========================================
 */
public class ChatFragment extends BaseFragment implements EMMessageListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.username)
    TextView usernameView;
    @BindView(R.id.message_list)
    EaseChatMessageList messageListView;
    @BindView(R.id.edit_text)
    EditText editText;
    private AnchorBean anchorBean;
    /**
     * 是否是正常样式true为整个页面，false为对话框样式
     */
    private boolean isNormalStyle;

    public ChatFragment() {
    }

    public static ChatFragment newInstance(AnchorBean anchorBean, boolean isNormalStyle) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(StatusConfig.ARG_ANCHOR, anchorBean);
        args.putBoolean(StatusConfig.ARG_IS_NORMAL, isNormalStyle);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            anchorBean = (AnchorBean) getArguments().getSerializable(StatusConfig.ARG_ANCHOR);
            isNormalStyle = getArguments().getBoolean(StatusConfig.ARG_IS_NORMAL, false);
            usernameView.setText(anchorBean.getName());
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNormalStyle)
                    getActivity().getSupportFragmentManager().popBackStack();
                else
                    getActivity().finish();
            }
        });

        if (isNormalStyle) {
            getView().findViewById(R.id.close).setVisibility(View.INVISIBLE);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
            usernameView.setTextColor(getResources().getColor(R.color.common_white));
        }
        messageListView.init(anchorBean.getAnchorId(), EaseConstant.CHATTYPE_SINGLE, null);

        // 获取当前conversation对象
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(anchorBean.getAnchorId());
        if (conversation != null) {
            conversation.markAllMessagesAsRead();
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        messageListView.refresh();
        EaseUI.getInstance().pushActivity(getActivity());
        // register the event listener when enter the foreground
        EMClient.getInstance().chatManager().addMessageListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the
        // background
        EMClient.getInstance().chatManager().removeMessageListener(this);

        // remove activity from foreground activity list
        EaseUI.getInstance().popActivity(getActivity());
    }


    @OnClick(R.id.btn_send)
    public void sendMessage() {
        if (TextUtils.isEmpty(editText.getText())) {
            Toast.makeText(getActivity(), "消息内容不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        EMMessage message = EMMessage.createTxtSendMessage(editText.getText().toString(), anchorBean.getName());
        sendMessage(message);
    }

    private void sendMessage(EMMessage message) {
        editText.setText("");
        //send message
        EMClient.getInstance().chatManager().sendMessage(message);
        //refresh ui
        messageListView.refreshSelectLast();
    }

    @OnClick(R.id.close)
    void close() {
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).commit();
    }

    private ChatFragmentListener listener;

    public void setChatFragmentListener(ChatFragmentListener listener) {
        this.listener = listener;
    }

    interface ChatFragmentListener {
        void onDestory();
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        messageListView.refreshSelectLast();
        EaseUI.getInstance().getNotifier().vibrateAndPlayTone(messages.get(messages.size() - 1));
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {

    }

    @Override
    public void onMessageReadAckReceived(List<EMMessage> messages) {
        messageListView.refresh();
    }

    @Override
    public void onMessageDeliveryAckReceived(List<EMMessage> messages) {
        messageListView.refresh();
    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object change) {
        messageListView.refresh();
    }
}

package com.hegp.controller;

import com.alibaba.fastjson.JSON;
import com.hegp.bean.*;
import com.hegp.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RequestMapping("/")
    public String index(HttpServletRequest request) {
        String basePath = (String) request.getAttribute("basePath");
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.hasText(username)) {
            return "redirect:"+basePath+"/chat";
        } else {
            return "redirect:"+basePath+"/login";
        }
    }

    @RequestMapping("/chat")
    public String chat(Model model) {
        HttpServletRequest request = ServletUtils.getCurrentRequest();
        String userName = (String) request.getSession().getAttribute("username");

        InfoBean infoMap = new InfoBean();

        UserBean jackInfo = new UserBean();
        jackInfo.setUsername("jack");
        jackInfo.setId(1);
        jackInfo.setSign("我是jack");
        jackInfo.setAvatar("static/images/jack.jpg");
        jackInfo.setStatus("online");

        UserBean tomInfo = new UserBean();
        tomInfo.setUsername("tom");
        tomInfo.setId(2);
        tomInfo.setSign("我是tom");
        tomInfo.setAvatar("static/images/tom.jpg");
        tomInfo.setStatus("online");

        List<UserBean> friendList = new ArrayList<>();
        List<FriendBean> friendBeanList = new ArrayList<>();
        List<GroupBean> groupList = new ArrayList<>();

        FriendBean friend = new FriendBean();
        GroupBean group = new GroupBean();

        group.setGroupname("websocket交流");
        group.setId(1000);
        group.setAvatar("static/images/group.jpg");
        groupList.add(group);

        infoMap.setGroup(groupList);

        friend.setGroupname("我的好友");
        friend.setId(100);

        if ("jack".equals(userName)) {

            friendList.add(tomInfo);
            infoMap.setMine(jackInfo);
        } else {

            friendList.add(jackInfo);
            infoMap.setMine(tomInfo);
        }

        friend.setList(friendList);
        friendBeanList.add(friend);
        infoMap.setFriend(friendBeanList);

        String jsonString = JSON.toJSONString(infoMap);

        model.addAttribute("mine", jsonString);

        return "chat";
    }

    @MessageMapping("/chat.group")
    // 拼接 WebSocketMessageBrokerConfigurer.configureMessageBroker方法的registry.setApplicationDestinationPrefixes("/app")前缀组成完整的路径 /app/chat.group
//    @SendTo("/topic/group1000") // @SendTo注解写的是全路径, 不存在拼接路径
    public void sendGroupMessage(@Payload SendMessageBean msg) {
        ChatMessageBean groupMsg = new ChatMessageBean();
        groupMsg.setUsername(msg.getData().getMine().getUsername());
        groupMsg.setAvatar(msg.getData().getMine().getAvatar());
        groupMsg.setId(1000);
        groupMsg.setType("group");
        groupMsg.setContent(msg.getData().getMine().getContent());
        groupMsg.setCid(0);
        groupMsg.setMine(false);
        groupMsg.setFromid(msg.getData().getMine().getId());
        groupMsg.setTimestamp(System.currentTimeMillis());
        // messagingTemplate.convertAndSend写的是全路径, 不存在拼接路径
        messagingTemplate.convertAndSend("/topic/group1000", groupMsg);
    }

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload SendMessageBean msg) {
        ChatMessageBean chatMsg = new ChatMessageBean();
        chatMsg.setUsername(msg.getData().getMine().getUsername());
        chatMsg.setAvatar(msg.getData().getMine().getAvatar());
        chatMsg.setId(msg.getData().getMine().getId());
        chatMsg.setType("friend");
        chatMsg.setContent(msg.getData().getMine().getContent());
        chatMsg.setCid(0);
        chatMsg.setMine(false);
        chatMsg.setFromid(msg.getData().getMine().getId());
        chatMsg.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSendToUser(msg.getData().getTo().getName(), "/topic/chat", chatMsg);
    }
}
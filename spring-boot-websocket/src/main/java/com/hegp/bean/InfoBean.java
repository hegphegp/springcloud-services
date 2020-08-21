package com.hegp.bean;

import java.util.List;

public class InfoBean {

    private UserBean mine;

    private List friend;

    private List group;

    public UserBean getMine() {
        return mine;
    }

    public void setMine(UserBean mine) {
        this.mine = mine;
    }

    public List getFriend() {
        return friend;
    }

    public void setFriend(List friend) {
        this.friend = friend;
    }

    public List getGroup() {
        return group;
    }

    public void setGroup(List group) {
        this.group = group;
    }
}

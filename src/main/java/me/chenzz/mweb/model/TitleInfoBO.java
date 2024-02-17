package me.chenzz.mweb.model;

import lombok.Data;

@Data
public class TitleInfoBO {

    private Integer titleNo;
    private Integer titleLevel;

    public TitleInfoBO(Integer titleNo, Integer titleLevel) {
        this.titleNo = titleNo;
        this.titleLevel = titleLevel;
    }
}

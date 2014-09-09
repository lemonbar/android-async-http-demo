package com.lemon.android.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by lemon on 14-9-9.
 */
@Data
public class Dialog implements Serializable {
    private int id;
    private User user;
    private String text;
    private String dateStr;
    private int type;
    private int height;
    private int width;
    private String picURL;
}

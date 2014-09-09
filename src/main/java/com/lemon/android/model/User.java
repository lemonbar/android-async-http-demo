package com.lemon.android.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by lemon on 14-9-9.
 */
@Data
public class User implements Serializable {
    private int id;
    private String username;
    private String avatar;
    private String position;
    private int type;
}
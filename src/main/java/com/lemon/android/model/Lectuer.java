package com.lemon.android.model;

import lombok.Data;

/**
 * Created by lemon on 14-9-9.
 */
@Data
public class Lectuer extends User {
    private float rating;
    private int courseCount;
}

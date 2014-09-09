package com.lemon.android.model;

import android.graphics.Bitmap;
import lombok.Data;

/**
 * Created by lemon on 14-9-9.
 */
@Data
public class PhotoWrapper {
    private int width;
    private int height;
    private Bitmap bitmap;
}

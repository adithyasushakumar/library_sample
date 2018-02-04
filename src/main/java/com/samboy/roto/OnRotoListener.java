package com.samboy.roto;

/**
 * Created by ANDROID on 6/2/2017.
 */

public interface OnRotoListener {
    boolean onRotoSucceed(String response, int requestId);
    boolean onRotoFailed(String error, int requestId);
}


package com.samboy.roto;


/**
 * Created by ANDROID on 6/2/2017.
 */

public class WebModel {
    final String url;
    final String[] keys;
    final String[] values;
    OnRotoListener onRotoListener;
    final boolean cancelAll;
    public final int requestId;

    public WebModel(String url, String[] keys, String[] values, boolean cancelAll, int requestId) {
        this.url = url;
        this.keys = keys;
        this.values = values;
        this.cancelAll = cancelAll;
        this.requestId = requestId;
    }
    public void setOnRotoListener(OnRotoListener onRotoListener) {
        this.onRotoListener = onRotoListener;
    }
}

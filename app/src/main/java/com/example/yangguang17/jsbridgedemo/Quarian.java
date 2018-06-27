package com.example.yangguang17.jsbridgedemo;

import android.webkit.WebView;

public abstract class Quarian {

    protected String getName() {
        return this.getClass().getSimpleName();
    }



    public void finalCall(WebView webView, String params, String callBackName) {
        Normandy.getNormandy().callBack(webView, params, callBackName);
    }

}

package com.example.yangguang17.jsbridgedemo;

import android.content.Context;
import android.webkit.WebView;
import android.widget.Toast;

public class Tali extends Quarian {
    private Context context;

    public Tali (Context context) {
        this.context = context;
    }
    public void onToast (String message, String callBackName, WebView webView) {
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
        String str = "keelah se'lai";
        finalCall(webView, str, callBackName);
    }


}

package com.example.yangguang17.jsbridgedemo;

import android.net.Uri;
import android.webkit.WebView;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Normandy {
    private Map<String, Object> normandy;
    public static final String SECHEMA = "GETH";
    private Normandy() {
        init();
    }

    public void on(WebView view, String message) {
        Uri uri = Uri.parse(message);
        String callBackNo = uri.getPort() + "";
        String params = uri.getQuery();
        String methodName = uri.getPath().replace("/", "");
        String className = uri.getHost();

        try {
            Object obj = normandy.get(className);
            Method[] methods = obj.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    method.invoke(obj, params, callBackNo, view);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class NormandyInstance {
        private static Normandy INSTANCE = new Normandy();
    }

    public static Normandy getNormandy() {
        return NormandyInstance.INSTANCE;
    }

    private void init() {
        normandy = new HashMap<>();
    }

    public void register(Quarian quarian) {
        this.normandy.put(quarian.getName(), quarian);
    }

    public  void unRegister(Quarian quarian) {
        this.normandy.remove(quarian);
    }

    public void callBack(WebView webView, String params, String callBackNo) {
        StringBuffer sb = new StringBuffer();
        sb.append("javascript:geth.onExecute(");
        sb.append(callBackNo + ",");
        sb.append("`" + params + "`" + ")");
        String callJs = sb.toString();
        webView.loadUrl(callJs);
    }

    public void launch(WebView webView, String functionName, String params) {
        StringBuffer sb = new StringBuffer();
        sb.append("javascript:"+ functionName +"(");
        sb.append("`" + params + "`" + ")");
        String callJs = sb.toString();
        webView.loadUrl(callJs);
    }
}

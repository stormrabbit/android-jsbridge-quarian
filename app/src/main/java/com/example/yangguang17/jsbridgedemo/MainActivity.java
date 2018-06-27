package com.example.yangguang17.jsbridgedemo;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn;
    WebView webView;
    Tali tali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tali = new Tali(this);
        btn = findViewById(R.id.button);
        webView = findViewById(R.id.webView);
        btn.setOnClickListener(this);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        Normandy.getNormandy().register(tali);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String uri, String defaultValue, JsPromptResult result) {
                String schema = Uri.parse(uri).getScheme();
                if (schema.equals(Normandy.SECHEMA)) {
                    Normandy.getNormandy().on(view, uri);
                    result.confirm("");
                    return true;
                } else {
                    return super.onJsPrompt(view, url, uri, defaultValue, result);
                }

            }

        });
        webView.loadUrl("file:///android_asset/doc.html");
    }

    @Override
    public void onClick(View view) {
        Normandy.getNormandy().launch(webView, "android2js", "希望你可以带领自己的船员平安穿越星际间的广袤虚空");
    }
}

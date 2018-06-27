# 简介
1. 重写 WebChromeClient 拦截 window.prompt 以达成 js 调用 native。
2. 主动使用 webview.loadUrl("javascript:functionName(params)") 调用 js 中的方法。


# 原理

通过重写 WebChromeClient 的 `onJsPrompt` 方法，在可以拦截到 window.prompt(msg) 中的 msg 参数。而 native 和 js 端则以 msg 为解析协议，使用 java 的反射原理在 native 端进行类的调用。

> 协议基本结构：`${sechema}://${host}:${port}/${path}?${params}`

## js 端使用的协议


```
`${SECHEMA}://${obj}:${callBackNo}/${method}?${params}`
```
- `SECHEMA`: 协议常量，带有此常量的协议为 js 与 native 之间通信的协议
- `obj`： 调用 native 端的类名
- `callBackNo`：js 端注册的回调函数编号，native 端完成业务后，通过主动调用该回调函数传递处理完的数据
- `method`：调用 native 端的方法
- `params`：调用方法所接受的参数，一般是 json 字符串。

## native 端的解析方式

`Normandy.java`

```
Uri uri = Uri.parse(message);
String schema =uri.getScheme()
String callBackNo = uri.getPort() + "";
String params = uri.getQuery();
String methodName = uri.getPath().replace("/", "");
String className = uri.getHost();

```

> 原本我按照调用顺序把协议写成 `${SECHEMA}://${obj}:${method}/${callBackNo}?${params}` callBackNo 所在的 port 位置只接受数字，用 string 类型只能拿到 -1，因此只能把 method 放在 path 的位置。


# 具体实现

##  `js 篇`

在 html 中增加以下代码：

```

window.geth = {
  // 用来存放注册的函数
  callbacks: {},
  call: function (obj, method, params, callback) {
    // js 端调用 native 端的步骤：
    // 1 获取一个随机的 callBackNo
    // 2 将一个 callback 函数以 key - value 的形式保存至 callbacks，key 为第一步获得的 callBackNo
    // 3 生成协议
    // 4 主动使用 window.prompt 让 natvie 拦截到协议
    const callBackNo = this.getCallBackNo();
    this.callbacks[callBackNo] = callback;
    const uri = this.getUri(obj, method, params, callBackNo);
    window.prompt(uri);
  },
  // 根据注册的 callBackNo 调用 callbacks 中的回调函数
  onExecute: function (callBackNo, params) {
    if (!!this.callbacks[callBackNo]) {
      this.callbacks[callBackNo](params);
      delete this.callbacks[callBackNo];
    }
  },
  getCallBackNo: () => Math.floor(Math.random() * (1 << 30)),
  getUri: (obj, method, params, callBackNo) => `${SECHEMA}://${obj}:${callBackNo}/${method}?${params}`
};
```

调用：
```
document.getElementById('btn1').onclick = () => {
  const {
    geth
  } = window;
  // 调用 native 端 Tali.class 中的 onToast 方法，传递参数为 MARK_PHRASE，同时注册一个回调函数 res => mark(res)
  geth.call('Tali', 'onToast', MARK_PHRASE, res => mark(res));

}
```

## native

`MainActivity.java`

```
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

```

`Normandy.java`

```
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

public void callBack(WebView webView, String params, String callBackNo) {
    StringBuffer sb = new StringBuffer();
    sb.append("javascript:geth.onExecute(");
    sb.append(callBackNo + ",");
    sb.append("`" + params + "`" + ")");
    String callJs = sb.toString();
    webView.loadUrl(callJs);
}
```

`Tali.java`

```
public void onToast (String message, String callBackName, WebView webView) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show();
    String str = "keelah se'lai";
    finalCall(webView, str, callBackName);
}


```
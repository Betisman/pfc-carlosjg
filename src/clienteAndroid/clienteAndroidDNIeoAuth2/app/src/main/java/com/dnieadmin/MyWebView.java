package com.dnieadmin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MyWebView extends android.app.Activity{

    private ProgressDialog dialog;

    public class MyWebViewClient extends WebViewClient {

    }

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.webviewlayout);

        final WebView webView = (WebView)findViewById(R.id.mainwebview);

        // Construimos el progressDialog
        dialog = new ProgressDialog(this);
        dialog.setTitle("DNIe AUTENTICACION");
        dialog.setMessage("Cargando. Por favor, espere...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                // Get endResult
                String htmlString = extras.getString("htmlString");

                webView.setVisibility(WebView.VISIBLE);
                webView.setInitialScale(1);
                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUseWideViewPort(true);

                webView.getSettings().setAllowContentAccess(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAllowFileAccessFromFileURLs(true);
                webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

                // Añado una clase de interfaz javascript
                webView.addJavascriptInterface(new WebAppInterface(this), "Android");

                webView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Toast.makeText(webView.getContext(), "soul: " + url, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    public void onPageFinished(WebView view, String url) {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    }

                    public void onLoadResource(WebView view, String url) {
                        Toast.makeText(webView.getContext(), url, Toast.LENGTH_SHORT).show();
                    }
                });
                webView.loadDataWithBaseURL("", htmlString, "text/html", "utf-8", "");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            dialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                Intent intent = new Intent(MyWebView.this, DNIeAdmin.class);
                startActivity(intent);
                return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
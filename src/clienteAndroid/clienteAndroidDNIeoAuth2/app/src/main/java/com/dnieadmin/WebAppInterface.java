package com.dnieadmin;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Carlos on 05/03/2016.
 */
public class WebAppInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        ((Activity)mContext).finish();
    }

    public void enviarFormularioAdroid(String formParamsJSON) {
        try {
            // Recupero los parámetros
            JSONObject jsonObj = new JSONObject(formParamsJSON);

            // Cierro webview

            //
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

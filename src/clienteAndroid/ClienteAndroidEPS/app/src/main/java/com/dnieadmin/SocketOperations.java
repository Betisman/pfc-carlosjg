package com.dnieadmin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SocketOperations extends Activity {

 	private ProgressDialog dialog;
    public String m_url;
               
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.setContentView(R.layout.socketopsssl);
    	
        //Recuperamos la información pasada en el intent
  		Bundle bundle 	= this.getIntent().getExtras();
  		m_url 	= bundle.getString("MYURL");
  		
  		if(m_url == null)
  		{
  			Toast.makeText(getApplicationContext(), "No se ha podido cargar ninguna URL.", Toast.LENGTH_SHORT).show();
  			return;
  		}
  		
  		// Construimos el progressDialog
  		dialog = new ProgressDialog(this);
		dialog.setTitle(R.string.process_title);
		dialog.setMessage(getApplicationContext().getString(R.string.process_msg_web));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
  		
        try
        {
	        // Construímos el cliente Web
            WebView webview = (WebView) findViewById(R.id.wv1);
	    	webview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                public void onPageFinished(WebView view, String url) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            });
	    	
	        webview.getSettings().setSupportZoom(true);
	        webview.getSettings().setBuiltInZoomControls(true);
	        webview.getSettings().setUseWideViewPort(true);
	        webview.setInitialScale(1);
	        webview.loadUrl(m_url);
	        
        }catch(Exception e)
        {
        	dialog.dismiss();
        }
    }		
}
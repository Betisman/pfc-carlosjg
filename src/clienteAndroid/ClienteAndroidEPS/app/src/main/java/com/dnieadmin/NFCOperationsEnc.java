package com.dnieadmin;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.Security;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import com.dnieadmin.gui.MyCaCertificate;
import com.dnieadmin.gui.MyPasswordDialog;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;

import es.gob.jmulticard.card.dnie.FakeX509Certificate;
import es.gob.jmulticard.jse.provider.DnieProvider;
import es.gob.jmulticard.ui.passwordcallback.DNIeDialogManager;
import es.inteco.labs.android.utils.DNIeMovilLogger;
import es.inteco.labs.net.DNIeCaCertsManager;
import es.inteco.labs.net.DroidHttpClient;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/////////////////////////////////////////////
public class NFCOperationsEnc extends Activity {
	// Tecnologías posibles sobre las que trabajar
	private NfcA exNfcA;
	private NfcB exNfcB;
	private IsoDep exIsoDep;
			
	private CANSpecDO canDnie;
	private CANSpecDOStore cansDO;
	
	private boolean m_bRestart = false;

	private Context myContext;
	Typeface fontType;
	 
	private Tag tagFromIntent;
	
    private static final String AUTH_CERT_ALIAS = "CertAutenticacion"; //$NON-NLS-1$
    private static final String SIGN_CERT_ALIAS = "CertFirmaDigital"; //$NON-NLS-1$
	
    private KeyStore m_ksUserDNIe;
    private String m_SSLtargetURL;
	private String m_SSLresultado;

	private WebView webView;
	
	private ProgressDialog progressDlg;
	final Handler handler = new Handler();
	private String textoProcessDlg;
	
	final Runnable updateStatus = new Runnable() {
		public void run() 
		{
			progressDlg.setMessage(textoProcessDlg);
		}
	};
	
	public class MyTaskDNIe extends AsyncTask<Void, Integer, Void>
	{		
		private boolean bCompleted = false;
		private String strError;

	    @Override        
	    protected void onPreExecute() 
	    { 
	    	// Limpiamos controles			
 			findViewById(R.id.result1).setVisibility(TextView.INVISIBLE);
 			findViewById(R.id.result2).setVisibility(TextView.INVISIBLE);
 			findViewById(R.id.resultimg).setVisibility(ImageView.INVISIBLE);
 			findViewById(R.id.resultinfo).setVisibility(TextView.INVISIBLE);
 			findViewById(R.id.BtnNFC_BACK).setVisibility(Button.INVISIBLE);
			
			// Lanzamos el diálogo con el progreso
			progressDlg.setIndeterminate(true);
			progressDlg.setCancelable(false);
			progressDlg.setTitle(R.string.process_title);
			progressDlg.setMessage(getApplicationContext().getString(R.string.process_msg_dni));
			progressDlg.show();
	    }
		
		@Override
	    protected Void doInBackground(Void... arg0) 
			{
				//////////////////////////////////////////////////////////////////////////////////
				// PASO 1: Leemos el CDF para obtener los certificados almacenados en la tarjeta
				//
				try 
				{
					CargarCDF();
				} 
				catch (Exception e) 
				{
					strError = "Ocurrió un error durante la lectura de ficheros.";
					
		    		if (e.getMessage()!=null)
		    		{
		    			if (e.getMessage().contains("lost"))
		    				strError = "Error de comunicación. Se ha perdido la conexión con el DNIe.";
		    			else
		    				strError = e.getMessage();
		    		}
		    			
		    		return null;
				}
				
				//////////////////////////////////////////////////////////////////////////////////
				// PASO 2: Lanzamos la conexión SSL
				//
				m_bRestart = false;
				try 
				{				
					// Obtencion mediante DroidHttp 3.0:
					// Creamos el objeto que gestionará los certificados adicionales
					MyCaCertificate myCert = new MyCaCertificate(NFCOperationsEnc.this);
					DNIeCaCertsManager.setCaCertHandler(myCert);
					
					// Actualizamos el estado del progressDialog
					textoProcessDlg="Estableciendo conexión web...";
		        	handler.post(updateStatus);
					HttpEntity webContent = DroidHttpClient.executeRequest(m_SSLtargetURL, getApplicationContext(), m_ksUserDNIe);

					String codificacion = EntityUtils.getContentCharSet(webContent);
		  			codificacion = (codificacion==null) ? "utf-8" : codificacion;
		  			String htmlPuntos = new String(EntityUtils.toByteArray(webContent),Charset.forName(codificacion));
		  			DroidHttpClient.cleanCookies();
                    m_SSLresultado = htmlPuntos;
					bCompleted 	= true;
				}
				catch (ClientProtocolException e) {
					e.printStackTrace();
					strError = "No se ha podido establecer la conexión.";
				}
				catch (SocketTimeoutException e) {
					e.printStackTrace();
					strError = "No se ha podido realizar la conexión. Se ha sobrepasado el tiempo de espera...";
				}
				catch (IOException e) {
					e.printStackTrace();
					strError = "No se ha podido realizar la conexión. Asegúrese de que está conectado a Internet...";
				}
				catch (Exception e) {
					DNIeMovilLogger.e(e);
					e.printStackTrace();
					
		  			String alternateMessage = "No se ha podido establecer la conexión SSL.";
		  			strError = "Error en la conexión con el servidor.\n" + (e.getMessage()!= null ? ": " + e.getMessage() : alternateMessage);
				}
	            
				return null;
	        }

	    @Override
	    protected void onPostExecute(Void result) { 
		    	
	    		// Destruímos el cuadro de diálogo
	    		progressDlg.dismiss();
    		
	    		// Si la operación se detuvo inesperadamente, salimos.
	    		if(!bCompleted)
	    		{
	    			HandleError(strError);
	    			return;
	    		}

                // Adaptamos el contenido web a las style sheet locales
                m_SSLresultado = m_SSLresultado.replace("/FwFront/", "file:///android_asset/madrid/FwFront/");
				
				webView.loadDataWithBaseURL("file:///android_asset/",m_SSLresultado,"text/html", "utf-8", "file:///");
	  			webView.setVisibility(WebView.VISIBLE);
				webView.setInitialScale(1);
				webView.getSettings().setSupportZoom(true);
				webView.getSettings().setBuiltInZoomControls(true);
				webView.getSettings().setUseWideViewPort(true) ;
				webView.getSettings().setLoadWithOverviewMode(true);
				webView.setWebViewClient(new WebViewClient());
				
				// Limpiamos el keystore
				m_ksUserDNIe=null;
				Security.removeProvider("DNIeJCAProvider");
        }
    }
		  
    @Override
    public void onCreate(Bundle savedState) {    	
        super.onCreate(savedState);
        
        // Si no hemos abierto correctamente, salimos
        if(!((MyAppDNIEADMIN)getApplicationContext()).isStarted())
        {
        	Toast.makeText(getApplicationContext(), "Esta aplicación había quedado abierta irregularmente. Salimos.\n", Toast.LENGTH_LONG).show();
    		
        	// Desactivamos la activity ENABLE = false
        	PackageManager packman = getApplicationContext().getPackageManager();
        	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
        	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);//*/
      
        	android.os.Process.killProcess(android.os.Process.myPid());
	     	System.exit(0);
	     	
        	return;
        }
        
        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.nfcact2);
        
        tagFromIntent 	= null;
    	myContext 		= NFCOperationsEnc.this;

		Intent intent = getIntent();
        
		// Intentamos leer el DNIe con el CAN que nos hayan pasado
		cansDO = new CANSpecDOStore(this);
		canDnie = ((MyAppDNIEADMIN)getApplicationContext()).getCAN();
        resolveIntent(intent);
        			
        // Obtenemos la url de conexión
        m_SSLtargetURL  = ((MyAppDNIEADMIN)getApplicationContext()).getUrl();
        webView = (WebView) findViewById(R.id.wvKS);
		webView.loadUrl("about:blank");
		webView.setVisibility(WebView.INVISIBLE);
		
        // Ajustamos tipo de letra
        fontType = Typeface.createFromAsset(myContext.getAssets(),"fonts/NeutraText-LightAlt.otf"); 
    	TextView myText = (TextView)findViewById(R.id.result1);
		//myText.setVisibility(TextView.INVISIBLE);
		myText.setTypeface(fontType);
		
		myText = (TextView)findViewById(R.id.result2);
		myText.setVisibility(TextView.INVISIBLE);
		myText.setTypeface(fontType);
		
		myText = (TextView)findViewById(R.id.resultinfo);
		myText.setVisibility(TextView.INVISIBLE);
		myText.setTypeface(fontType);
		
		// Creamos el diálogo de progreso
		progressDlg = new ProgressDialog(NFCOperationsEnc.this);
		
		if(tagFromIntent==null)
		{
			HandleError("No hay disponible ningún dispositivo NFC.");
			return;
		}
		
		// Interfaz NfcA o NfcB
    	if( (exNfcA!=null) ||
    		(exNfcB!=null))
    	{
    		if(exIsoDep!=null)
    		{    			
    			exIsoDep.setTimeout(10000);
    			
    			MyTaskDNIe newTask = new MyTaskDNIe();
    			newTask.execute();
    		}  
    	} 

		///////////////////////////////////////////////////////////////////////////////////
		// Botón de vuelta al Activity anterior
    	Button btnNFCBack = (Button)findViewById(R.id.BtnNFC_BACK);    	
    	btnNFCBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// Devolvemos el valor del módulo al Activity anterior
				Intent intent = new Intent(NFCOperationsEnc.this, DNIeCanSelection.class);
				startActivity(intent);
			}
		});
    }
    
    public void HandleError(String strError) 
    {
    	// Indicamos el error en la activity
		((TextView)findViewById(R.id.result1)).setText(R.string.result_nok_title);
		findViewById(R.id.result1).setVisibility(TextView.VISIBLE);
		((TextView)findViewById(R.id.result2)).setText(R.string.result_nok_description);
		findViewById(R.id.result2).setVisibility(TextView.VISIBLE);
		((ImageView)findViewById(R.id.resultimg)).setImageResource(R.drawable.checked_false);
		findViewById(R.id.resultimg).setVisibility(ImageView.VISIBLE);
		((TextView)findViewById(R.id.resultinfo)).setText(strError);
		findViewById(R.id.resultinfo).setVisibility(TextView.VISIBLE);
		findViewById(R.id.BtnNFC_BACK).setVisibility(Button.VISIBLE);
		    		
		// Activamos botón "VOLVER"
        Button btnVolver = (Button)findViewById(R.id.BtnNFC_BACK);
        btnVolver.setVisibility(Button.VISIBLE);
        btnVolver.setClickable(true);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		// Activamos la activity ENABLE = true
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);//*/
		
		if(m_bRestart)
		{
			// Si no hemos abierto correctamente, salimos
	        if(!((MyAppDNIEADMIN)getApplicationContext()).isStarted())
	        {
	        	Toast.makeText(getApplicationContext(), "Esta aplicación había quedado abierta irregularmente. Salimos.\n", Toast.LENGTH_SHORT).show();
	        	android.os.Process.killProcess(android.os.Process.myPid());
		     	System.exit(0);
	        	return;
	        }
			
			if(tagFromIntent==null)
			{
				HandleError("No hay disponible ningún dispositivo NFC.");
				return;
			}
			

			// Interfaz NfcA o NfcB
	    	if( (exNfcA!=null) ||
	    		(exNfcB!=null))
	    	{
	    		if(exIsoDep!=null)
	    		{
	    			exIsoDep.setTimeout(10000);
	    			
	    			MyTaskDNIe newTask = new MyTaskDNIe();
	    			newTask.execute();
	    		}
	    	} 
		}
	}    
        
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		// Activamos la activity ENABLE = true
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
        	// Devolvemos el valor del módulo al Activity padre
			Intent intent = new Intent(NFCOperationsEnc.this, DNIeCanSelection.class);
	        startActivity(intent);
	        return false;
        }
        else
        	return super.onKeyDown(keyCode, event);
    }

    public boolean CargarCDF() throws Exception
    { 
		// Leemos los certificados
		if(exIsoDep!=null)
		{			
    		try 
    		{
                // Activamos el modo rápido para agilizar la carga.
                System.setProperty("es.gob.jmulticard.fastmode", "true");
    			
    			final DnieProvider p = new DnieProvider();
    			p.setProviderTag(tagFromIntent);
    			p.setProviderCan(canDnie.getCanNumber());
    			Security.insertProviderAt(p, 1);

    			// Cargamos certificados y keyReferences
    			m_ksUserDNIe = KeyStore.getInstance("MRTD");
    			m_ksUserDNIe.load(null,null);

    	    	// Actualizamos la BBDD de los CAN para añadir estos datos
    	    	String certSubject = ((FakeX509Certificate)m_ksUserDNIe.getCertificate(AUTH_CERT_ALIAS)).getSubjectDN().toString();
    	    	CANSpecDO newCan = new CANSpecDO(canDnie.getCanNumber(), 
    	    					certSubject.substring((certSubject.indexOf("CN=")+3)), 
    	    					certSubject.substring(certSubject.indexOf("NIF ")+4));
    	    	cansDO.delete(canDnie);
    	    	cansDO.save(newCan);
    	    	
    	    	// Construímos el diálogo que solicitará el PIN
            	MyPasswordDialog myFragment = new MyPasswordDialog(NFCOperationsEnc.this, true);
            	DNIeDialogManager.setDialogUIHandler(myFragment);
            	
            	// Actualizamos el estado del progressDialog
				textoProcessDlg="Cargando certificados...";
	        	handler.post(updateStatus);
	        	
            	// Forzamos a cargar los certificados reales pidiendo la clave
    			m_ksUserDNIe.getKey(AUTH_CERT_ALIAS, null);
    			m_ksUserDNIe.getCertificate(AUTH_CERT_ALIAS);
    		}
    		catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
		}
		
		return true;
    }

    void resolveIntent(Intent intent) {

    	tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tagFromIntent!= null)
        {
        	// Preparamos los interfaces disponibles
        	exNfcA	 = NfcA.get(tagFromIntent);
        	exNfcB	 = NfcB.get(tagFromIntent);
        	exIsoDep = IsoDep.get(tagFromIntent);
        }

        // Parse the intent
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            // When a tag is discovered we send it to the service to be save. We
            // include a PendingIntent for the service to call back onto. This
            // will cause this activity to be restarted with onNewIntent(). At
            // that time we read it from the database and view it.
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }

            setTitle(R.string.str_nfc_newtag);
        } else {
            finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
        
        // Indicamos que ya se ha creado el Intent así que habrá que reiniciarlo, simplemente.
        m_bRestart = true;
    }
}

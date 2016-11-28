package com.dnieadmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dnieadmin.gui.MyCaCertificate;
import com.dnieadmin.gui.MyPasswordDialog;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.Security;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;
import de.tsenger.androsmex.pace.PaceException;
import es.gob.jmulticard.card.CryptoCardException;
import es.gob.jmulticard.card.dnie.FakeX509Certificate;
import es.gob.jmulticard.jse.provider.DnieProvider;
import es.gob.jmulticard.ui.passwordcallback.CancelledOperationException;
import es.gob.jmulticard.ui.passwordcallback.DNIeDialogManager;
import es.inteco.labs.net.DNIeCaCertsManager;
import es.inteco.labs.net.DroidHttpClient;

@SuppressLint("NewApi")
public class NFCOperationsEncKitKat extends Activity implements ReaderCallback {
	static private NfcAdapter myNfcAdapter = null;
	private Tag tagFromIntent;

	// Gestión del CAN
	private CANSpecDO canDnie;
	private CANSpecDOStore cansDO;

    private Activity myActivity;
	private Context myContext;
	Typeface fontType;

	private String m_SSLresultado;
	private String m_SSLtargetURL;
    private boolean bConexionOK = false;

	private int currentapiVersion;
	private int iDisplaySize;

    private static final String AUTH_CERT_ALIAS = "CertAutenticacion"; //$NON-NLS-1$

    private static final int SIZE_10_INCHES = 10;
    private static final int SIZE_7_INCHES  = 7;
    private static final int SIZE_5_INCHES  = 5;
    private static final int SIZE_4_INCHES  = 4;

    private KeyStore m_ksUserDNIe=null;

    final Handler myHandler = new Handler();
	private ProgressDialog progressBar;
	private String textoProcessDlg;
	private String textoResultDlg;

	final Runnable updateStatus = new Runnable() {
		public void run()
		{
			progressBar.setMessage(textoProcessDlg);
			progressBar.show();
		}
	};

	final Runnable cleanResult = new Runnable()
	{
		public void run()
		{
			// Cambiamos el fondo de pantalla
	    	findViewById(R.id.fondopantalla).setBackgroundResource(R.drawable.fondo_secundario);

			textoResultDlg ="";
			findViewById(R.id.result1).setVisibility(TextView.INVISIBLE);
			((ImageView)findViewById(R.id.resultimg)).setImageResource(R.drawable.checked_false);
			findViewById(R.id.resultimg).setVisibility(ImageView.INVISIBLE);
			findViewById(R.id.result2).setVisibility(TextView.INVISIBLE);
			findViewById(R.id.resultinfo).setVisibility(TextView.INVISIBLE);
	        findViewById(R.id.BtnNFC_BACK).setVisibility(Button.INVISIBLE);
		}
	};

	final Runnable askForRead = new Runnable()
	{
		public void run()
		{
			// Cambiamos el fondo de pantalla
			findViewById(R.id.fondopantalla).setBackgroundResource(R.drawable.fondo_secundario);

			textoResultDlg ="";
			((TextView)findViewById(R.id.result1)).setText(R.string.op_dgtreinit);
			findViewById(R.id.result1).setVisibility(TextView.VISIBLE);
			((ImageView)findViewById(R.id.resultimg)).setImageResource(R.drawable.dni30_grey_peq);
			findViewById(R.id.resultimg).setVisibility(ImageView.VISIBLE);
		}
	};

	final Runnable newRead = new Runnable()
	{
		public void run()
		{
			// Cambiamos el fondo de pantalla
	    	findViewById(R.id.fondopantalla).setBackgroundResource(R.drawable.fondo_secundario);

			textoResultDlg ="";
			((TextView)findViewById(R.id.result1)).setText(R.string.process_msg_lectura);
			findViewById(R.id.result1).setVisibility(TextView.VISIBLE);
			((ImageView)findViewById(R.id.resultimg)).setImageResource(R.drawable.dni30_peq);
			findViewById(R.id.resultimg).setVisibility(ImageView.VISIBLE);
			findViewById(R.id.result2).setVisibility(TextView.INVISIBLE);
			findViewById(R.id.resultinfo).setVisibility(TextView.INVISIBLE);
	        findViewById(R.id.BtnNFC_BACK).setVisibility(Button.INVISIBLE);
		}
	};

public class MyTaskDNIe extends AsyncTask<Void, Integer, Void>
	{
		boolean bForzamosReinicio = true;

	    @Override
	    protected void onPreExecute()
	    {
			bConexionOK = false;

			myHandler.post(newRead);

			// Lanzamos el diálogo con el progreso
 			progressBar.setIndeterminate(true);
 			progressBar.setCancelable(false);
 			progressBar.setTitle(R.string.process_title);
 			progressBar.setMessage(getApplicationContext().getString(R.string.process_msg_dni));
 			textoProcessDlg=getApplicationContext().getString(R.string.process_msg_dni);
	    }

	    @Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			progressBar.setMessage(textoProcessDlg);
			progressBar.show();
		}

		@Override
	    protected Void doInBackground(Void... arg0)
		{
			//////////////////////////////////////////////////////////////////////////////////
			// PASO 1: Leemos el CDF para obtener los certificados almacenados en la tarjeta
			//
			try
			{
				// Lanzamos la operación de lectura del DNIe
				CargarCDF();

				// Si llegamos hasta aquí, hemos solicitado y presentado el PIN,
				// de manera que ya no reintentaremos la conexión
				bForzamosReinicio = false;
			} catch (CryptoCardException e){
				bForzamosReinicio = false;
				return null;
			} catch (CancelledOperationException e){
				bForzamosReinicio = false;
				textoResultDlg = "Operación cancelada por el usuario.";
				return null;
			}  catch (PaceException e){
				// Si el código CAN es incorrecto, mostramos el error y no permitimos continuar.
				bForzamosReinicio   = false;
				textoResultDlg    	= e.getMessage();
				return null;
			} catch (Exception e){
				textoResultDlg = "Ocurrió un error durante la lectura de ficheros.";
				if (e.getMessage()!=null)
	    		{
	    			if (e.getMessage().contains("lost"))
	    				textoResultDlg = "Error de comunicación. Se ha perdido la conexión con el DNIe.";
	    			else
	    				textoResultDlg = e.getMessage();
	    		}

				return null;
			}

			//////////////////////////////////////////////////////////////////////////////////
			// PASO 2: Lanzamos la conexión SSL
			//
			try
			{
				// Lanzamos la operación de conexión con el servidor SSL
				bConexionOK = ConectarSSL();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
        }

	    @Override
	    protected void onPostExecute(Void result)
	    {
			// Destruímos el cuadro de diálogo
			progressBar.dismiss();

			// Si no hemos presentado el PIN aún, permitimos que se continuen las lecturas.
			// Es posible que el DNIe se haya movido sin querer así que intentamos reconectar
			if(bForzamosReinicio) {
				// Repintamos la pantalla
				myHandler.post(askForRead);

				// Si la versión de Android lo soporta, ajustamos la comprobación de presencia NFC a 1 segundo
				if (currentapiVersion > Build.VERSION_CODES.KITKAT)
					EnableReaderMode(1000);

				return;
			}

			// Desactivamos el modo reader de NFC
			DisableReaderMode();

			// Limpiamos el keystore y descargamos el provider del DNIe
			m_ksUserDNIe=null;
			Security.removeProvider("DNIeJCAProvider");

			// Si la operación se detuvo inesperadamente, mostramos el error y salimos.
			if(!bConexionOK)
			{
				HandleError(textoResultDlg);
				return;
			}

			// La operación se completó correctamente, así que actualizamos los controles
			myHandler.post(cleanResult);

			try {
				// Adaptamos el contenido web a las style sheet locales
				m_SSLresultado = m_SSLresultado.replace("/Fw", "file:///android_asset/Fw");
				m_SSLresultado = m_SSLresultado.replace("/assets/", "file:///android_asset/assets/");
				m_SSLresultado = m_SSLresultado.replace("/FWProjects", "file:///android_asset/FWProjects");
				m_SSLresultado = m_SSLresultado.replace(".jpg", ".png");

				// Lanzamos el intent con el resultado
				Intent myWebViewIntent = new Intent(NFCOperationsEncKitKat.this, MyWebView.class);
				myWebViewIntent.putExtra("htmlString", m_SSLresultado);
				//startActivity(myWebViewIntent);
				startActivityForResult(myWebViewIntent, 0);

			}catch (Exception e)
			{
				e.printStackTrace();
			}
        }
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				// A contact was picked.  Here we will just display it
				// to the user.
				//startActivity(new Intent(Intent.ACTION_VIEW, data));
				String ppppp = "ppppspspspsps";
			}
			String eeeee = "dwdwdwdwddw";
		}
	}

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.nfcactkitkat);

        // Inicializamos controles
        tagFromIntent 	= null;
    	myContext 		= NFCOperationsEncKitKat.this;
    	myActivity 		= ((Activity) myContext);

    	// Obtenemos el adaptador NFC
        myNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        myNfcAdapter.setNdefPushMessage(null, this);
    	myNfcAdapter.setNdefPushMessageCallback(null, this);

		// Lanzamos el diálogo con el progreso
		progressBar = new ProgressDialog(myContext);

		// Limpiamos controles
		findViewById(R.id.result2).setVisibility(TextView.INVISIBLE);
		findViewById(R.id.resultinfo).setVisibility(TextView.INVISIBLE);
		findViewById(R.id.BtnNFC_BACK).setVisibility(Button.INVISIBLE);

		currentapiVersion = android.os.Build.VERSION.SDK_INT;

		// Conexión con el DNIe
 		cansDO 	= new CANSpecDOStore(this);
 		canDnie = ((MyAppDNIEADMIN)getApplicationContext()).getCAN();

 		// Recuperamos la URL del servicio al que queríamos conectar
        m_SSLtargetURL =((MyAppDNIEADMIN)getApplicationContext()).getUrl();

        // Calculamos el tamaño de la pantalla
        iDisplaySize = getDisplaySize();

 		// Ajustamos tipo de letra
        fontType = Typeface.createFromAsset(myContext.getAssets(),"fonts/NeutraText-LightAlt.otf");
    	TextView myText = (TextView)findViewById(R.id.result1);
		myText.setTypeface(fontType);

		myText = (TextView)findViewById(R.id.result2);
		myText.setVisibility(TextView.INVISIBLE);
		myText.setTypeface(fontType);

		myText = (TextView)findViewById(R.id.resultinfo);
		myText.setVisibility(TextView.INVISIBLE);
		myText.setTypeface(fontType);

		///////////////////////////////////////////////////////////////////////////////////
		// Botón de vuelta al Activity anterior
    	Button btnNFCBack = (Button)findViewById(R.id.BtnNFC_BACK);
    	btnNFCBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

                // Desactivamos el modo reader de NFC, si fuera neceario
                DisableReaderMode();

				// Devolvemos el valor del módulo al Activity anterior
				onBackPressed();
			}
		});
    }

    public void HandleError(String strError)
    {
    	// Recolocamos los textos e imágenes
    	RelativeLayout layoutMain = (RelativeLayout)findViewById(R.id.mainRelativeLayout);
    	LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)layoutMain.getLayoutParams();
        switch(iDisplaySize)
        {
            case SIZE_10_INCHES:
            case SIZE_7_INCHES:
                params.setMargins(0, 400, 0, 0);
                findViewById(R.id.fondopantalla).setBackgroundResource(R.drawable.fondo800_dni);
                break;
            case SIZE_5_INCHES:
                params.setMargins(0, 600, 0, 0);
                findViewById(R.id.fondopantalla).setBackgroundResource(R.drawable.fondo1080_dni);
                break;
            default:
                params.setMargins(0, 400, 0, 0);
                findViewById(R.id.fondopantalla).setBackgroundResource(R.drawable.fondo1080_dni);
                break;
        }
    	layoutMain.setLayoutParams(params);

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

    public void CargarCDF() throws Exception {

		// Leemos los certificados
		try
		{
			// Actualizamos el estado del progress Dlg
			textoProcessDlg = "Accediendo al DNIe...";
			myHandler.post(updateStatus);

			// Activamos el modo rápido para agilizar la carga.
			System.setProperty("es.gob.jmulticard.fastmode", "true");

			final DnieProvider p = new DnieProvider();
			p.setProviderTag(tagFromIntent);
			p.setProviderCan(canDnie.getCanNumber());
			Security.insertProviderAt(p, 1);

			// Cargamos certificados y keyReferences
			m_ksUserDNIe = KeyStore.getInstance("MRTD");
			m_ksUserDNIe.load(null, null);

			// Actualizamos la BBDD de los CAN para añadir estos datos si no los tuviéramos
			if(canDnie.getUserNif().length()==0){
				String certSubject = ((FakeX509Certificate) m_ksUserDNIe.getCertificate(AUTH_CERT_ALIAS)).getSubjectDN().toString();
				CANSpecDO newCan = new CANSpecDO(canDnie.getCanNumber(),
						certSubject.substring((certSubject.indexOf("CN=") + 3)),
						certSubject.substring(certSubject.indexOf("NIF ") + 4));
				cansDO.delete(canDnie);
				cansDO.save(newCan);
			}

			// Construímos el diálogo que solicitará el PIN
			MyPasswordDialog myFragment = new MyPasswordDialog(NFCOperationsEncKitKat.this, true);
			DNIeDialogManager.setDialogUIHandler(myFragment);

			// Actualizamos el estado del progressDialog
			textoProcessDlg = "Cargando certificados...";
			myHandler.post(updateStatus);

			// Forzamos a cargar los certificados reales pidiendo la clave
			m_ksUserDNIe.getKey(AUTH_CERT_ALIAS, null);
			m_ksUserDNIe.getCertificate(AUTH_CERT_ALIAS).getEncoded();

		} catch (CryptoCardException e) {
			textoResultDlg = "Ocurrió un error durante la lectura del DNIe.\n";
			textoResultDlg += e.getMessage();
			e.printStackTrace();
			throw e;
		} catch (CancelledOperationException e) {
			textoResultDlg = "Ocurrió un error durante la lectura del DNIe.\n";
			textoResultDlg += "La operación de firma ha sido cancelada.";
			e.printStackTrace();
			throw new CancelledOperationException(textoResultDlg);
		} catch (Exception e) {
			textoResultDlg = "Ocurrió un error durante la lectura del DNIe.\n";
			e.printStackTrace();
			if (e.getMessage().contains("CAN incorrecto"))
				throw new PaceException("Error al montar canal PACE. CAN incorrecto.");
			else
				throw new Exception(e.getMessage());
		}
	}

    public boolean ConectarSSL() throws Exception
    {
		try
		{
			// Procedemos a la conexión con el servidor
			m_SSLresultado = "";
			textoProcessDlg="Lanzando conexion...";
			myHandler.post(updateStatus);

			// Creamos el objeto que gestionará los certificados adicionales
			MyCaCertificate myCert = new MyCaCertificate(NFCOperationsEncKitKat.this);
			DNIeCaCertsManager.setCaCertHandler(myCert);

			//HttpEntity webContent = DroidHttpClient.executeRequest(m_SSLtargetURL, myContext, m_ksUserDNIe);
			//HttpEntity webContent = DroidHttpClient.executeRequest("https://192.168.1.154:8443", myContext, m_ksUserDNIe);
			HttpEntity webContent = DroidHttpClient.executeRequest("https://37.134.154.40:8444", myContext, m_ksUserDNIe);

			String codificacion = EntityUtils.getContentCharSet(webContent);
  			codificacion = (codificacion==null) ? "utf-8" : codificacion;

			m_SSLresultado = new String(EntityUtils.toByteArray(webContent),Charset.forName(codificacion));

			// Hemos recogido el contenido del formulario. Hay que mostrarlo y que el usuario lo acepte.
			// Por ahora simulamos que lo hace.
			//Toast.makeText(myContext, m_SSLresultado.substring(0, 50), Toast.LENGTH_LONG);

			textoProcessDlg=m_SSLresultado;
			myHandler.post(updateStatus);

			final String sIP_VOTING_SERVER = "192.168.1.145";
			final String sIP_OAUTH_SERVER = "37.134.154.40:8444";

			/*******************************************/
			if (false) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String title = "Permisos oAuth2";
				String message = m_SSLresultado;
				if (title != null) builder.setTitle(title);

				builder.setMessage(message);
				builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//String url2 = "https://192.168.1.153:8443/web/authorize/?response_type=code&client_id=testclient&redirect_uri=h&state=somestate&client_type=androidnfcapp&step=2";
						String url2 = "https://" + sIP_OAUTH_SERVER + "/web/authorize/?response_type=code&client_id=testclient&redirect_uri=h&state=somestate&client_type=androidnfcapp&step=2";
						HttpEntity webContent = null;
						try {
							webContent = DroidHttpClient.executeRequest(url2, myContext, m_ksUserDNIe);
							String codificacion = EntityUtils.getContentCharSet(webContent);
							codificacion = (codificacion==null) ? "utf-8" : codificacion;
							m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));
							//Toast.makeText(myContext, m_SSLresultado.substring(0, 50), Toast.LENGTH_LONG);

							JSONObject jsonObj = new JSONObject(m_SSLresultado);
							String state = jsonObj.getString("state");
							String code = jsonObj.getString("code");
							String dni = jsonObj.getString("dnie");
							String url3 = "https://" + sIP_OAUTH_SERVER + "/api/v1/tokens?code=" + code + "&client_secret=testpassword&grant_type=authorization_code&client_id=testclient";

							webContent = DroidHttpClient.executeRequest(url3, myContext, m_ksUserDNIe);
							m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));
							jsonObj = new JSONObject(m_SSLresultado);
							String tokenid = jsonObj.getString("id");
							String accessToken = jsonObj.getString("access_token");
							String expiresIn = jsonObj.getString("expires_in");
							String tokenType = jsonObj.getString("token_type");
							String scope = jsonObj.getString("scope");
							String refreshToken = jsonObj.getString("refresh_token");

							String url4 = "https://" + sIP_OAUTH_SERVER + "/web/me?access_token=" + accessToken + "&client_secret=testpassword&grant_type=authorization_code&client_id=testclient";
							webContent = DroidHttpClient.executeRequest(url4, myContext, m_ksUserDNIe);
							m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));
							jsonObj = new JSONObject(m_SSLresultado);

							DroidHttpClient.cleanCookies();

							//WebView webview = new WebView(this);
							//setContentView(webview);
							byte[] post = EncodingUtils.getBytes("info=" + m_SSLresultado + "&auth_system_name=dnie&access_token=" + accessToken, "BASE64");
							//webview.postUrl("https://www.192.168.1.153/auth/after/", post);

							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + sIP_VOTING_SERVER + "/auth/after/?client_type=androidnfcapp&auth_system_name=dnie&access_token=" + accessToken + "&info=" + URLEncoder.encode(m_SSLresultado, "utf-8")));
							startActivity(browserIntent);
							System.exit(0);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				builder.show();
			}else {
				/*******************************************/


				String url2 = "https://" + sIP_OAUTH_SERVER + "/web/authorize/?response_type=code&client_id=testclient&redirect_uri=h&state=somestate&client_type=androidnfcapp&step=2";
				webContent = DroidHttpClient.executeRequest(url2, myContext, m_ksUserDNIe);
				m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));
				//Toast.makeText(myContext, m_SSLresultado.substring(0, 50), Toast.LENGTH_LONG);

				textoProcessDlg=m_SSLresultado;
				myHandler.post(updateStatus);

				JSONObject jsonObj = new JSONObject(m_SSLresultado);
				String state = jsonObj.getString("state");
				String code = jsonObj.getString("code");
				String dni = jsonObj.getString("dnie");
				String url3 = "https://" + sIP_OAUTH_SERVER + "/api/v1/tokens?code=" + code + "&client_secret=testpassword&grant_type=authorization_code&client_id=testclient";

				webContent = DroidHttpClient.executeRequest(url3, myContext, m_ksUserDNIe);
				m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));

				textoProcessDlg=m_SSLresultado;
				myHandler.post(updateStatus);

				jsonObj = new JSONObject(m_SSLresultado);
				String tokenid = jsonObj.getString("id");
				String accessToken = jsonObj.getString("access_token");
				String expiresIn = jsonObj.getString("expires_in");
				String tokenType = jsonObj.getString("token_type");
				String scope = jsonObj.getString("scope");
				String refreshToken = jsonObj.getString("refresh_token");

				String url4 = "https://" + sIP_OAUTH_SERVER + "/web/me?access_token=" + accessToken + "&client_secret=testpassword&grant_type=authorization_code&client_id=testclient";
				webContent = DroidHttpClient.executeRequest(url4, myContext, m_ksUserDNIe);
				m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));
				jsonObj = new JSONObject(m_SSLresultado);

				textoProcessDlg=m_SSLresultado;
				myHandler.post(updateStatus);

				DroidHttpClient.cleanCookies();

				//WebView webview = new WebView(this);
				//setContentView(webview);
				byte[] post = EncodingUtils.getBytes("info=" + m_SSLresultado + "&auth_system_name=dnie&access_token=" + accessToken, "BASE64");
				//webview.postUrl("https://www.192.168.1.153/auth/after/", post);

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + sIP_VOTING_SERVER + "/auth/after/?client_type=androidnfcapp&auth_system_name=dnie&access_token=" + accessToken + "&info=" + URLEncoder.encode(m_SSLresultado, "utf-8")));
				startActivity(browserIntent);
				System.exit(0);
			}
		}
		catch (ClientProtocolException e) {
			textoResultDlg = "No se ha podido establecer la conexión.";
			throw new ClientProtocolException(textoResultDlg);
		}
		catch (SocketTimeoutException e) {
			textoResultDlg = "No se ha podido realizar la conexión. Se ha sobrepasado el tiempo de espera...";
			throw new SocketTimeoutException(textoResultDlg);
		}
		catch (IOException e) {
            textoResultDlg = "No se ha podido realizar la conexión. Asegúrese de que está conectado a Internet..."
                    + ((e.getMessage() != null) ? ("\n" + e.getMessage()) : "");
			throw new IOException(textoResultDlg);
		}
		catch (Exception e) {
            e.printStackTrace();
            String alternateMessage = "No se ha podido establecer la conexión SSL.";
            textoResultDlg = "Error en la conexión con el servidor.\n" + (e.getMessage()!= null ? ": " + e.getMessage() : alternateMessage);
            throw new Exception(textoResultDlg);
		}

		return true;
    }

	@Override
    public void onResume() {
		super.onResume();

		// Activamos la lectura NFC tras unos segundos para dar tiempo a colocar la tarjeta
	    Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
			public void run() {
				if (!bConexionOK) {
					if (currentapiVersion <= Build.VERSION_CODES.KITKAT)
						EnableReaderMode(20000);
					else
						EnableReaderMode(1000);
				}
			}
		}, 1000);

    }

	@Override
	public void onTagDiscovered(Tag tag) {
		tagFromIntent = tag;

		// En versiones anteriores a KITKAT no se puede ajustar en tiempo real el timer de comprobación
		if (currentapiVersion > Build.VERSION_CODES.KITKAT){
			// Reconfiguramos el adaptador NFC para que el control de presencia de NFC no nos tire el canal
			EnableReaderMode(30000);
		}

		MyTaskDNIe newTask = new MyTaskDNIe();
		newTask.execute();
	}

	private boolean EnableReaderMode (int msDelay)
	{
		// Ponemos en msDelay milisegundos el tiempo de espera para comprobar presencia de lectores NFC
		Log.d("", "Enabling Reader Mode (" + msDelay + " ms)...");
		Bundle options = new Bundle();
		options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, msDelay);
        myNfcAdapter.enableReaderMode( 	myActivity,
                                        this,
                                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK|
                                        NfcAdapter.FLAG_READER_NFC_B,
                                        options);

		return true;
	}

	private boolean DisableReaderMode()
	{
		// Desactivamos el modo reader de NFC
		Log.d("", "Disabling Reader Mode...");
		myNfcAdapter.disableReaderMode(myActivity);
		return true;
	}

    public int getDisplaySize()
    {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        int dens=dm.densityDpi;
        double wi=(double)width/(double)dens;
        double hi=(double)height/(double)dens;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double screenInches = Math.sqrt(x+y);

        if(screenInches>=9)
            return SIZE_10_INCHES;
        if(screenInches>=6)
            return SIZE_7_INCHES;
        if(screenInches>=4.5)
            return SIZE_5_INCHES;
        else
            return SIZE_4_INCHES;
    }
}
package com.dnieadmin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dnieadmin.gui.MyCaCertificate;
import com.dnieadmin.gui.MyPasswordDialog;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreSpi;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;
import de.tsenger.androsmex.mrtd.DG11;
import de.tsenger.androsmex.mrtd.DG1_Dnie;
import de.tsenger.androsmex.mrtd.DG2;
import de.tsenger.androsmex.mrtd.DG7;
import de.tsenger.androsmex.mrtd.EF_COM;
import de.tsenger.androsmex.pace.PaceException;
import es.gob.jmulticard.card.CryptoCardException;
import es.gob.jmulticard.card.dnie.FakeX509Certificate;
import es.gob.jmulticard.jse.provider.DnieKeyStore;
import es.gob.jmulticard.jse.provider.DnieProvider;
import es.gob.jmulticard.jse.provider.MrtdKeyStoreImpl;
import es.gob.jmulticard.ui.passwordcallback.CancelledOperationException;
import es.gob.jmulticard.ui.passwordcallback.DNIeDialogManager;
import es.inteco.labs.net.DNIeCaCertsManager;
import es.inteco.labs.net.DroidHttpClient;
import es.inteco.labs.net.auth.dnie.DNIeKeyManagerImpl;

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
				//CargarDGs();

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
				startActivity(myWebViewIntent);

			}catch (Exception e)
			{
				e.printStackTrace();
			}
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
			//m_ksUserDNIe = KeyStore.getInstance("DNI");
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


	public boolean CargarDGs() throws PaceException, Exception
	{
		try
		{
			// Leemos los datos del DG1 y DG11
			textoProcessDlg="Leyendo datos...";
			myHandler.post(updateStatus);

			// Activamos el modo rápido para agilizar la carga.
			System.setProperty("es.gob.jmulticard.fastmode", "true");

			// Cargamos el proveedor de servicios del DNIe
			final DnieProvider p = new DnieProvider();
			p.setProviderTag(tagFromIntent);
			String can6digitos = canDnie.getCanNumber();
			while(can6digitos.length()<6)
				can6digitos = "0"+can6digitos;
			p.setProviderCan(can6digitos);
			Security.insertProviderAt(p, 1);

			DnieKeyStore m_ksUserMrtd = null;
			// Creamos el DnieKeyStore
			KeyStoreSpi ksSpi = new MrtdKeyStoreImpl();
			m_ksUserMrtd = new DnieKeyStore(ksSpi, p, "MRTD");
			m_ksUserMrtd.load(null, null);

			// Leemos la configuración para saber qué datos debemos obtener y cargar sólo los DGs que nos hayan solicitado
			readUserConfiguration();

			////////////////////////////////////////////////
			// Leemos el EF_COM para saber qué datos hay disponibles en el documento
			try{
				EF_COM m_efcom = m_ksUserMrtd.getEFCOM();
				byte[] tagList = m_efcom.getTagList();

				for(int idx=0;idx<tagList.length;idx++) {
					switch (tagList[idx]){
						case 0x61:
							// DG_1. Lo leemos siempre que esté disponible
							m_existDg1 = true;
							break;
						case 0x75:
							// DG_2. Lo leemos si el usuario lo especificó
							m_existDg2 = true;
							break;
						case 0x67:
							// DG_7. Lo leemos si el usuario lo especificó
							m_existDg7 = true;
							break;
						case 0x6B:
							// DG_11. Lo leemos siempre que esté disponible
							m_existDg11 = true;
							break;
					}
				}
			}catch (Exception e)
			{
				e.printStackTrace();
				throw e;
			}

			////////////////////////////////////////////////
			// Leemos el DG1
			try{
				if(m_readDg1&&m_existDg1)
					m_dg1  = m_ksUserMrtd.getDatagroup1();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			////////////////////////////////////////////////
			// Leemos el DG11
			try{
				if(m_readDg11&&m_existDg11)
					m_dg11 = m_ksUserMrtd.getDatagroup11();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

/*			// Actualizamos la BBDD de los CAN para añadir estos datos si no estuvieran
			if(canDnie.getUserNif().length()==0)
			{
				String docNumber;
				String certSubject = m_dg1.getName() + " " + m_dg1.getSurname();
				if (m_dg11 == null)
					docNumber = m_dg1.getDocNumber();
				else
					docNumber = m_dg11.getPersonalNumber();
				CANSpecDO newCan = new CANSpecDO(canDnie.getCanNumber(), certSubject, docNumber);
				cansDO.delete(canDnie);
				cansDO.save(newCan);
			}

			////////////////////////////////////////////////
			// Leemos el DG2
			if(m_readDg2&&m_existDg2)
			{
				try{
					// Leemos los datos del DG2
					textoProcessDlg = "Cargando foto...";
					myHandler.post(updateStatus);

					// Obtenemos la imagen del ciudadano
					m_dg2 = m_ksUserMrtd.getDatagroup2();

				}catch (Exception e)
				{
					e.printStackTrace();
					throw e;
				}
			}

			////////////////////////////////////////////////
			// Leemos el DG7
			if(m_readDg7&&m_existDg7)
			{
				try{
					// Leemos los datos del DG7
					textoProcessDlg = "Cargando firma...";
					myHandler.post(updateStatus);

					// Obtenemos la imagen del ciudadano
					m_dg7 = m_ksUserMrtd.getDatagroup7();
				}catch (Exception e)
				{
					e.printStackTrace();
					throw e;
				}
			}*/

			System.out.println(m_dg1.getDateOfBirth());
			System.out.println(m_dg1.getDateOfExpiry());
			System.out.println(m_dg1.getDocNumber());
			System.out.println(m_dg1.getDocType());
			System.out.println(m_dg1.getIssuer());
			System.out.println(m_dg1.getName());
			System.out.println(m_dg1.getNationality());
			System.out.println(m_dg1.getOptData());
			System.out.println(m_dg1.getSex());
			System.out.println(m_dg1.getSurname());
		}
		catch(Exception e)
		{
			textoResultDlg = "Ocurrió un error durante la lectura de los DGs.\n";
			if(e.getMessage()!=null) {
				if (e.getMessage().contains("CAN incorrecto")) {
					textoResultDlg = "Error al montar canal PACE. CAN incorrecto.";
					throw new PaceException(textoResultDlg);
				}

				if (e.getMessage().contains("Tag was lost")) {
					textoResultDlg += "Se perdió la conexión inalámbrica con el DNI electrónico.";
					throw new Exception(textoResultDlg);
				}

				textoResultDlg += e.getMessage();
			}

			throw new Exception(textoResultDlg);
		}

		return true;
	}

	private boolean m_readDg1;
	private boolean m_readDg2;
	private boolean m_readDg7;
	private boolean m_readDg11;

	// Variables miembro de los ficheros disponibles en el documento
	private boolean m_existDg1;
	private boolean m_existDg2;
	private boolean m_existDg7;
	private boolean m_existDg11;

	private DG1_Dnie m_dg1;
	private DG11 m_dg11;
	private DG2 m_dg2;
	private DG7 m_dg7;

	void readUserConfiguration()
	{
		// Actualizamos los valores mostrados para cuenta y contraseña
		SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.sp.main_preferences", Context.MODE_PRIVATE);

		String SETTING_READ_DG1 = "read_DG_1";
		String SETTING_READ_DG2 = "read_DG_2";
		String SETTING_READ_DG7 = "read_DG_7";
		String SETTING_READ_DG11 = "read_DG_11";

		// Recupera los valores de lectura de DGs
		m_readDg1  = sharedPreferences.getBoolean(SETTING_READ_DG1, true);
		m_readDg2  = sharedPreferences.getBoolean(SETTING_READ_DG2, true);
		m_readDg7  = sharedPreferences.getBoolean(SETTING_READ_DG7, false);
		m_readDg11 = sharedPreferences.getBoolean(SETTING_READ_DG11, true);
	}

	public HttpsURLConnection setUpHttpsConnection(String urlString)
	{
		try
		{
			// Load CAs from an InputStream
			// (could be from a resource or ByteArrayInputStream or ...)
			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			InputStream caInput = new BufferedInputStream(this.getBaseContext().getAssets().open("CARLOSPFCUSPEPS.crt"));
			Certificate ca = cf.generateCertificate(caInput);
			System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

			// Create a KeyStore containing our trusted CAs
			//String keyStoreType = KeyStore.getDefaultType();
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);


			// Certificados cliente en KeyManagers
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(m_ksUserDNIe, "Pavone16!".toCharArray());




			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			//context.init(null, tmf.getTrustManagers(), null);
			//context.init(new KeyManager[]{new DNIeKeyManagerImpl(m_ksUserDNIe, (char[])null)}, tmf.getTrustManagers(), null);

			// Tell the URLConnection to use a SocketFactory from our SSLContext
			URL url = new URL(urlString);
			HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
			urlConnection.setSSLSocketFactory(context.getSocketFactory());

			return urlConnection;
		}
		catch (Exception ex)
		{
			//Log.e("", "Failed to establish SSL connection to server: " + ex.toString());
			ex.printStackTrace();
			return null;
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

			if (1 == 0) {
				// Creamos el objeto que gestionará los certificados adicionales
				MyCaCertificate myCert = new MyCaCertificate(NFCOperationsEncKitKat.this);
				DNIeCaCertsManager.setCaCertHandler(myCert);

				HttpEntity webContent = DroidHttpClient.executeRequest(m_SSLtargetURL, myContext, m_ksUserDNIe);

				String codificacion = EntityUtils.getContentCharSet(webContent);
				codificacion = (codificacion == null) ? "utf-8" : codificacion;

				m_SSLresultado = new String(EntityUtils.toByteArray(webContent), Charset.forName(codificacion));
				DroidHttpClient.cleanCookies();
			}else{
				HttpsURLConnection urlConnection = setUpHttpsConnection(m_SSLtargetURL);
				InputStream in = urlConnection.getInputStream();

				m_SSLresultado = in.toString();
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
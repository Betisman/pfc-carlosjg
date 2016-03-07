package com.dnieadmin;

import com.dnieadmin.gui.MyRadialMenuItem;
import com.dnieadmin.gui.MyRadialMenuWidget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class DNIeAdmin extends FragmentActivity {
		 
	 // Miembros de la clase DNIeAdmin
	public String m_url;

	private boolean doubleBackToExitPressedOnce = false;

	// Menú circular
	private MyRadialMenuWidget pieMenu;
	public MyRadialMenuItem itemTributos;
	public MyRadialMenuItem itemMultas;
	public MyRadialMenuItem itemCenso;
	public MyRadialMenuItem itemExpedientes;
	public MyRadialMenuItem itemMadrid;
	public MyRadialMenuItem itemHelp;
	
    private ViewGroup container = null;

    int m_SelectedAct = NOTIFICACION_OPT_NO_OP;

    // Urls de acceso a servicios del Ayuntamiento
//    private static final String URL_AYUNTAMIENTO= "http://www.madrid.es/portales/munimadrid/es/Inicio/Ayuntamiento?vgnextfmt=default&vgnextchannel=ce069e242ab26010VgnVCM100000dc0ca8c0RCRD";
//    private static final String URL_MULTAS 		= "https://sede-c.madrid.es/portal/site/tramites/template.LOGIN/action.process/?realm=realm1&loginType=ssl&rl=/portal/site/tramites/menuitem.29e9b77c901fb5b380d480d45141b2a0/?vgnextoid=23d8adb92e389210VgnVCM100000171f5a0aRCRD";
//    private static final String URL_CENSO		= "https://sede-c.madrid.es/portal/site/tramites/template.LOGIN/action.process/?realm=realm1&loginType=ssl&rl=/portal/site/tramites/menuitem.378c1b1111a70f38c134c1344680a5a0/?vgnextoid=273fec942a6d9210VgnVCM100000171f5a0aRCRD";
//	private static final String URL_EXPEDIENTES = "https://sede-c.madrid.es/portal/site/tramites/template.LOGIN/action.process/?realm=realm1&loginType=ssl&rl=/portal/site/tramites/menuitem.744d92db5bd2a648c134c1344680a5a0/?vgnextoid=6320115579d89210VgnVCM100000171f5a0aRCRD";
//	private static final String URL_TRIBUTOS 	= "https://sede-c.madrid.es/portal/site/tramites/template.LOGIN/action.process/?realm=realm1&loginType=ssl&rl=/portal/site/tramites/menuitem.0d1df42f88c5b5b380d480d45141b2a0/?vgnextoid=85e5eb4119989210VgnVCM100000171f5a0aRCRD";

	private static final String URL_AYUNTAMIENTO= "https://192.168.1.153:8443";
	private static final String URL_MULTAS 		= "https://192.168.1.153:8443";
	private static final String URL_CENSO		= "https://192.168.1.153:8443";
	private static final String URL_EXPEDIENTES = "https://192.168.1.153:8443";
	private static final String URL_TRIBUTOS 	= "https://192.168.1.153:8443";

	private static final int NOTIFICACION_OPT_NO_OP	= -1;
	private static final int NOTIFICACION_OPT_0 	= 0;	// Tributos
	private static final int NOTIFICACION_OPT_1 	= 1;	// Gestión de multas
	private static final int NOTIFICACION_OPT_2 	= 2;	// Datos censales
	private static final int NOTIFICACION_OPT_3 	= 3;	// Consulta de expedientes
	private static final int NOTIFICACION_OPT_4 	= 4;	// Madrid.es
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.setContentView(R.layout.main);
        
        // Indicamos que la aplicación ha empezado bien
        ((MyAppDNIEADMIN)getApplicationContext()).setStarted(true);
                
        // Construímos los controles
        container = (ViewGroup)findViewById(R.id.grupo_opciones);
        
        boolean bIsTabletSize= false;
        if ((getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) 
        {
        	 bIsTabletSize= true;
        }


		LanzarActivity(NOTIFICACION_OPT_2);

    }
        
    private void ShowThePieMenu()
    { 
    	LayoutInflater inflater = this.getLayoutInflater();
    	final View layout = inflater.inflate(R.layout.main, null);
    	layout.post(new Runnable() { 
    		public void run() { 
    			pieMenu.show(layout); 
    			pieMenu.setActivated(true);
    		} 
    	}); 
    }
    
    private void LanzarActivity(int iAct)
    {
    	Intent intent = null;
    	
    	// Lanzamos la activity correspondiente al botón
    	switch(iAct)
		{
            case NOTIFICACION_OPT_0:
                intent = new Intent(DNIeAdmin.this, DNIeCanSelection.class);
                m_url = URL_TRIBUTOS;
                ((MyAppDNIEADMIN)getApplicationContext()).setUrl(m_url);
	        	break;
    		case NOTIFICACION_OPT_1:
    			intent = new Intent(DNIeAdmin.this, DNIeCanSelection.class);
                m_url = URL_MULTAS;
                ((MyAppDNIEADMIN)getApplicationContext()).setUrl(m_url);
				break;
			case NOTIFICACION_OPT_2:
                intent = new Intent(DNIeAdmin.this, DNIeCanSelection.class);
                m_url = URL_CENSO;
                ((MyAppDNIEADMIN)getApplicationContext()).setUrl(m_url);
		    	break;
			case NOTIFICACION_OPT_3:
                intent = new Intent(DNIeAdmin.this, DNIeCanSelection.class);
                m_url = URL_EXPEDIENTES;
                ((MyAppDNIEADMIN)getApplicationContext()).setUrl(m_url);
            	break;
			case NOTIFICACION_OPT_4:
                intent = new Intent(DNIeAdmin.this, SocketOperations.class);
                m_url = URL_AYUNTAMIENTO;
                intent.putExtra("MYURL", m_url);
            	break;
		}
    	
    	startActivityForResult(intent, 1);
    }
    
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
        	this.moveTaskToBack(true);
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Pulse de nuevo VOLVER para salir...", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	doubleBackToExitPressedOnce=false; 
            }
        }, 2000);
    }
    
    private void SeleccionarOpcion(int option)
    {  	
    	// Ocultamos la opción anterior y mostramos la nueva
    	if(m_SelectedAct!=NOTIFICACION_OPT_NO_OP)
    		container.getChildAt(m_SelectedAct).setVisibility(Button.INVISIBLE);
		
    	if(option!=NOTIFICACION_OPT_NO_OP)
    		container.getChildAt(option).setVisibility(View.VISIBLE);  
		
		// Dejamos indicada la operación actual
    	m_SelectedAct = option;
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		ShowThePieMenu();

		String msg = "inicializado";
		Intent intent = getIntent();
		if (intent != null){

			String foo = intent.getStringExtra("foo");
			if (foo != null){
				msg = foo;
			}else{
				msg = "NULLLLLL";
			}
			new AlertDialog.Builder(this)
					.setTitle("Intents rulez")
					.setMessage(msg)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// continue with delete
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();

		}else{
			msg = "INTENT NULLL";
		}
	}
}
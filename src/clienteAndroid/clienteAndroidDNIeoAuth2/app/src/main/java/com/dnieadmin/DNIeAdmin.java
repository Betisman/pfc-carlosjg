package com.dnieadmin;

import com.dnieadmin.gui.MyRadialMenuItem;
import com.dnieadmin.gui.MyRadialMenuWidget;

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
	
    private ViewGroup container = null;


   	private static final String URL_OAUTH_SERVER		= "https://192.168.1.153:8443/web/authorize/?response_type=code&client_id=testclient&redirect_uri=h&state=somestate&client_type=androidnfcapp&step=1";

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
        
        /*// Menú circular
        pieMenu = new MyRadialMenuWidget(this);
        
        // Items del menú circular
        itemHelp = new MyRadialMenuItem("Help", null);
		itemHelp.setDisplayIcon(R.drawable.boton_info2);
		itemHelp.setOnMenuItemPressed(new MyRadialMenuItem.RadialMenuItemClickListener() {
			@Override
			public void execute() {
				// Deseleccionamos cualquier opción que hubiese antes
				SeleccionarOpcion(NOTIFICACION_OPT_NO_OP);
				
				FragmentManager fragmentManager = getSupportFragmentManager();
				HelpActivity newFragment = new HelpActivity();
				newFragment.show(fragmentManager, "HelpDialog");
			}
		});

        itemTributos = new MyRadialMenuItem("Multas", null);
        itemTributos.setDisplayIcon(R.drawable.icon_multas);
        itemTributos.setDisplayIconSelected(R.drawable.icon_multas_selected);
        itemTributos.setOnMenuItemPressed(new MyRadialMenuItem.RadialMenuItemClickListener() {
			@Override
			public void execute() {
				// Mostramos el estado que corresponda
				if(m_SelectedAct != NOTIFICACION_OPT_0)
					SeleccionarOpcion(NOTIFICACION_OPT_0);
				else
					LanzarActivity(NOTIFICACION_OPT_0);
			}
		});

        itemMultas = new MyRadialMenuItem("Puntos", null);
        itemMultas.setDisplayIcon(R.drawable.icon_transportes);
        itemMultas.setDisplayIconSelected(R.drawable.icon_transportes_selected);
        itemMultas.setOnMenuItemPressed(new MyRadialMenuItem.RadialMenuItemClickListener() {
			@Override
			public void execute() {				
				// Mostramos el estado que corresponda
				if(m_SelectedAct != NOTIFICACION_OPT_1)
					SeleccionarOpcion(NOTIFICACION_OPT_1);
				else
					LanzarActivity(NOTIFICACION_OPT_1);
			}
		});

        itemCenso = new MyRadialMenuItem("Conductores", null);
        itemCenso.setDisplayIcon(R.drawable.icon_datoscensales);
        itemCenso.setDisplayIconSelected(R.drawable.icon_datoscensales_selected);
        itemCenso.setOnMenuItemPressed(new MyRadialMenuItem.RadialMenuItemClickListener() {
			@Override
			public void execute() {				
				// Mostramos el estado que corresponda
				if(m_SelectedAct != NOTIFICACION_OPT_2)
					SeleccionarOpcion(NOTIFICACION_OPT_2);
				else
					LanzarActivity(NOTIFICACION_OPT_2);	
			}
		});

        itemExpedientes = new MyRadialMenuItem("Vehiculos", null);
        itemExpedientes.setDisplayIcon(R.drawable.icon_expedientes);
        itemExpedientes.setDisplayIconSelected(R.drawable.icon_expedientes_selected);
        itemExpedientes.setOnMenuItemPressed(new MyRadialMenuItem.RadialMenuItemClickListener() {
			@Override
			public void execute() {
				// Mostramos el estado que corresponda
				if(m_SelectedAct != NOTIFICACION_OPT_3)
					SeleccionarOpcion(NOTIFICACION_OPT_3);
				else
					LanzarActivity(NOTIFICACION_OPT_3);
			}
		});

        itemMadrid = new MyRadialMenuItem("Revista", null);
        itemMadrid.setDisplayIcon(R.drawable.icon_ayuntamiento);
        itemMadrid.setDisplayIconSelected(R.drawable.icon_ayuntamiento_selected);
        itemMadrid.setOnMenuItemPressed(new MyRadialMenuItem.RadialMenuItemClickListener() {
			@Override
			public void execute() {
				// Mostramos el estado que corresponda
				if(m_SelectedAct != NOTIFICACION_OPT_4)
					SeleccionarOpcion(NOTIFICACION_OPT_4);
				else
					LanzarActivity(NOTIFICACION_OPT_4);
			}
		});
		
		// Posicionamos el menú circular
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		if(bIsTabletSize)
		{
			pieMenu.setCenterLocation(size.x/2,size.y/2+120);
			pieMenu.setInnerRingRadius(100, 205);
			pieMenu.setCenterCircleRadius(100);
			pieMenu.setIconSize(50, 95);
			pieMenu.setTextSize(18);
		}
		else
		{
			pieMenu.setCenterLocation(size.x/2,size.y/2+100);
			pieMenu.setInnerRingRadius(70, 135);
			pieMenu.setCenterCircleRadius(70);
			pieMenu.setIconSize(30, 60);
			pieMenu.setTextSize(13);
		}
		
		// Configuramos el menú
		pieMenu.setAnimationSpeed(0L);
		pieMenu.setOutlineColor(Color.WHITE, 255);
		pieMenu.setInnerRingColor(Color.WHITE, 0);
		pieMenu.setDisabledColor(Color.WHITE, 255);
        pieMenu.setSelectedColor(Color.WHITE, 200);
		pieMenu.setCenterCircle(itemHelp);
		
		// Insertamos las opciones
		pieMenu.addMenuEntry(itemMultas);
		pieMenu.addMenuEntry(itemCenso);
		pieMenu.addMenuEntry(itemExpedientes);
		pieMenu.addMenuEntry(itemMadrid);
		pieMenu.addMenuEntry(itemTributos);*/

		LanzarActivity(0);
    }
        
    private void ShowThePieMenu()
    { 
    	/*LayoutInflater inflater = this.getLayoutInflater();
    	final View layout = inflater.inflate(R.layout.main, null);
    	layout.post(new Runnable() { 
    		public void run() { 
    			pieMenu.show(layout); 
    			pieMenu.setActivated(true);
    		} 
    	}); */
    }
    
   /* private void LanzarActivity(int iAct)
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
    }*/

	private void LanzarActivity(int iAct)
	{
		Intent intent = null;

		intent = new Intent(DNIeAdmin.this, DNIeCanSelection.class);
		m_url = URL_OAUTH_SERVER;
		((MyAppDNIEADMIN)getApplicationContext()).setUrl(m_url);

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
    	/*// Ocultamos la opción anterior y mostramos la nueva
    	if(m_SelectedAct!=NOTIFICACION_OPT_NO_OP)
    		container.getChildAt(m_SelectedAct).setVisibility(Button.INVISIBLE);
		
    	if(option!=NOTIFICACION_OPT_NO_OP)
    		container.getChildAt(option).setVisibility(View.VISIBLE);  
		
		// Dejamos indicada la operación actual
    	m_SelectedAct = option;*/
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ShowThePieMenu();
	}
}
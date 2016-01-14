package com.dnieadmin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

public class DNIeReader extends Activity {

	public static final String ACTION_READ = "ACTION_READ";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
        this.setContentView(R.layout.dnie_00);
    	    	    	    	             
        // Si no hemos abierto correctamente, salimos
        if(!((MyAppDNIEADMIN)getApplicationContext()).isStarted())
        {
        	Toast.makeText(getApplicationContext(), "Esta aplicación había quedado abierta irregularmente. Salimos.\n", Toast.LENGTH_LONG).show();
    		
        	// Desactivamos la activity ENABLE = false
        	PackageManager packman = getApplicationContext().getPackageManager();
        	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
        	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
      
        	android.os.Process.killProcess(android.os.Process.myPid());
	     	System.exit(0);
        }
    }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		// Activamos la activity ENABLE = true
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}
    
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		// Desactivamos la activity ENABLE = false
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	}
}
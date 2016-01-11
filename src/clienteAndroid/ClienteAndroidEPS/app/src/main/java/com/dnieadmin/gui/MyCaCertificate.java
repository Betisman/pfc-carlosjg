package com.dnieadmin.gui;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashSet;

import com.dnieadmin.R;

import es.inteco.labs.net.CaCertificatesHandler;

import android.content.Context;
import android.content.res.TypedArray;

public class MyCaCertificate implements CaCertificatesHandler{

	static private Context myContext;
	static private KeyStore trustKeyStore;
	static private HashSet<String> validCerts;
	
	
	public MyCaCertificate(final Context context) throws Exception 
	{
		// Guardamos el contexto 
		myContext = context;
    	
    	try
    	{
	    	// Obtenemos el keyStore del archivo indicado
	    	trustKeyStore = KeyStore.getInstance("BKS", "BC");
            InputStream certCA 	= myContext.getResources().openRawResource(R.raw.sedemadrid2017);
	        String passwordCA = "";
	        trustKeyStore.load(certCA,passwordCA.toCharArray());
	        
	        // Obtenemos los hashes de los certificados válidos desde el archivo indicado
	        TypedArray trustedHosts = myContext.getResources().obtainTypedArray(R.array.trusted_hosts);
	        validCerts = new HashSet<String>();
			for(int i = 0; i < trustedHosts.length(); i++){
				validCerts.add(trustedHosts.getString(i));
			}		
			
    	} catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    }

	@Override
	public Object getAndroidContext() {
		return myContext;
	}

    public HashSet<String> getValidCertificates(){
		return validCerts;    	
    }
    
    public KeyStore getCaKeyStore(){
		return trustKeyStore;  
    }
}
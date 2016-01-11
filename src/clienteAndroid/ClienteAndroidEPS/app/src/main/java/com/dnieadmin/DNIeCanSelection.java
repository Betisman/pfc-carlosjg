package com.dnieadmin;

import java.util.ArrayList;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DNIeCanSelection extends Activity implements OnClickListener, OnItemLongClickListener, OnItemClickListener{

	
	private static final String ACTION_LABEL_READ   = "Leer";
	private static final String ACTION_LABEL_EDIT   = "Modificar";
	private static final String ACTION_LABEL_DELETE = "Borrar";
	private static final String[] ACTION_LABELS = {ACTION_LABEL_READ,ACTION_LABEL_EDIT, ACTION_LABEL_DELETE};
	
	private static final int REQ_EDIT_NEW_CAN 	= 1;
	private static final int REQ_EDIT_CAN 		= 2;
	private static final int REQ_READ_PP 		= 3;
	private Button readNewW;
	private ListView listW;

	private CANSpecDOStore cans;
	private ArrayAdapter<CANSpecDO> listA;
	
	private CANSpecDO selectedBac;
	
	AlertDialog ad = null;
	private Context myContext = null;
	Typeface fontType;
	
	ArrayList<MrtdItem> mrtdItems = new ArrayList<MrtdItem>();
	private SampleAdapter m_adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        myContext = DNIeCanSelection.this;
     
        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.setContentView(R.layout.can_list);
        
        fontType = Typeface.createFromAsset(myContext.getAssets(),"fonts/NeutraText-LightAlt.otf"); 
	    	    
        cans = new CANSpecDOStore(this);
        prepareWidgets();

		///////////////////////////////////////////////////////////////////////////////////
		// Botón de vuelta al Activity anterior
		Button btnNFCBack = (Button)findViewById(R.id.BtnNFC_BACK);
		btnNFCBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// Devolvemos el valor del módulo al Activity anterior
				onBackPressed();
			}
		});
    }
	
	private void prepareWidgets() {
        readNewW = (Button) findViewById(R.id.BtnCAN_NEW);
        readNewW.setOnClickListener(this);
        readNewW.setTypeface(fontType);
        
        listW = (ListView) findViewById(R.id.canList);
        listA = new ArrayAdapter<CANSpecDO>(this, android.R.layout.simple_list_item_1, cans.getAll());
        
        int idx=0;
        while(idx < listA.getCount())
        {
        	CANSpecDO canItem = listA.getItem(idx);
        	mrtdItems.add(new MrtdItem(canItem.getCanNumber(), canItem.getUserName(), null));
        	idx++;
        }
        m_adapter = new SampleAdapter(getApplicationContext(), mrtdItems);
        listW.setAdapter(m_adapter);
        listW.setOnItemClickListener( this );
        listW.setOnItemLongClickListener( this );
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setIntent(data);

		if( requestCode == REQ_EDIT_NEW_CAN )
		{
			if( resultCode == RESULT_OK )
			{
				CANSpecDO can = data.getExtras().getParcelable( CANSpecDO.EXTRA_CAN );
				cans.save(can);
				refreshAdapter();
				read(can);
			}
		}
		else if( requestCode == REQ_EDIT_CAN )
		{
			if( resultCode == RESULT_OK )
			{
				CANSpecDO can = data.getExtras().getParcelable( CANSpecDO.EXTRA_CAN );
				cans.save(can);
				refreshAdapter();
			}
		}
		else if( requestCode == REQ_READ_PP )
		{
			if( resultCode == RESULT_OK )
			{
				Intent i;
				
			    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
			    	i = new Intent(this, DNIeReader.class).putExtras(data.getExtras()); 
			    else
			    	// Build.VERSION_CODES.KITKAT
			    	i = new Intent(this, com.dnieadmin.NFCOperationsEncKitKat.class).putExtras(data.getExtras());
				
			    startActivityForResult(i, 1);
			}
			else if( resultCode == RESULT_CANCELED )
			{
				toastIt("error");
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		if( v == readNewW )
		{
			LayoutInflater factory = LayoutInflater.from(myContext);
            final View canEntryView = factory.inflate(R.layout.can_entry, null);
            ad = new AlertDialog.Builder(myContext).create();
		    ad.setCancelable(false);
		    ad.setIcon(R.drawable.alert_dialog_icon);
		    ad.setView(canEntryView);
		    ad.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.psswd_dialog_ok), new DialogInterface.OnClickListener() 
		    {
		        public void onClick(DialogInterface dialog, int which) 
		        {
		        	EditText text = (EditText) ad.findViewById(R.id.can_editbox);
		        	CANSpecDO can = new CANSpecDO(text.getText().toString(), "", "");
					cans.save(can);
					refreshAdapter();
					read(can);
		        }
		    });
		    ad.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.psswd_dialog_cancel), new DialogInterface.OnClickListener() 
		    {
		        public void onClick(DialogInterface dialog, int which) 
		        {
		        	
		        }
		    });
			ad.show();
			
			// Ajustamos el tipo de letra
		    Typeface type = Typeface.createFromAsset(myContext.getAssets(),"fonts/NeutraText-LightAlt.otf"); 
		    ((TextView)ad.findViewById(R.id.can_textbox)).setTypeface(type);
		    ((TextView)ad.findViewById(R.id.can_editbox)).setTypeface(type);
		}
	}
	
	private void read(CANSpecDO b)
	{
		ArrayList<CANSpecDO> cans = new ArrayList<CANSpecDO>();
		cans.add(b);
		
		// Dejamos disponible el CAN para la lectura del DNIe
		((MyAppDNIEADMIN)getApplicationContext()).setCAN(b);
			 
		read( cans );
	}
	private void read(ArrayList<CANSpecDO> bs)
	{
		Intent i;
				
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion <= Build.VERSION_CODES.JELLY_BEAN_MR2)
			i = new Intent( DNIeCanSelection.this, DNIeReader.class )
			.putParcelableArrayListExtra(CANSpecDO.EXTRA_CAN_COL, bs )
			.setAction( DNIeReader.ACTION_READ );
		else
	    	// Build.VERSION_CODES.KITKAT
	    	i = new Intent( this, com.dnieadmin.NFCOperationsEncKitKat.class );

		startActivityForResult(i, 1);
	}
	
	private void delete(CANSpecDO b)
	{
		cans.delete(b);
		refreshAdapter();
	}
	private void edit(final CANSpecDO b)
	{
		LayoutInflater factory = LayoutInflater.from(myContext);
        final View canEntryView = factory.inflate(R.layout.can_entry, null);
        ad = new AlertDialog.Builder(myContext).create();
	    ad.setCancelable(false);
	    ad.setIcon(R.drawable.alert_dialog_icon);
	    ad.setView(canEntryView);
	    ad.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.psswd_dialog_ok), new DialogInterface.OnClickListener() 
	    {
	        public void onClick(DialogInterface dialog, int which) 
	        {
	        	EditText text = (EditText) ad.findViewById(R.id.can_editbox);
	        	CANSpecDO can = new CANSpecDO(text.getText().toString(), "", "");
	        	cans.delete(b);
				cans.save(can);
				refreshAdapter(); 	
	        }
	    });
	    ad.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.psswd_dialog_cancel), new DialogInterface.OnClickListener() 
	    {
	        public void onClick(DialogInterface dialog, int which) 
	        {
	        	
	        }
	    });
		ad.show();
		((EditText)ad.findViewById(R.id.can_editbox)).setText(b.getCanNumber());
	}
	
	private void toastIt( String msg )
	{
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		selectedBac = listA.getItem(position);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Opciones");
		builder.setItems( ACTION_LABELS, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	switch (item) {
				case 0: read(selectedBac); break;
				case 1: edit(selectedBac); break;
				case 2: delete(selectedBac); break;

				default:
					break;
				}
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		// Si la pulsación es corta, directamente leemos con ese CAN
		selectedBac = listA.getItem(position);
		read(selectedBac);
	}
	
	private void refreshAdapter() {
		m_adapter.clear();
		listA.clear();
		for(CANSpecDO b : cans.getAll())
			listA.add(b);
		
		int idx=0;
        while(idx < listA.getCount())
        {
        	CANSpecDO canItem = listA.getItem(idx);
        	mrtdItems.add(new MrtdItem(canItem.getCanNumber(), canItem.getUserName(), null));
	        idx++;
        }
	}
	
	
	public class SampleAdapter extends ArrayAdapter<MrtdItem> {
		private ArrayList<MrtdItem> items;
		private LayoutInflater vi;
		
		public SampleAdapter(Context context, ArrayList<MrtdItem> items) {
			super(context,0, items);
			this.items = items;
			vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			final MrtdItem i = items.get(position);
			if (i != null) 
			{
				v = vi.inflate(R.layout.list_mrtd_row, null);
				final TextView can 		= (TextView)v.findViewById(R.id.row_title);
				final TextView name 	= (TextView)v.findViewById(R.id.row_name);
				final TextView nif 		= (TextView)v.findViewById(R.id.row_nif);

				if(name != null) 
				{
					name.setText(i.strName);
					name.setTypeface(fontType);
				}
				if(nif != null)
				{
					nif.setText(i.strNif);
					nif.setTypeface(fontType);
				}
				if(can != null)
				{
					can.setText(i.strCan);
					can.setTypeface(fontType);
				}

				Button deleteImageView = (Button)  v.findViewById(R.id.Btn_DESTROYENTRY);
		    	deleteImageView.setOnClickListener(new OnClickListener() {
		    		public void onClick(View v) {
		    			RelativeLayout vwParentRow = (RelativeLayout)v.getParent();				    	
		    			int position = listW.getPositionForView(vwParentRow);
		    			
		    			// Borramos la entrada y refrescamos el listado
		    			selectedBac = listA.getItem(position);
		    			delete(selectedBac);
		    		}
	    		});
			}
			return v;
		}
	}
	
	// Clase que define una tarjeta MRTD
  	private class MrtdItem {
  		public final String strCan;
  		public final String strName;
  		public final String strNif;
  		private final int iconRes;
  		
  		public MrtdItem(String strCan, String strName, String strNif) 
  		{
  			this.iconRes = 0;
  			this.strCan  = strCan;
  			this.strName = strName;
  			this.strNif  = strNif;
  		}
  	}
}
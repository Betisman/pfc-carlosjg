package com.dnieadmin;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class HelpActivity extends DialogFragment {
	    
	private View view = null;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment		
		view = inflater.inflate(R.layout.activity_help, container, false);
	       
		// Obtenemos el tipo de letra
		Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NeutraText-LightAlt.otf");
       	
		// Actualizamos las fuentes
		TextView txt = (TextView) view.findViewById(R.id.texto_00);
		txt.setTypeface(font);		
		txt = (TextView) view.findViewById(R.id.texto_01);
		txt.setTypeface(font);		
		txt = (TextView) view.findViewById(R.id.texto_02);
		txt.setTypeface(font);
		
		// Asignamos el códifo al botón de cierre
		view.findViewById(R.id.help_ok_button).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
		
        return view;
    }

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(),android.R.style.Theme_Translucent_NoTitleBar);
        view = getActivity().getLayoutInflater().inflate(R.layout.activity_help, null);

        final Drawable d = new ColorDrawable(Color.BLACK);
        d.setAlpha(0x10);
        dialog.getWindow().setBackgroundDrawable(d);
        dialog.getWindow().setContentView(view);
        
        return dialog;
    }
}
package com.dnieadmin.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.dnieadmin.R;

import es.gob.jmulticard.ui.passwordcallback.CancelledOperationException;
import es.gob.jmulticard.ui.passwordcallback.DialogUIHandler;

public class MyPasswordDialog implements DialogUIHandler {

    static private AlertDialog.Builder alertDialogBuilder;
    static private Context myContext;
    private final Activity activity;

    /**
     * Flag que indica si se cachea el PIN.
     */
    private final boolean cachePIN;

    /**
     * El password introducido. Si está activado el cacheo se reutilizará.
     */
    private char[] password = null;

    public MyPasswordDialog(final Context context, final boolean cachePIN) {

        // Guardamos el contexto para poder mostrar el diálogo
        myContext = context;
        activity = ((Activity) context);
        this.cachePIN = cachePIN;

        // Cuadro de diálogo para confirmación de firmas
        alertDialogBuilder = new AlertDialog.Builder(myContext);
        alertDialogBuilder.setIcon(R.drawable.alert_dialog_icon);
    }

    @Override
    public int showConfirmDialog(String message) {
        return doShowConfirmDialog(message);
    }

    public int doShowConfirmDialog(String message) {
        final AlertDialog.Builder dialog 	= new AlertDialog.Builder(activity);
        final MyPasswordDialog instance 	= this;
        final StringBuilder resultBuilder 	= new StringBuilder();
        resultBuilder.append(message);

        synchronized (instance)
        {
            activity.runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    try {
                        dialog.setTitle("Proceso de firma con el DNI electrónico");
                        dialog.setMessage(resultBuilder);
                        dialog.setPositiveButton(R.string.psswd_dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                synchronized (instance) {
                                    resultBuilder.delete(0, resultBuilder.length());
                                    resultBuilder.append("0");
                                    instance.notifyAll();
                                }
                            }
                        });
                        dialog.setNegativeButton(R.string.psswd_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                synchronized (instance) {
                                    resultBuilder.delete(0, resultBuilder.length());
                                    resultBuilder.append("1");
                                    instance.notifyAll();
                                }
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.create().show();
                    } catch (CancelledOperationException ex) {
                        Log.e("MyPasswordFragment", "Excepción en diálogo de confirmación" + ex.getMessage());
                    } catch (Error err) {
                        Log.e("MyPasswordFragment", "Error en diálogo de confirmación" + err.getMessage());
                    }
                }
            });
            try
            {
                instance.wait();
                return Integer.parseInt(resultBuilder.toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception ex) {
                throw new CancelledOperationException();
            }
        }
    }

    @SuppressLint("InflateParams")
    private char[] doShowPasswordDialog(final int retries) {
        final AlertDialog.Builder dialog 	= new AlertDialog.Builder(activity);
        final LayoutInflater inflater 		= activity.getLayoutInflater();
        final StringBuilder passwordBuilder = new StringBuilder();
        final MyPasswordDialog instance 	= this;
        dialog.setMessage(getTriesMessage(retries));

        synchronized (instance)
        {
            activity.runOnUiThread( new Runnable() {

                @Override
                public void run() {
                    try {
                        final View passwordView = inflater.inflate(R.layout.passwordentry, null);
                        final EditText passwordText = (EditText) passwordView.findViewById(R.id.password_edit);
                        final CheckBox passwordShow = (CheckBox) passwordView.findViewById(R.id.checkBoxShow);

                        // Ajustamos el tipo de letra
                        Typeface type = Typeface.createFromAsset(myContext.getAssets(),"fonts/NeutraText-LightAlt.otf");
                        TextView myText = (TextView)passwordView.findViewById(R.id.password_view);
                        myText.setTypeface(type);
                        passwordText.setTypeface(type);
                        passwordShow.setTypeface(type);

                        dialog.setPositiveButton(R.string.psswd_dialog_ok, new DialogInterface.OnClickListener() {

                            /**
                             * @param dialog El diálogo que genera el evento.
                             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
                             *      int)
                             */
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                synchronized (instance) {
                                    passwordBuilder.delete(0, passwordBuilder.length());
                                    passwordBuilder.append(passwordText.getText().toString());
                                    instance.notifyAll();
                                }
                            }
                        });
                        dialog.setNegativeButton(R.string.psswd_dialog_cancel, new DialogInterface.OnClickListener() {

                            /**
                             * @param dialog El diálogo que genera el evento.
                             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface,
                             *      int)
                             */
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                synchronized (instance) {
                                    passwordBuilder.delete(0, passwordBuilder.length());
                                    instance.notifyAll();
                                }
                            }
                        });
                        passwordShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                                if (isChecked) {
                                    passwordText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    passwordShow.setText(activity.getString(R.string.psswd_dialog_show));
                                } else {
                                    passwordText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passwordShow.setText(activity.getString(R.string.psswd_dialog_hide));
                                }
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.setView(passwordView);
                        dialog.create().show();
                    } catch (Exception ex) {
                        Log.e("MyPasswordFragment", "Excepción en diálogo de contraseña" + ex.getMessage());
                    } catch (Error err) {
                        Log.e("MyPasswordFragment", "Error en diálogo de contraseña" + err.getMessage());
                    }
                }
            });
            try
            {
                instance.wait();
                return passwordBuilder.toString().toCharArray();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public char[] showPasswordDialog(final int retries) {
        char[] returning;

        if (retries < 0 && cachePIN && password != null && password.length > 0)
            returning = password.clone();
        else
            returning = doShowPasswordDialog(retries);

        if (cachePIN && returning != null && returning.length > 0)
            password = returning.clone();
        else
            return null;

        return returning;
    }

    @Override
    public Object getAndroidContext() {
        // TODO Auto-generated method stub

        return myContext;
    }//*/

    /**
     * Genera el mensaje de reintentos del diálogo de contraseña.
     *
     * @param retries El número de reintentos pendientes. Si es negativo, se considera que no se conocen los intentos.
     * @return El mensaje a mostrar.
     */
    private String getTriesMessage(final int retries) {
        String text;
        if (retries < 0) {
            text = activity.getString(R.string.dni_password_msg);
        } else if (retries == 1) {
            text = activity.getString(R.string.dni_password_msg_1_try);
        } else {
            text = "Introduzca PIN. Quedan " +retries+" reintentos.";
        }
        return text;
    }
}
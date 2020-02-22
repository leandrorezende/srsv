package br.com.srsv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import br.com.srsv.api.UsuarioAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.utils.Auxiliar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class A_RecuperarSenha extends Activity {

	private EditText inputEmail;
	private ProgressDialog pDialog;

	private final BroadcastReceiver mensagemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recupera_senha);

		inputEmail = (EditText) findViewById(R.id.email);
		pDialog = new ProgressDialog(this);
		
		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));
	}

	public void cadastrar(View v) {
		Intent i = new Intent(A_RecuperarSenha.this, A_Cadastrar.class);
		startActivity(i);
		finish();
	}

	public void cancelar(View v) {
		finish();
	}

	public void recuperarSenha(View v) {
		String email = inputEmail.getText().toString().trim();
		Pattern pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		Matcher matcher = pattern.matcher(email);
		if (email.length() == 0) {
			inputEmail.requestFocus();
			inputEmail.setError("Campo obrigatório!");
		} else if (!matcher.matches()) {
			inputEmail.requestFocus();
			inputEmail.setError("Campo email inválido!");				
		} else  {
			if (Auxiliar.isOnline(A_RecuperarSenha.this)) {
				pDialog.setMessage("Enviando...");
				pDialog.show();
				RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
						AppConfig.ENDPOINT).build();

				UsuarioAPI api = adapter.create(UsuarioAPI.class);
				api.recuperarSenha(email, new Callback<String>() {
					@Override
					public void failure(RetrofitError erro) {
						pDialog.dismiss();
						if (erro.getResponse() != null) {
							Toast.makeText(A_RecuperarSenha.this,
									erro.getResponse().getReason(),
									Toast.LENGTH_LONG).show();
						} else {
							if (Auxiliar.isOnline(A_RecuperarSenha.this)){
								Toast.makeText(A_RecuperarSenha.this,
										"Problemas nos servidores do SRSV",
										Toast.LENGTH_LONG).show();
							}
							else 
								Toast.makeText(A_RecuperarSenha.this, "Conexão com a Internet não está disponível",
										Toast.LENGTH_LONG).show();								
						}	
					}

					@Override
					public void success(String msg, Response resp) {
						pDialog.dismiss();
						Toast.makeText(A_RecuperarSenha.this, msg,
								Toast.LENGTH_LONG).show();
						finish();
					}
				});
			} else {
				pDialog.dismiss();
				Toast.makeText(this, "Conexão com a Internet não esta disponível",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}

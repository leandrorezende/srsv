package br.com.srsv;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import br.com.srsv.api.UsuarioAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.utils.Auxiliar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class A_TrocarSenha extends Activity {
	private EditText inputUsuario;
	private EditText inputSenhaAtual;
	private EditText inputNovaSenha;
	private EditText inputRepetirSenha;

	private final BroadcastReceiver mensagemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getExtras().getString("msg");
			if (msg != null) {
				AppConfig.MSG_RECEIVER = msg;
				finish();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trocar_senha);

		inputUsuario = (EditText) findViewById(R.id.usuario);
		inputSenhaAtual = (EditText) findViewById(R.id.senha_atual);
		inputNovaSenha = (EditText) findViewById(R.id.nova_senha);
		inputRepetirSenha = (EditText) findViewById(R.id.repetir_senha);
		
		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));
		String msg = getIntent().getStringExtra("msg");
		if (msg != null)
			finish();
	}

	public void cancelar(View v) {
		finish();
	}
	
	public void fazerLogout(View v) {
		Auxiliar.logoutUser(A_TrocarSenha.this);
		finish();
	}

	public void trocar(View v) {
		String usuario = inputUsuario.getText().toString().trim();
		String senhaAtual = inputSenhaAtual.getText().toString().trim();
		String novaSenha = inputNovaSenha.getText().toString().trim();
		String repetirSenha = inputRepetirSenha.getText().toString().trim();

		if (usuario.length() == 0) {
			inputUsuario.requestFocus();
			inputUsuario.setError("Campo obrigatório");
			if (senhaAtual.length() == 0)
				inputSenhaAtual.setError("Campo obrigatório!");
			if (novaSenha.length() == 0)
				inputNovaSenha.setError("Campo obrigatório!");
			if (repetirSenha.length() == 0)
				inputRepetirSenha.setError("Campo obrigatório!");
		} else if (senhaAtual.length() == 0) {
			inputSenhaAtual.requestFocus();
			inputSenhaAtual.setError("Campo obrigatório!");
			if (novaSenha.length() == 0)
				inputNovaSenha.setError("Campo obrigatório!");
			if (repetirSenha.length() == 0)
				inputRepetirSenha.setError("Campo obrigatório!");
		} else if (novaSenha.length() == 0) {
			inputNovaSenha.requestFocus();
			inputNovaSenha.setError("Campo obrigatório!");
			if (repetirSenha.length() == 0)
				inputRepetirSenha.setError("Campo obrigatório!");
		} else if (repetirSenha.length() == 0) {
			inputRepetirSenha.requestFocus();
			inputRepetirSenha.setError("Campo obrigatório!");
		} else if (!repetirSenha.equals(novaSenha)) {
			inputRepetirSenha.requestFocus();
			inputRepetirSenha.setError("Senha não confere!");
		}else {
			if (Auxiliar.isOnline(A_TrocarSenha.this)) {
				RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
						AppConfig.ENDPOINT).build();

				UsuarioAPI api = adapter.create(UsuarioAPI.class);
				api.trocarSenha(AppConfig.API_KEY, usuario, senhaAtual, novaSenha, new Callback<String>() {
					@Override
					public void failure(RetrofitError erro) {
						if (erro.getResponse() != null) {
							Toast.makeText(A_TrocarSenha.this,
									erro.getResponse().getReason(),
									Toast.LENGTH_LONG).show();
						} else {
							if (Auxiliar.isOnline(A_TrocarSenha.this)){
								Toast.makeText(A_TrocarSenha.this,
										"Problemas nos servidores do SRSV",
										Toast.LENGTH_LONG).show();
							}
							else 
								Toast.makeText(A_TrocarSenha.this, "Conexão com a Internet não está disponível",
										Toast.LENGTH_LONG).show();								
						}	
					}
					
					@Override
					public void success(String msg, Response resp) {
						Toast.makeText(A_TrocarSenha.this, msg,
								Toast.LENGTH_LONG).show();
						if(msg.equals("Senha alterada com sucesso!"))
							finish();
					}					
				});
			} else {
				Toast.makeText(A_TrocarSenha.this,
						"Conexão com a Internet não esta disponível",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}

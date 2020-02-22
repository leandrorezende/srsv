package br.com.srsv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
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
import br.com.srsv.api.UsuarioAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.bean.BUsuario;
import br.com.srsv.utils.Auxiliar;

public class A_Cadastrar extends Activity {

	private EditText inputNome;
	private EditText inputUsuario;
	private EditText inputSenha;
	private EditText inputEmail;
	private ProgressDialog pDialog;

	private final BroadcastReceiver mensagemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastrar);
		inputNome = (EditText) findViewById(R.id.nome);
		inputUsuario = (EditText) findViewById(R.id.usuario);
		inputSenha = (EditText) findViewById(R.id.senha);
		inputEmail = (EditText) findViewById(R.id.email);
		pDialog = new ProgressDialog(this);

		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));
	}

	public void login(View view) {
		finish();
	}

	public void cadastrar(View view) {
		String nome = inputNome.getText().toString().trim();
		String usuario = inputUsuario.getText().toString().trim();
		String senha = inputSenha.getText().toString().trim();
		String email = inputEmail.getText().toString().trim();
		
		Pattern pattern = Pattern
				.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
						+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
		Matcher matcher = pattern.matcher(email);

		if (nome.length() == 0) {
			inputNome.requestFocus();
			inputNome.setError("Campo obrigatório!");
			if (usuario.length() == 0)
				inputUsuario.setError("Campo obrigatório!");
			if (senha.length() == 0)
				inputSenha.setError("Campo obrigatório!");
			if (email.length() == 0)
				inputEmail.setError("Campo obrigatório!");
		} else if (usuario.length() == 0) {
			inputUsuario.requestFocus();
			inputUsuario.setError("Campo obrigatório");
			if (senha.length() == 0)
				inputSenha.setError("Campo obrigatório!");
			if (email.length() == 0)
				inputEmail.setError("Campo obrigatório!");
		} else if (senha.length() == 0) {
			inputSenha.requestFocus();
			inputSenha.setError("Campo obrigatório!");
			if (email.length() == 0)
				inputEmail.setError("Campo obrigatório!");
		} else if (!matcher.matches()) {
			inputEmail.requestFocus();
			inputEmail.setError("Campo email inválido!");
		} else {
			if (Auxiliar.isOnline(A_Cadastrar.this)) {
				pDialog.setMessage("Cadastrando...");
				pDialog.show();

				BUsuario novo_usuario = new BUsuario();
				novo_usuario.setNome(nome);
				novo_usuario.setUsuario(usuario);
				novo_usuario.setSenha(senha);
				novo_usuario.setEmail(email);
				RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
						AppConfig.ENDPOINT).build();

				UsuarioAPI api = adapter.create(UsuarioAPI.class);
				api.cadastrarUsuario(novo_usuario, new Callback<String>() {

					@Override
					public void failure(RetrofitError erro) {
						pDialog.dismiss();
						if (erro.getResponse() != null) {
							Toast.makeText(A_Cadastrar.this,
									erro.getResponse().getReason(),
									Toast.LENGTH_LONG).show();
						} else {
							if (Auxiliar.isOnline(A_Cadastrar.this)){
								Toast.makeText(A_Cadastrar.this,
										"Problemas nos servidores do SRSV",
										Toast.LENGTH_LONG).show();
							}
							else 
								Toast.makeText(A_Cadastrar.this, "Conexão com a Internet não está disponível",
										Toast.LENGTH_LONG).show();								
						}	
					}

					@Override
					public void success(String msg, Response resp) {
						pDialog.dismiss();
						Toast.makeText(A_Cadastrar.this, msg, Toast.LENGTH_LONG)
								.show();
						if (msg.equals("Usuário cadastrado com sucesso")) 
							finish();
					}
				});
			} else {
				pDialog.dismiss();
				Toast.makeText(this,
						"Conexão com a Internet não está disponível",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}

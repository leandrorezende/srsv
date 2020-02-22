package br.com.srsv;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import br.com.srsv.api.UsuarioAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.bean.BUsuario;
import br.com.srsv.helper.SQLiteHandler;
import br.com.srsv.helper.SessionManager;
import br.com.srsv.utils.Auxiliar;
import br.com.srsv.utils.CustomBuilder;

public class A_Login extends Activity {

	private EditText inputUsuario;
	private EditText inputSenha;
	private CheckBox checkBoxRegistro;
	private ProgressDialog pDialog;
	private String usuario;
	private String senha;

	private final BroadcastReceiver mensagemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getExtras().getString("msg");
			if (msg != null){
				if(msg.length() < 100)
					mensagem_servidor(msg);
				else 
					Toast.makeText(context, "Aplicativo Registrado com sucesso.", Toast.LENGTH_LONG).show();
			}
		}
	};

	private void mensagem_servidor(String msg) {
		AppConfig.MSG_RECEIVER = msg;
		String[] parts = msg.split("»");
		final String placa = parts[0];
		ContextThemeWrapper ctw = new ContextThemeWrapper(A_Login.this,
				R.style.MyTheme);
		CustomBuilder builder = new CustomBuilder(ctw, true);
		builder.setTitle("Alerta de segurança")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(
						"O veículo cuja a placa é "
								+ placa
								+ ", está associado a esse dispositivo, "
								+ "esta com sua integridade de segurança está ameaçada. Sugerimos tomar as atitudes "
								+ "para evitar danos ao mesmo.")
				.setCancelable(false)
				.setPositiveButton("Fechar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		builder.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		GCM.registrar(getApplicationContext(),
				AppConfig.PROJECT_NUMBER);
		
		inputUsuario = (EditText) findViewById(R.id.usuario);
		inputSenha = (EditText) findViewById(R.id.senha);
		checkBoxRegistro = (CheckBox) findViewById(R.id.checkBoxRegistro);

		pDialog = new ProgressDialog(this);
		Auxiliar.session = new SessionManager(getApplicationContext());

		if (Auxiliar.session.isLoggedIn()) {
			String msg = getIntent().getStringExtra("msg");
			Intent intent = new Intent(A_Login.this, A_Inicial.class);
			if (msg != null){
				AppConfig.MSG_RECEIVER = msg;
			}
			startActivity(intent);
			finish();
		}
		
		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));
		String msg = getIntent().getStringExtra("msg");
		if (msg != null) 
			AppConfig.MSG_RECEIVER = msg;		
	}

	public void recuperarSenha(View v) {
		Intent i = new Intent(A_Login.this, A_RecuperarSenha.class);
		startActivity(i);
	}

	public void cadastrar(View v) {
		Intent i = new Intent(A_Login.this, A_Cadastrar.class);
		startActivity(i);
	}

	public void login(View view) {
		usuario = inputUsuario.getText().toString().trim();
		senha = inputSenha.getText().toString().trim();

		if (usuario.length() == 0) {
			inputUsuario.requestFocus();
			inputUsuario.setError("Campo obrigatório!");
			if (senha.length() == 0) {
				inputSenha.setError("Campo obrigatório!");
			}
		} else if (senha.length() == 0) {
			inputSenha.requestFocus();
			inputSenha.setError("Campo obrigatório!");
		} else
			login();
	}

	private void login() {		
		if (Auxiliar.isOnline(A_Login.this)) {
			pDialog.setMessage("Logando...");
			pDialog.show();
			
			final String id_registro;
			id_registro = GCM.registrar(getApplicationContext(),
					AppConfig.PROJECT_NUMBER);
			
			if(!id_registro.equals("")){
				byte[] loginBytes = (usuario + ":" + senha).getBytes();
				final StringBuilder loginBuilder = new StringBuilder().append(
						"Basic ").append(
						Base64.encodeToString(loginBytes, Base64.DEFAULT));
				RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
						AppConfig.ENDPOINT).build();

				UsuarioAPI api = adapter.create(UsuarioAPI.class);
				api.login(loginBuilder.toString(), id_registro,
						checkBoxRegistro.isChecked(), new Callback<BUsuario>() {
							@Override
							public void failure(RetrofitError erro) {
								pDialog.dismiss();
								if (erro.getResponse() != null) {
									Toast.makeText(A_Login.this,
											erro.getResponse().getReason(),
											Toast.LENGTH_LONG).show();
								} else {
									if (Auxiliar.isOnline(A_Login.this)){
										Toast.makeText(A_Login.this,
												"Problemas nos servidores do SRSV",
												Toast.LENGTH_LONG).show();
									}
									else 
										Toast.makeText(A_Login.this, "Conexão com a Internet não está disponível",
												Toast.LENGTH_LONG).show();								
								}
							}

							@Override
							public void success(BUsuario usuario, Response resp) {
								pDialog.dismiss();
								if (usuario != null) {
									Auxiliar.session.setLogin(true);
									SQLiteHandler db = new SQLiteHandler(
											getApplicationContext());
									db.addUser(usuario.getId_usuario(),
											usuario.getNome(),
											usuario.getApi_key(), id_registro,
											usuario.getCreated_at());

									Intent intent = new Intent(A_Login.this,
											A_Inicial.class);

									if (AppConfig.MSG_RECEIVER != null) {
										intent.putExtra("msg",
												AppConfig.MSG_RECEIVER);
										AppConfig.MSG_RECEIVER = null;
									}

									startActivity(intent);
									finish();
								} else {
									Toast.makeText(A_Login.this,
											"Usuário e/ou Senha inválidos",
											Toast.LENGTH_LONG).show();
								}
							}
						});
			} else {
				pDialog.dismiss();
				Toast.makeText(this, "Registrando aplicativo, aguarde um momento.",
						Toast.LENGTH_SHORT).show();
			}
			
		} else {
			pDialog.dismiss();
			Toast.makeText(this, "Conexão com a Internet não está disponível",
					Toast.LENGTH_SHORT).show();
		}
	}
}

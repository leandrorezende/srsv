package br.com.srsv;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import br.com.srsv.api.UsuarioAPI;
import br.com.srsv.api.VeiculoAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.utils.CustomBuilder;
import br.com.srsv.bean.BVeiculo;
import br.com.srsv.helper.SQLiteHandler;
import br.com.srsv.helper.SessionManager;
import br.com.srsv.utils.Auxiliar;

public class A_Inicial extends Activity {

	private ListView listView;
	private ProgressBar pb;
	private Button btn_recarregar;
	private TextView lista_vazia;
	private boolean flag_exibe;

	private final BroadcastReceiver mensagemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getExtras().getString("msg");
			if (msg != null && flag_exibe == true)
				mensagem_servidor(msg);
		}
	};

	private void mensagem_servidor(String msg) {
		String[] parts = msg.split("»");
		final String placa = parts[0];
		final String id_veiculo = parts[1];
		ContextThemeWrapper ctw = new ContextThemeWrapper(A_Inicial.this,
				R.style.MyTheme);
		CustomBuilder builder = new CustomBuilder(ctw, true);
		builder.setTitle("Alerta de segurança")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(
						"O veículo de placa "
								+ placa
								+ ", esta associado a esse dispositivo e "
								+ "está com sua integridade de segurança ameaçada. Sugerimos tomar alguma ação "
								+ "para evitar danos ao mesmo.")
				.setCancelable(false)
				.setPositiveButton("Fechar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								flag_exibe = false;
								Intent it = new Intent(A_Inicial.this,
										A_Acoes.class);
								it.putExtra("placa", placa);
								it.putExtra("id_veiculo", id_veiculo);
								startActivity(it);
							}
						});
		builder.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inicial);

		if (Auxiliar.db == null) {
			Auxiliar.db = new SQLiteHandler(getApplicationContext());
		}

		if (Auxiliar.session == null) {
			Auxiliar.session = new SessionManager(getApplicationContext());
		}

		if (!Auxiliar.session.isLoggedIn()) {
			finish();
		}

		HashMap<String, String> user = Auxiliar.db.getUserDetails();
		AppConfig.API_KEY = user.get("api_key");
		AppConfig.ID_USUARIO = Integer.parseInt(user.get("id"));
		AppConfig.REGISTRO = user.get("registro");

		listView = (ListView) findViewById(R.id.listView);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		btn_recarregar = (Button) findViewById(R.id.btnRecarregar);
		lista_vazia = (TextView) findViewById(R.id.lista_vazia);
		lista_vazia.setVisibility(View.INVISIBLE);

		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));

		flag_exibe = true;
		String msg = getIntent().getStringExtra("msg");
		if (msg != null)
			mensagem_servidor(msg);
	}

	private Context getContext() {
		return getApplicationContext();
	}

	@Override
	protected void onResume() {
		if (AppConfig.MSG_RECEIVER != null) {
			mensagem_servidor(AppConfig.MSG_RECEIVER);
			AppConfig.MSG_RECEIVER = null;
		}
		this.flag_exibe = true;
		atualiza_view();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		this.flag_exibe = false;
		super.onPause();
	}

	public void reload(View v) {
		atualiza_view();
	}

	public void fazerLogout(View v) {
		Auxiliar.logoutUser(A_Inicial.this);
		finish();
	}

	public void atualiza_view() {
		listView.setAdapter(null);
		btn_recarregar.setVisibility(View.INVISIBLE);
		pb.setVisibility(View.VISIBLE);
		lista_vazia.setVisibility(View.INVISIBLE);
		if (Auxiliar.isOnline(A_Inicial.this)) {
			RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
					AppConfig.ENDPOINT).build();
			VeiculoAPI api = adapter.create(VeiculoAPI.class);
			api.listarVeiculo(AppConfig.API_KEY, null,
					new Callback<List<BVeiculo>>() {
						@Override
						public void failure(RetrofitError erro) {
							pb.setVisibility(View.INVISIBLE);
							btn_recarregar.setVisibility(View.VISIBLE);
							if (erro.getResponse() != null) {
								Toast.makeText(A_Inicial.this,
										erro.getResponse().getReason(),
										Toast.LENGTH_LONG).show();
							} else {
								if (Auxiliar.isOnline(A_Inicial.this)) {
									Toast.makeText(A_Inicial.this,
											"Problemas nos servidores do SRSV",
											Toast.LENGTH_LONG).show();
								} else
									Toast.makeText(
											A_Inicial.this,
											"Conexão com a Internet não está disponível",
											Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void success(List<BVeiculo> listVeiculos,
								Response resp) {
							pb.setVisibility(View.INVISIBLE);
							btn_recarregar.setVisibility(View.INVISIBLE);
							if (listVeiculos != null) {
								if (listVeiculos.isEmpty())
									lista_vazia.setVisibility(View.VISIBLE);
								else
									lista_vazia.setVisibility(View.INVISIBLE);
								Adapter_Veiculo adapter = new Adapter_Veiculo(
										A_Inicial.this, listVeiculos);
								listView.setAdapter(adapter);
							}
						}
					});
		} else {
			Toast.makeText(this, "Conexão com a Internet não está disponível",
					Toast.LENGTH_SHORT).show();
			btn_recarregar.setVisibility(View.VISIBLE);
			pb.setVisibility(View.INVISIBLE);
		}
	}

	public void configuracoes(View v) {
		if (Auxiliar.isOnline(A_Inicial.this)) {
			final String[] option = new String[] { "Trocar Senha",
					"Retirar associações da conta",
					"Retirar associações do dispositivo" };
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.select_dialog_item, option);

			ContextThemeWrapper ctw = new ContextThemeWrapper(A_Inicial.this,
					R.style.MyTheme);
			CustomBuilder builder = new CustomBuilder(ctw, false);
			builder.setTitle("Opções de Configuração")
					.setIcon(R.drawable.carro_icone_alert)
					.setCancelable(false)
					.setAdapter(adapter, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							if (item == 0) {
								Intent i = new Intent(A_Inicial.this,
										A_TrocarSenha.class);
								startActivity(i);
							} else if (item == 1)
								retirarAssociacoesConta();
							else
								retirarAssociacoesDispositivo();
						}
					})
					.setPositiveButton("Fechar",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
			builder.show();
		} else {
			Toast.makeText(A_Inicial.this,
					"Conexão com a Internet não está disponível",
					Toast.LENGTH_LONG).show();
		}
	}

	private void retirarAssociacoesDispositivo() {
		ContextThemeWrapper ctw = new ContextThemeWrapper(A_Inicial.this,
				R.style.MyTheme);
		CustomBuilder builder = new CustomBuilder(ctw, true);
		builder.setTitle("Mensagem de aviso")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(
						"Essa ação implica em cancelar o recebimento de notificações de todas contas "
								+ "associadas a este dispositivo, do qual esta configurado para receber atualizações "
								+ "de dados dessas contas. Deseja continuar com o procedimento?")
				.setNegativeButton("Cancelar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton("Confirmar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (Auxiliar.isOnline(A_Inicial.this)) {
									RestAdapter adapter = new RestAdapter.Builder()
											.setEndpoint(AppConfig.ENDPOINT)
											.build();
									UsuarioAPI api = adapter
											.create(UsuarioAPI.class);
									api.removerAssocicaoDispositivo(
											AppConfig.API_KEY,
											AppConfig.REGISTRO,
											new Callback<String>() {
												@Override
												public void failure(
														RetrofitError erro) {
													if (erro.getResponse() != null) {
														Toast.makeText(
																A_Inicial.this,
																erro.getResponse()
																		.getReason(),
																Toast.LENGTH_LONG)
																.show();
													} else {
														if (Auxiliar
																.isOnline(A_Inicial.this)) {
															Toast.makeText(
																	A_Inicial.this,
																	"Problemas nos servidores do SRSV",
																	Toast.LENGTH_LONG)
																	.show();
														} else
															Toast.makeText(
																	A_Inicial.this,
																	"Conexão com a Internet não está disponível",
																	Toast.LENGTH_LONG)
																	.show();
													}
												}

												@Override
												public void success(String msg,
														Response resp) {
													Toast.makeText(
															A_Inicial.this,
															"Associoações foram desfeitas.",
															Toast.LENGTH_LONG)
															.show();
												}
											});
								} else {
									Toast.makeText(
											A_Inicial.this,
											"Conexão com a Internet não está disponível",
											Toast.LENGTH_SHORT).show();
								}
							}
						});
		builder.show();
	}

	public void retirarAssociacoesConta() {
		ContextThemeWrapper ctw = new ContextThemeWrapper(A_Inicial.this,
				R.style.MyTheme);
		CustomBuilder builder = new CustomBuilder(ctw, true);
		builder.setTitle("Mensagem de aviso")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(
						"Essa ação implica em cancelar o envio de notificações a todos os "
								+ "dispositivos dos quais estam configurados para receber atualizações "
								+ "sobre os dados da sua conta. Deseja continuar com o procedimento?")
				.setNegativeButton("Cancelar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton("Confirmar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Auxiliar.retirarAssociacoes(A_Inicial.this,
										true);
								Toast.makeText(A_Inicial.this,
										"Associoações foram desfeitas.",
										Toast.LENGTH_LONG).show();
							}
						});
		builder.show();
	}

	public void cadastrarVeiculo(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(A_Inicial.this);
		LayoutInflater inflater = getLayoutInflater();

		builder.setIcon(R.drawable.carro_icone);
		builder.setTitle("Cadastro de novo veículo");
		builder.setView(inflater.inflate(R.layout.alert_form_veiculo, null))
				.setNegativeButton("Confirmar",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						})
				.setPositiveButton("Cancelar",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

							}
						});
		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				atualiza_view();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		Button botaoConfirmar = alertDialog
				.getButton(DialogInterface.BUTTON_NEGATIVE);
		botaoConfirmar.setOnClickListener(new CustomListener(alertDialog,
				A_Inicial.this, 0));
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mensagemReceiver);
		GCM.onDestroy(getContext());
		super.onDestroy();
	}
}

class CustomListener implements View.OnClickListener {
	private final Dialog dialog;
	Activity context;
	int id_veiculo;

	public CustomListener(Dialog dialog, Activity context, int id_veiculo) {
		this.dialog = dialog;
		this.context = context;
		this.id_veiculo = id_veiculo;
	}

	@Override
	public void onClick(View v) {
		Dialog f = (Dialog) dialog;
		EditText inputPlaca = (EditText) f.findViewById(R.id.placa);
		EditText inputNumDispositivo = (EditText) f
				.findViewById(R.id.num_dispositivo);
		String placa = inputPlaca.getText().toString().toUpperCase().trim();
		String num_dispositivo = inputNumDispositivo.getText().toString()
				.toUpperCase().trim();

		Pattern pattern = Pattern.compile("[a-zA-Z]{3,3}-\\d{4,4}");
		Matcher matcher = pattern.matcher(placa);

		if (!matcher.matches()) {
			inputPlaca.requestFocus();
			inputPlaca.setError("Campo inválido!");
			if (num_dispositivo.length() == 0)
				inputNumDispositivo.setError("Campo obrigatório!");
		} else if (num_dispositivo.length() < 10) {
			inputNumDispositivo.requestFocus();
			inputNumDispositivo.setError("Deve conter 10 caracteres!");
		} else {
			if (Auxiliar.isOnline(context)) {
				BVeiculo veiculo = new BVeiculo();
				veiculo.setPlaca(placa);
				veiculo.setNum_dispositivo(num_dispositivo);

				RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
						AppConfig.ENDPOINT).build();
				VeiculoAPI api = adapter.create(VeiculoAPI.class);
				if (id_veiculo == 0) {
					api.cadastrarVeiculo(AppConfig.API_KEY, veiculo,
							new Callback<String>() {
								@Override
								public void failure(RetrofitError erro) {
									if (erro.getResponse() != null) {
										Toast.makeText(context,
												erro.getResponse().getReason(),
												Toast.LENGTH_LONG).show();
									} else {
										if (Auxiliar.isOnline(context)){
											Toast.makeText(context,
													"Problemas nos servidores do SRSV",
													Toast.LENGTH_LONG).show();
										}
										else 
											Toast.makeText(context, "Conexão com a Internet não está disponível",
													Toast.LENGTH_LONG).show();								
									}
								}

								@Override
								public void success(String msg, Response arg1) {
									Toast.makeText(context, msg,
											Toast.LENGTH_LONG).show();
									dialog.dismiss();
								}
							});
				} else {
					veiculo.setId_veiculo(id_veiculo);
					api.alterarVeiculo(AppConfig.API_KEY, veiculo,
							new Callback<String>() {

								@Override
								public void failure(RetrofitError erro) {
									if (erro.getResponse() != null) {
										Toast.makeText(context,
												erro.getResponse().getReason(),
												Toast.LENGTH_LONG).show();
									}  else {
										if (Auxiliar.isOnline(context)){
											Toast.makeText(context,
													"Problemas nos servidores do SRSV",
													Toast.LENGTH_LONG).show();
										}
										else 
											Toast.makeText(context, "Conexão com a Internet não está disponível",
													Toast.LENGTH_LONG).show();								
									}
								}

								@Override
								public void success(String msg, Response arg1) {
									Toast.makeText(context, msg,
											Toast.LENGTH_LONG).show();
									dialog.dismiss();
								}
							});
				}
			} else {
				Toast.makeText(context,
						"Conexão com a Internet não está disponível",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}

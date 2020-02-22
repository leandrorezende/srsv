package br.com.srsv;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import br.com.srsv.api.OcorrenciaAPI;
import br.com.srsv.api.VeiculoAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.bean.BVeiculo;
import br.com.srsv.utils.Auxiliar;

public class A_Acoes extends Activity {
	private ToggleButton toggle_presenca;
	private ToggleButton toggle_audio;
	private ToggleButton toggle_movimento;

	private ToggleButton toggle_ctrl_sistema;
	private ToggleButton toggle_ctrl_carro;
	private ToggleButton toggle_ctrl_alarme;

	private Button btnEditar;
	private Button btnExcluir;

	private BVeiculo veiculo;
	private TextView txt_placa;
	private String id_veiculo;

	private ListView listView;
	private Adapter_Notificacoes adapter_list;

	private final BroadcastReceiver mensagemReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getExtras().getString("msg");
			if (msg != null) {
				String[] parts = msg.split("»");
				if (parts[0].equals(txt_placa.getText()))
					atualiza_view(false);
				else {
					AppConfig.MSG_RECEIVER = msg;
					finish();
				}
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acoes);

		txt_placa = (TextView) findViewById(R.id.txt_placa);
		Intent it = getIntent();
		if (it != null) {
			txt_placa.setText(it.getStringExtra("placa"));
			id_veiculo = it.getStringExtra("id_veiculo");
		}
		toggle_presenca = (ToggleButton) findViewById(R.id.toggle_presenca);
		toggle_audio = (ToggleButton) findViewById(R.id.toggle_audio);
		toggle_movimento = (ToggleButton) findViewById(R.id.toggle_movimento);
		toggle_ctrl_sistema = (ToggleButton) findViewById(R.id.toggle_ctrl_sistema);
		toggle_ctrl_carro = (ToggleButton) findViewById(R.id.toggle_ctrl_carro);
		toggle_ctrl_alarme = (ToggleButton) findViewById(R.id.toggle_ctrl_alarme);
		btnEditar = (Button) findViewById(R.id.btn_editar);
		btnExcluir = (Button) findViewById(R.id.btn_excluir);
		
		toggle_ctrl_sistema.setEnabled(false);
		toggle_ctrl_carro.setEnabled(false);
		toggle_ctrl_alarme.setEnabled(false);
		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
		
		listView = (ListView) findViewById(R.id.listView);
		adapter_list = new Adapter_Notificacoes(A_Acoes.this);

		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));
	}

	@Override
	protected void onStart() {
		atualiza_view(false);
		super.onStart();
	}

	public void fazerLogout(View v) {
		Auxiliar.logoutUser(A_Acoes.this);
		finish();
	}

	public void atualiza_view(final boolean flag) {
		listView.setAdapter(null);
		if (Auxiliar.isOnline(A_Acoes.this)) {
			RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
					AppConfig.ENDPOINT).build();
			OcorrenciaAPI api = adapter.create(OcorrenciaAPI.class);
			api.listarOcorrenciaAcoes(AppConfig.API_KEY, id_veiculo,
					new Callback<BVeiculo>() {

						@Override
						public void failure(RetrofitError erro) {
							if (erro.getResponse() != null) {
								Toast.makeText(A_Acoes.this,
										erro.getResponse().getReason(),
										Toast.LENGTH_LONG).show();
							} else
								Toast.makeText(A_Acoes.this,
										"Problemas nos servidores do SRSV",
										Toast.LENGTH_LONG).show();
						}

						@Override
						public void success(BVeiculo ocorrenciaveiculo,
								Response resp) {
							if (ocorrenciaveiculo != null) {
								toggle_ctrl_sistema.setEnabled(true);
								toggle_ctrl_carro.setEnabled(true);
								toggle_ctrl_alarme.setEnabled(true);
								btnEditar.setEnabled(true);
								btnExcluir.setEnabled(true);
								
								veiculo = ocorrenciaveiculo;
								txt_placa.setText(veiculo.getPlaca());
								toggle_presenca.setChecked(veiculo
										.isPresenca_sensor());
								toggle_audio.setChecked(veiculo
										.isAudio_sensor());
								toggle_movimento.setChecked(veiculo
										.isMovimento());
								toggle_ctrl_sistema.setChecked(veiculo
										.isFuncionamento_sistema_status());
								toggle_ctrl_carro.setChecked(veiculo
										.isFuncionamento_carro_status());
								toggle_ctrl_alarme.setChecked(veiculo
										.isSistema_seguranaca_status());

								adapter_list.add(veiculo, flag);
								listView.setAdapter(adapter_list);
							}
						}
					});
		} else 
			Toast.makeText(this, "Conexão com a Internet não está disponível",
					Toast.LENGTH_LONG).show();
	}

	public void fecharView(View v) {
		finish();
	}

	public void editar(View v) {
		if (Auxiliar.isOnline(A_Acoes.this)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(A_Acoes.this);
			builder.setIcon(R.drawable.carro_icone);
			builder.setTitle("Editar veículo " + veiculo.getPlaca());
			LayoutInflater inflater = (LayoutInflater) A_Acoes.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.alert_form_veiculo, null);

			EditText placa = (EditText) layout.findViewById(R.id.placa);
			placa.setText(veiculo.getPlaca());

			EditText num_dispositivo = (EditText) layout
					.findViewById(R.id.num_dispositivo);
			num_dispositivo
					.setText(String.valueOf(veiculo.getNum_dispositivo()));

			int id_veiculo = veiculo.getId_veiculo();
			builder.setView(layout)

					.setNegativeButton("Confirmar",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

								}
							})
					.setPositiveButton("Cancelar",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {

								}
							});
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					atualiza_view(true);
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
			Button botaoConfirmar = alertDialog
					.getButton(DialogInterface.BUTTON_NEGATIVE);
			botaoConfirmar.setOnClickListener(new CustomListener(alertDialog,
					A_Acoes.this, id_veiculo));
		} else {
			Toast.makeText(this, "Conexão com a Internet não está disponível",
					Toast.LENGTH_LONG).show();
		}
	}

	public void excluir(View v) {
		AlertDialog.Builder alerta = new AlertDialog.Builder(this);
		alerta.setIcon(R.drawable.carro_icone);
		alerta.setTitle("Exluir");
		alerta.setMessage("Veículo " + veiculo.getPlaca() + " será excluído");

		alerta.setPositiveButton("Confirmar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (Auxiliar.isOnline(A_Acoes.this)) {
							RestAdapter adapter = new RestAdapter.Builder()
									.setEndpoint(AppConfig.ENDPOINT).build();
							VeiculoAPI api = adapter.create(VeiculoAPI.class);
							api.removerVeiculo(AppConfig.API_KEY,
									veiculo.getId_veiculo(),
									new Callback<String>() {

										@Override
										public void failure(RetrofitError erro) {
											if (erro.getResponse() != null) {
												Toast.makeText(
														A_Acoes.this,
														erro.getResponse()
																.getReason()
																.toString(),
														Toast.LENGTH_LONG)
														.show();
											} else {
												if (Auxiliar.isOnline(A_Acoes.this)){
													Toast.makeText(A_Acoes.this,
															"Problemas nos servidores do SRSV",
															Toast.LENGTH_LONG).show();
												}
												else 
													Toast.makeText(A_Acoes.this, "Conexão com a Internet não está disponível",
															Toast.LENGTH_LONG).show();								
											}	
										}

										@Override
										public void success(String msg,
												Response arg) {
											Toast.makeText(A_Acoes.this, msg,
													Toast.LENGTH_LONG).show();
											finish();

										}
									});
						} else {
							Toast.makeText(
									A_Acoes.this,
									"Conexão com a Internet não está disponível",
									Toast.LENGTH_LONG).show();
						}

					}
				});

		alerta.setNegativeButton("Cancelar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		alerta.show();
	}

	public void controle_srsv(View v) {
		altera_controle(toggle_ctrl_sistema, "funcionamento_sistema_status");
	}

	public void controle_carro(View v) {
		altera_controle(toggle_ctrl_carro, "funcionamento_carro_status");
	}

	public void controle_alarme(View v) {
		altera_controle(toggle_ctrl_alarme, "sistema_seguranca_status");
	}

	public void altera_controle(final ToggleButton toggle_button, String campo) {
		if (Auxiliar.isOnline(A_Acoes.this)) {
			RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
					AppConfig.ENDPOINT).build();
			VeiculoAPI api = adapter.create(VeiculoAPI.class);
			api.alterarControle(AppConfig.API_KEY, toggle_button.isChecked(), campo,
					veiculo.getId_veiculo(), new Callback<String>() {
						@Override
						public void failure(RetrofitError erro) {
							if (erro.getResponse() != null) {
								Toast.makeText(A_Acoes.this,
										erro.getResponse().getReason(),
										Toast.LENGTH_LONG).show();
							} else {
								if (Auxiliar.isOnline(A_Acoes.this)){
									toggle_button.setChecked((toggle_button.isChecked()) ? false : true);
									Toast.makeText(A_Acoes.this,
											"Problemas nos servidores do SRSV",
											Toast.LENGTH_LONG).show();
								}
								else 
									Toast.makeText(A_Acoes.this, "Conexão com a Internet não está disponível",
											Toast.LENGTH_LONG).show();								
							}	
						}

						@Override
						public void success(String msg, Response resp) {
							if (msg != null)
								Toast.makeText(A_Acoes.this, msg,
										Toast.LENGTH_SHORT).show();
						}
					});
		} else {
			toggle_button.setChecked((toggle_button.isChecked()) ? false : true);
			Toast.makeText(this, "Conexão com a Internet não está disponível",
					Toast.LENGTH_LONG).show();
		}			
	}
}

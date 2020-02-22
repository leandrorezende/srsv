package br.com.srsv;

import java.util.List;

import br.com.srsv.bean.BVeiculo;
import br.com.srsv.utils.Auxiliar;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Adapter_Veiculo extends BaseAdapter {
	private Context context;
	private List<BVeiculo> lista;

	public Adapter_Veiculo(Context context, List<BVeiculo> lista) {
		this.context = context;
		this.lista = lista;
	}

	@Override
	public int getCount() {
		return lista.size();
	}

	@Override
	public Object getItem(int posicao) {
		BVeiculo veiculo = lista.get(posicao);
		return veiculo;
	}

	@Override
	public long getItemId(int posicao) {
		return posicao;
	}

	@Override
	public View getView(int posicao, View convertView, ViewGroup parent) {

		final BVeiculo veiculo = lista.get(posicao);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.lista_veiculos, null);


		TextView placaTxt = (TextView) v.findViewById(R.id.placa);
		placaTxt.setText("Placa: " + veiculo.getPlaca());
		
		TextView num_dispostivoTxt = (TextView) v.findViewById(R.id.txt_num_dispositivo);
		num_dispostivoTxt.setText(veiculo.getNum_dispositivo());
		
		TextView valor_saldoTxt = (TextView) v.findViewById(R.id.txt_saldo);
		valor_saldoTxt.setText(String.valueOf(veiculo.getSaldo_simcard()));

		Button btnAcoes = (Button) v.findViewById(R.id.btnAcoes);
		btnAcoes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Auxiliar.isOnline(context)) {
					Intent it = new Intent(context, A_Acoes.class);
					it.putExtra("placa", veiculo.getPlaca());
					it.putExtra("id_veiculo", String.valueOf(veiculo.getId_veiculo()));
					v.getContext().startActivity(it);
				} else {
					Toast.makeText(context, "Conexão com a Internet não está disponível",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		Button btnMapa = (Button) v.findViewById(R.id.btnMapa);
		btnMapa.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Auxiliar.isOnline(context)) {
					Intent it = new Intent(context, A_Rastrear.class);
					it.putExtra("placa", veiculo.getPlaca());
					v.getContext().startActivity(it);
				} else {
					Toast.makeText(context, "Conexão com a Internet não está disponível",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		return v;
	}
}

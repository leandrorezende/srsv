package br.com.srsv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.srsv.bean.BVeiculo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Adapter_Notificacoes extends BaseAdapter {
	private Context context;
	private List<BVeiculo> lista;

	public Adapter_Notificacoes(Context context) {
		this.context = context;
		this.lista = new ArrayList<BVeiculo>();
	}

	@Override
	public int getCount() {
		return lista.size();
	}
	
	public void add(BVeiculo veiculo, boolean flag){
		if(getCount() > 0 && flag)
			lista.remove(0);
		lista.add(veiculo);
		notifyDataSetChanged();
	}
	
	public void remove(){
		lista.remove(0);
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
	public View getView(final int posicao, View convertView, ViewGroup parent) {
		final BVeiculo veiculo = lista.get(posicao);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.lista_notificacoes, null);
		
		TextView txt_msg_notificacao = (TextView) v.findViewById(R.id.msg_notificacao);
		txt_msg_notificacao.setText("Ocorrência - " + (posicao + 1));
		
		SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm");
		String date = df.format(Calendar.getInstance().getTime());
		TextView txt_horario = (TextView) v.findViewById(R.id.horario);
		txt_horario.setText("Horário: " + date);
		
		
		
		ImageView not_audio = (ImageView) v.findViewById(R.id.not_audio);
		ImageView not_presenca = (ImageView) v.findViewById(R.id.not_presenca);
		ImageView not_movimento = (ImageView) v.findViewById(R.id.not_movimento);
		ImageView not_sistema = (ImageView) v.findViewById(R.id.not_sistema);	
		
		if(!veiculo.isAudio_sensor())
			not_audio.setImageResource(R.drawable.btn_check_buttonless_on);
		else 
			not_audio.setImageResource(R.drawable.ic_alert);
		
		if(!veiculo.isPresenca_sensor())
			not_presenca.setImageResource(R.drawable.btn_check_buttonless_on);
		else 
			not_presenca.setImageResource(R.drawable.ic_alert);
		
		if(!veiculo.isMovimento())
			not_movimento.setImageResource(R.drawable.btn_check_buttonless_on);
		else 
			not_movimento.setImageResource(R.drawable.ic_alert);
		
		if(!veiculo.isFalha())
			not_sistema.setImageResource(R.drawable.btn_check_buttonless_on);
		else 
			not_sistema.setImageResource(R.drawable.ic_alert);
	
		ImageButton btnMapa = (ImageButton) v.findViewById(R.id.retirar_item);
		btnMapa.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				lista.remove(posicao);
				notifyDataSetChanged();
			}
		});
		
		return v;
	}
}

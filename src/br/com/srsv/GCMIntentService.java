package br.com.srsv;

import android.content.Context;
import android.content.Intent;
import br.com.srsv.app.AppConfig;
import br.com.srsv.utils.ActivityStackUtils;
import br.com.srsv.utils.NotificationUtil;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * IntentService responsável por tratar as mensagens do GCM (Google Cloud
 * Messaging).
 * 
 * O método onRegistered é chamado quando o registro no GCM é feito com sucesso.
 * Neste momento temos o registrationId único do device.
 * 
 * O método onMessage é chamado quando uma mensagem por push é recebida.
 * 
 * @author Ricardo Lecheta
 * 
 */
public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(AppConfig.PROJECT_NUMBER);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		enviarMensagemParaApp("Device registrado: registrationId = "
				+ registrationId);

		reconfiguraActivity();
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		enviarMensagemParaApp("Device removido do registro.");

		reconfiguraActivity();
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		String msg = intent.getStringExtra("msg");

		enviarMensagemParaApp(msg);
	}

	@Override
	public void onError(Context context, String errorId) {
		enviarMensagemParaApp("Erro: " + errorId);
	}

	private void enviarMensagemParaApp(String msg) {
		// Se a aplicação está aberta
		if (ActivityStackUtils.isMyApplicationTaskOnTop(this)) {
			// Dispara uma Intent para o receiver configurado na Activity
			Intent intent = new Intent("RECEIVER_QUE_VAI_RECEBER_ESTA_MSG");
			intent.putExtra("msg", msg);

			sendBroadcast(intent);

		} else {
			// Cria a notificação, e informa para abrir a activity de
			// entrada
			Intent intent2 = new Intent(this, A_Login.class);
			intent2.putExtra("msg", msg);
			String[] parts = msg.split("»");
			final String placa = parts[0];
			NotificationUtil.generateNotification(this,
					"Alerta para veículo placa " + placa, intent2);
		}
	}

	private void reconfiguraActivity() {
		// Se a aplicação está aberta
		if (ActivityStackUtils.isMyApplicationTaskOnTop(this)) {
			// Dispara uma Intent para o receiver configurado na Activity
			Intent intent = new Intent("RECEIVER_QUE_VAI_RECEBER_ESTA_MSG");
			intent.putExtra("refresh", true);
			sendBroadcast(intent);
		}
	}
}

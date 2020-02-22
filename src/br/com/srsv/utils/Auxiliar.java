package br.com.srsv.utils;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import br.com.srsv.A_Login;
import br.com.srsv.api.UsuarioAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.helper.SQLiteHandler;
import br.com.srsv.helper.SessionManager;

public class Auxiliar {
	public static SQLiteHandler db;
	public static SessionManager session;

	public static void logoutUser(Context context) {
		session.setLogin(false);
		db.deleteUsers();
		retirarAssociacoes(context, false);	
		Intent intent = new Intent(context,A_Login.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	public static void retirarAssociacoes(final Context context, boolean flag){
		if (Auxiliar.isOnline(context)) {
			RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
					AppConfig.ENDPOINT).build();
			UsuarioAPI api = adapter.create(UsuarioAPI.class);
			api.removerAssocicao(AppConfig.API_KEY, AppConfig.REGISTRO, flag,
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
						public void success(String msg,
								Response resp) {
						}
					});
		} else {
			Toast.makeText(context,
					"Conexão com a Internet não está disponível",
					Toast.LENGTH_SHORT).show();
		}
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}
	}
}

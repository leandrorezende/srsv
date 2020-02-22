package br.com.srsv;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * @author Ricardo Lecheta
 * 
 */
public class GCM {
	private static final String TAG = "gcm";

	public static String getRegistrationId(Context context) {
		return GCMRegistrar.getRegistrationId(context);
	}
	public static String registrar(Context context, String projectId) {
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		 String regId = null;
		regId = getRegistrationId(context);
		if (regId.equals("")) {
			GCMRegistrar.register(context, projectId);
			Log.d(TAG, "GCM registrado com sucesso.");
			regId = getRegistrationId(context);
		} else {
			Log.d(TAG, "GCM já está registrado, ID: " + regId);
		}
		return regId;
	}
	public static void cancelar(Context context) {
		GCMRegistrar.unregister(context);
		Log.d(TAG, "GCM cancelado com sucesso.");
	}
	public static boolean isAtivo(Context context) {
		return GCMRegistrar.isRegistered(context);
	}
	public static void onDestroy(Context context) {
		GCMRegistrar.onDestroy(context);
	}
}
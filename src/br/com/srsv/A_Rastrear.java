package br.com.srsv;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import br.com.srsv.api.OcorrenciaAPI;
import br.com.srsv.app.AppConfig;
import br.com.srsv.bean.BOcorrencia;
import br.com.srsv.helper.GPSTracker;
import br.com.srsv.utils.Auxiliar;
import br.com.srsv.utils.CustomBuilder;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class A_Rastrear extends android.support.v4.app.FragmentActivity
		implements OnMapClickListener, OnCameraChangeListener,
		LocationListener, Runnable {

	protected GoogleMap map;
	private SupportMapFragment mapFragment;
	protected SRSVLocationSource locationSource;

	private LatLng usuarioLocalizacao = null;
	private LatLng veiculoLocalizacao = null;

	private ArrayList<LatLng> directionPointsBackup = null;
	private LatLngBounds latlngBounds;
	private Polyline newPolyline;
	private Marker marker = null;
	private TextView placaText;
	private ImageButton btnRoute;
	private BOcorrencia ocorrencia;

	private ProgressBar pb;
	private Button btn_recarregar;

	private Handler handler;
	private boolean on;
	private boolean route = false;
	private String placa;

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
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_rastrear);
		Intent it = getIntent();
		if (it != null) {
			placa = it.getStringExtra("placa");
		}

		handler = new Handler();
		handler.post(this);
		placaText = (TextView) findViewById(R.id.placa);
		placaText.setText(placa);
		btnRoute = (ImageButton) findViewById(R.id.btnRoute);

		pb = (ProgressBar) findViewById(R.id.progressBar);
		btn_recarregar = (Button) findViewById(R.id.btnRecarregar);
		btn_recarregar.setVisibility(View.INVISIBLE);
		pb.setVisibility(View.INVISIBLE);

		if (icicle != null) {
			if (icicle.containsKey("route")) {
				route = icicle.getBoolean("route");
				if (route == true && icicle.containsKey("direcoes")) {
					directionPointsBackup = icicle
							.getParcelableArrayList("direcoes");
					btnRoute.setImageResource(R.drawable.carro_icone);
				}
			}
		}

		registerReceiver(mensagemReceiver, new IntentFilter(
				"RECEIVER_QUE_VAI_RECEBER_ESTA_MSG"));
		String msg = getIntent().getStringExtra("msg");
		if (msg != null)
			finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("direcoes", directionPointsBackup);
		outState.putBoolean("route", route);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		on = true;
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		on = false;
		super.onDestroy();
	}

	public void fazerLogout(View v) {
		Auxiliar.logoutUser(A_Rastrear.this);
		finish();
	}

	protected void configureMap() {
		if (map == null) {
			mapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			map = mapFragment.getMap();
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			GPSTracker mGPS = new GPSTracker(this);

			usuarioLocalizacao = new LatLng(mGPS.getLatitude(),
					mGPS.getLongitude());
			if (usuarioLocalizacao.latitude != 0.0
					&& usuarioLocalizacao.longitude != 0.0) {
				final CameraPosition positon = new CameraPosition.Builder()
						.target(usuarioLocalizacao).zoom(12).build();
				CameraUpdate update = CameraUpdateFactory
						.newCameraPosition(positon);
				map.animateCamera(update, 5000, new CancelableCallback() {
					@Override
					public void onFinish() {
						Toast.makeText(getBaseContext(), "Mapa centralizado",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onCancel() {
						Toast.makeText(getBaseContext(), "Animação cancelada",
								Toast.LENGTH_LONG).show();
					}
				});
			}

			map.setOnMapClickListener(this);
			map.setMyLocationEnabled(true);
			if (directionPointsBackup != null)
				handleGetDirectionsResult(directionPointsBackup);
		}
	}

	private void adicionarMarcador(GoogleMap map, LatLng latLng,
			final BOcorrencia ocorrencia) {
		GPSTracker mGPS = new GPSTracker(this);
		usuarioLocalizacao = new LatLng(mGPS.getLatitude(), mGPS.getLongitude());
		this.ocorrencia = ocorrencia;
		boolean janelaStatus = false;
		veiculoLocalizacao = latLng;

		if (marker != null) {
			if (marker.isInfoWindowShown())
				janelaStatus = true;
			marker.remove();
		}

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(veiculoLocalizacao).title("Veículo " + placa)
				.snippet("Livro Android");
		markerOptions.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.carro_icone_trans));
		marker = map.addMarker(markerOptions);

		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				marker.hideInfoWindow();
			}
		});
		map.setInfoWindowAdapter(new InfoWindowAdapter() {

			@Override
			public View getInfoWindow(Marker marker) {
				LinearLayout linear = (LinearLayout) this
						.getInfoContents(marker);
				linear.setBackgroundResource(R.drawable.janela_marker);
				return linear;
			}

			@Override
			public View getInfoContents(Marker marker) {
				LinearLayout linear = new LinearLayout(getBaseContext());
				linear.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				linear.setOrientation(LinearLayout.VERTICAL);
				linear.setBackgroundColor(Color.BLACK);

				TextView t = new TextView(getBaseContext());
				t.setText("Veículo " + placa);
				t.setTextColor(Color.BLACK);
				t.setTextSize(20);
				t.setGravity(Gravity.CENTER);
				linear.addView(t);

				TextView tTitle = new TextView(getBaseContext());
				tTitle.setText("lat: "
						+ ocorrencia.getLatitude()
						+ "\nlon: "
						+ ocorrencia.getLongitude()
						+ "\ndistância: "
						+ CalculationByDistance(usuarioLocalizacao.latitude,
								usuarioLocalizacao.longitude,
								veiculoLocalizacao.latitude,
								veiculoLocalizacao.longitude) + "\nhorário: "
						+ ocorrencia.getCreated_at());
				t.setMaxLines(5);
				tTitle.setTextColor(Color.BLACK);
				linear.addView(tTitle);

				return linear;
			}
		});
		if (janelaStatus == true)
			marker.showInfoWindow();
	}

	public String CalculationByDistance(double initialLat, double initialLong,
			double finalLat, double finalLong) {
		if (initialLat != 0.0 && initialLong != 0.0) {
			int R = 6371;
			double lat1, lat2;
			double dLat = toRadians(finalLat - initialLat);
			double dLon = toRadians(finalLong - initialLong);

			lat1 = toRadians(initialLat);
			lat2 = toRadians(finalLat);

			double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
					+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1)
					* Math.cos(lat2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

			DecimalFormat df = new DecimalFormat("0.000");

			return df.format(R * c) + " km";
		} else
			return "GPS desligado";
	}

	public double toRadians(double deg) {
		return deg * (Math.PI / 180);
	}

	public void getRoute(View v) {
		if (route == false) {
			GPSTracker mGPS = new GPSTracker(this);
			usuarioLocalizacao = new LatLng(mGPS.getLatitude(),
					mGPS.getLongitude());
			if (usuarioLocalizacao.latitude != 0.0
					&& usuarioLocalizacao.longitude != 0.0) {
				route = true;
				btnRoute.setImageResource(R.drawable.ic_route_desativado);
				executaRoute(true);
			} else {
				Toast.makeText(A_Rastrear.this,
						"Gsp desligado, impossível traçar rota",
						Toast.LENGTH_LONG).show();
			}
		} else {
			route = false;
			map.clear();
			adicionarMarcador(map, veiculoLocalizacao, ocorrencia);
			btnRoute.setImageResource(R.drawable.ic_route_ativado);
		}
	}

	public void executaRoute(boolean progressoRota) {
		findDirections(usuarioLocalizacao, veiculoLocalizacao,
				GMapV2Direction.MODE_DRIVING, progressoRota);
	}

	public void fecharView(View v) {
		finish();
	}

	@Override
	public void onMapClick(LatLng latLng) {
		CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
		map.animateCamera(update);
	}

	@Override
	protected void onResume() {
		super.onResume();
		configureMap();

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
		} else {
			AlertDialog.Builder alertDialogBuilter = new AlertDialog.Builder(
					this);
			alertDialogBuilter
					.setMessage("O GPS está desligado, deseja ligar agora?")
					.setCancelable(false)
					.setPositiveButton("Sim",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent callGPSSettingIntent = new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivity(callGPSSettingIntent);
								}
							});

			alertDialogBuilter.setNegativeButton("Não",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			AlertDialog alert = alertDialogBuilter.create();
			alert.show();
		}
	}

	public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {

		PolylineOptions rectLine = new PolylineOptions().width(6).color(
				Color.BLUE);

		for (int i = 0; i < directionPoints.size(); i++) {
			rectLine.add(directionPoints.get(i));
		}
		if (newPolyline != null) {
			newPolyline.remove();
		}
		newPolyline = map.addPolyline(rectLine);
		latlngBounds = createLatLngBoundsObject(usuarioLocalizacao,
				veiculoLocalizacao);
		if (directionPointsBackup == null) {
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds,
					100));
			directionPointsBackup = directionPoints;
		}

	}

	private LatLngBounds createLatLngBoundsObject(LatLng firstLocation,
			LatLng secondLocation) {
		if (firstLocation != null && secondLocation != null) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(firstLocation).include(secondLocation);

			return builder.build();
		}
		return null;
	}

	public void findDirections(LatLng usuarioLocalizacao,
			LatLng veiculoLocalizacao, String mode, boolean progressoRota) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT,
				String.valueOf(usuarioLocalizacao.latitude));
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG,
				String.valueOf(usuarioLocalizacao.longitude));
		map.put(GetDirectionsAsyncTask.DESTINATION_LAT,
				String.valueOf(veiculoLocalizacao.latitude));
		map.put(GetDirectionsAsyncTask.DESTINATION_LONG,
				String.valueOf(veiculoLocalizacao.longitude));
		map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

		GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this,
				progressoRota);
		asyncTask.execute(map);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}

	public void reload(View v) {
		btn_recarregar.setVisibility(View.INVISIBLE);
		pb.setVisibility(View.VISIBLE);
		on = true;
		run();
	}

	public void run() {
		if (on) {
			RestAdapter adapter = new RestAdapter.Builder().setEndpoint(
					AppConfig.ENDPOINT).build();
			OcorrenciaAPI api = adapter.create(OcorrenciaAPI.class);
			api.listarOcorrencia(AppConfig.API_KEY, placa,
					new Callback<BOcorrencia>() {
						@Override
						public void failure(RetrofitError erro) {
							if (erro.getResponse() != null) {
								Toast.makeText(A_Rastrear.this,
										erro.getResponse().getReason(),
										Toast.LENGTH_LONG).show();
							} else {
								if (Auxiliar.isOnline(A_Rastrear.this)){
									Toast.makeText(A_Rastrear.this,
											"Problemas nos servidores do SRSV",
											Toast.LENGTH_LONG).show();
									on = false;
									btn_recarregar.setVisibility(View.VISIBLE);
									pb.setVisibility(View.INVISIBLE);
								}
								else {
									Toast.makeText(A_Rastrear.this, "Conexão com a Internet não está disponível",
											Toast.LENGTH_LONG).show();
									on = false;
									btn_recarregar.setVisibility(View.VISIBLE);
									pb.setVisibility(View.INVISIBLE);
								}								
							} 
						}

						@Override
						public void success(BOcorrencia ocorrencia,
								Response resp) {
							btn_recarregar.setVisibility(View.INVISIBLE);
							pb.setVisibility(View.INVISIBLE);
							if (ocorrencia != null) {
								veiculoLocalizacao = new LatLng(ocorrencia
										.getLatitude(), ocorrencia
										.getLongitude());
								adicionarMarcador(map, veiculoLocalizacao,
										ocorrencia);
								if (route == true)
									executaRoute(false);
							} else {
								ContextThemeWrapper ctw = new ContextThemeWrapper(
										A_Rastrear.this, R.style.MyTheme);
								CustomBuilder builder = new CustomBuilder(ctw,
										true);
								builder.setTitle("Aviso")
										.setIcon(
												android.R.drawable.ic_dialog_alert)
										.setMessage(
												"O dispositivo referente ao veículo, não enviou informações."
														+ "Verifique se o número de refência foi devidamente cadastrado ao "
														+ "veículo de placa: "
														+ placa
														+ " ou se o dispositivo foi devidamente"
														+ " instalado.")
										.setCancelable(false)
										.setPositiveButton(
												"Fechar",
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.dismiss();
													}
												});
								builder.show();
								on = false;
								btn_recarregar.setVisibility(View.VISIBLE);
								pb.setVisibility(View.INVISIBLE);
							}
						}
					});
			handler.postDelayed(this, 15000);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onCameraChange(CameraPosition position) {
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Provedor do GPS foi desabilitado
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Provedor do GPS foi habilitado
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Status do provedor do GPS mudou
	}
}

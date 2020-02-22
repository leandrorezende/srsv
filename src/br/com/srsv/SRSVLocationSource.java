package br.com.srsv;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

public class SRSVLocationSource implements LocationSource {
	private OnLocationChangedListener listener;
	@Override
	public void activate(OnLocationChangedListener listener){
		//my location layer foi ativado
		this.listener = listener;
	}
	
	@Override
	public void deactivate(){
		//my location layer desativado
		this.listener = null;
	}
	
	public void setLocation(Location location){
		if(this.listener != null){
			this.listener.onLocationChanged(location);
		}
	}
	
	public void setLocation(LatLng latLng){
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(latLng.latitude);
		location.setLongitude(latLng.longitude);
		if(this.listener != null){
			this.listener.onLocationChanged(location);
		}
	}
}

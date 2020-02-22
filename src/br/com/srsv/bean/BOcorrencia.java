package br.com.srsv.bean;

public class BOcorrencia {
	private int id_ocorrencia;
	private double latitude;
	private double longitude;
	private String created_at;
	public int getId_ocorrencia() {
		return id_ocorrencia;
	}
	public void setId_ocorrencia(int id_ocorrencia) {
		this.id_ocorrencia = id_ocorrencia;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
}

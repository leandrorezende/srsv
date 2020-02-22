package br.com.srsv.bean;

public class BVeiculo {
	private int id_veiculo;
	private int usuario_id;
	private String placa;
	private String num_dispositivo;
	private boolean funcionamento_carro_status;
	private boolean funcionamento_sistema_status;
	private boolean sistema_seguranaca_status;
	private boolean presenca_sensor;
	private boolean movimento;
	private boolean audio_sensor;
	private boolean falha;
	private float saldo_simcard;
	public int getId_veiculo() {
		return id_veiculo;
	}
	public void setId_veiculo(int id_veiculo) {
		this.id_veiculo = id_veiculo;
	}
	public int getUsuario_id() {
		return usuario_id;
	}
	public void setUsuario_id(int usuario_id) {
		this.usuario_id = usuario_id;
	}
	public String getPlaca() {
		return placa;
	}
	public void setPlaca(String placa) {
		this.placa = placa;
	}
	public String getNum_dispositivo() {
		return num_dispositivo;
	}
	public void setNum_dispositivo(String num_dispositivo) {
		this.num_dispositivo = num_dispositivo;
	}	
	public boolean isFuncionamento_carro_status() {
		return funcionamento_carro_status;
	}
	public void setFuncionamento_carro_status(boolean funcionamento_carro_status) {
		this.funcionamento_carro_status = funcionamento_carro_status;
	}
	public boolean isFuncionamento_sistema_status() {
		return funcionamento_sistema_status;
	}
	public void setFuncionamento_sistema_status(boolean funcionamento_sistema_status) {
		this.funcionamento_sistema_status = funcionamento_sistema_status;
	}
	public boolean isSistema_seguranaca_status() {
		return sistema_seguranaca_status;
	}
	public void setSistema_seguranaca_status(boolean sistema_seguranaca_status) {
		this.sistema_seguranaca_status = sistema_seguranaca_status;
	}
	public boolean isPresenca_sensor() {
		return presenca_sensor;
	}
	public void setPresenca_sensor(boolean presenca_sensor) {
		this.presenca_sensor = presenca_sensor;
	}
	public boolean isMovimento() {
		return movimento;
	}
	public void setMovimento(boolean movimento) {
		this.movimento = movimento;
	}
	public boolean isAudio_sensor() {
		return audio_sensor;
	}
	public void setAudio_sensor(boolean audio_sensor) {
		this.audio_sensor = audio_sensor;
	}
	public float getSaldo_simcard() {
		return saldo_simcard;
	}
	public void setSaldo_simcard(float saldo_simcard) {
		this.saldo_simcard = saldo_simcard;
	}
	public boolean isFalha() {
		return falha;
	}
	public void setFalha(boolean falha) {
		this.falha = falha;
	}
}

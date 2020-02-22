package br.com.srsv.api;

import br.com.srsv.bean.BOcorrencia;
import br.com.srsv.bean.BVeiculo;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.POST;

public interface OcorrenciaAPI {
	@FormUrlEncoded
	@POST("/ocorrencia/listarOcorrencia")
	public void listarOcorrencia(@Header("autorizacao") String autorizacao,
			@Field("placa") String placa, Callback<BOcorrencia> response);
	
	@FormUrlEncoded
	@POST("/ocorrencia/listarOcorrenciaAcoes")
	public void listarOcorrenciaAcoes(@Header("autorizacao") String autorizacao,
			@Field("id_veiculo") String id_veiculo, Callback<BVeiculo> response);
}

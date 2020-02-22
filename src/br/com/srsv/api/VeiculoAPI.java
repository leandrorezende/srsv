package br.com.srsv.api;

import java.util.List;

import br.com.srsv.bean.BVeiculo;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.POST;

public interface VeiculoAPI {
	@FormUrlEncoded
	@POST("/veiculo/listarVeiculo")
	public void listarVeiculo(@Header("autorizacao") String autorizacao,
			@Field("id_veiculo") String id_veiculo,
			Callback<List<BVeiculo>> response);

	@POST("/veiculo/cadastrarVeiculo")
	public void cadastrarVeiculo(@Header("autorizacao") String autorizacao,
			@Body BVeiculo veiculo, Callback<String> response);

	@FormUrlEncoded
	@POST("/veiculo/removerVeiculo")
	public void removerVeiculo(@Header("autorizacao") String autorizacao,
			@Field("id_veiculo") int id_veiculo, Callback<String> response);

	@POST("/veiculo/alterarVeiculo")
	public void alterarVeiculo(@Header("autorizacao") String autorizacao,
			@Body BVeiculo veiculo, Callback<String> response);

	@FormUrlEncoded
	@POST("/veiculo/alterarControle")
	public void alterarControle(@Header("autorizacao") String autorizacao,
			@Field("valor") boolean valor, @Field("campo") String campo,
			@Field("id_veiculo") int id_veiculo, Callback<String> response);
}

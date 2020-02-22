package br.com.srsv.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.POST;
import br.com.srsv.bean.BUsuario;

public interface UsuarioAPI {

	@POST("/usuario/cadastrarUsuario")
	public void cadastrarUsuario(@Body BUsuario usuario,
			Callback<String> response);

	@FormUrlEncoded
	@POST("/usuario/login")
	public void login(@Header("autorizacao") String autorizacao,
			@Field("id_registro") String id_registro,
			@Field("flag") boolean flag, Callback<BUsuario> response);

	@FormUrlEncoded
	@POST("/usuario/recuperarSenha")
	public void recuperarSenha(@Field("email") String email,
			Callback<String> response);

	@FormUrlEncoded
	@POST("/usuario/trocarSenha")
	public void trocarSenha(@Header("autorizacao") String autorizacao,
			@Field("usuario") String usuario,
			@Field("senhaAtual") String senhaAtual,
			@Field("novaSenha") String novaSenha, Callback<String> response);

	@FormUrlEncoded
	@POST("/usuario/removerAssocicao")
	public void removerAssocicao(@Header("autorizacao") String autorizacao,
			@Field("registro") String registro, @Field("flag") boolean flag,
			Callback<String> response);

	@FormUrlEncoded
	@POST("/usuario/removerAssocicaoDispositivo")
	public void removerAssocicaoDispositivo(
			@Header("autorizacao") String autorizacao,
			@Field("registro") String registro, Callback<String> callback);
}

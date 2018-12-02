package jp.kentan.minecraft.nekocore.data.api

import jp.kentan.minecraft.nekocore.data.model.MojangUser
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MojangService {

    @GET("/users/profiles/minecraft/{username}")
    fun getMojangUser(@Path("username") username: String): Call<MojangUser>
}
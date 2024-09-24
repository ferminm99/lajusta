package com.example.lajusta.data.remote

import com.example.lajusta.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface LaJustaService {

    @GET("verdura")
    suspend fun getVerduras(): Response<List<Verdura>>

    @GET("verdura")
    suspend fun getVerdurasEnvio(): Response<List<VerduraEnvio>>

    @GET("verdura/{id}")
    suspend fun getVerdura(@Path("id") id: Int): Response<Verdura>

    @POST("verdura")
    suspend fun addVerdura(@Body verdura: Verdura): Response<Verdura>

    @PUT("verdura")
    suspend fun editVerdura(@Body verdura: Verdura): Response<Verdura>

    @DELETE("verdura/{id}")
    suspend fun deleteVerdura(@Path("id") id: Int): Response<Any>?


    //

    @GET("Quintas")
    suspend fun getQuintas(): Response<List<Quinta>>

    @GET("Quintas/{id}")
    suspend fun getQuinta(@Path("id") id: Int): Response<Quinta>

    @PUT("Quintas")
    suspend fun editQuinta(@Body quinta: Quinta): Response<Quinta>

    @POST("Quintas")
    suspend fun addQuinta(@Body quinta: Quinta): Response<Quinta>

    @DELETE("Quintas/{id}")
    suspend fun deleteQuinta(@Path("id") id: Int): Response<Any>?

    //

    @GET("bolson")
    suspend fun getBolsones(@Query("id_ronda") id_ronda: Int): Response<List<Bolson>>

    @GET("bolson/{id}")
    suspend fun getBolson(@Path("id") id: Int): Response<Bolson>

    @PUT("bolson")
    suspend fun editBolson(@Body bolson: BolsonEnvio): Response<BolsonEnvio>

    @POST("bolson")
    suspend fun addBolson(@Body bolson: BolsonEnvio)

    @DELETE("bolson/{id}")
    suspend fun deleteBolson(@Path("id") id: Int): Response<Any>?

    //

    @GET("FamiliasProductoras")
    suspend fun getFamiliasProductoras(): Response<List<FamiliaProductora>>

    @GET("FamiliasProductoras/{id}")
    suspend fun getFamiliaProductora(@Path("id") id: Int): Response<FamiliaProductora>

    @PUT("FamiliasProductoras")
    suspend fun editFamiliaProductora(@Body familiaProductora: FamiliaProductora): Response<FamiliaProductora>

    @POST("FamiliasProductoras")
    suspend fun addFamiliaProductora(@Body familiaProductora: FamiliaProductora): Response<FamiliaProductora>

    @DELETE("FamiliasProductoras/{id}")
    suspend fun deleteFamiliaProductora(@Path("id") id: Int): Response<Any>?


    //

    @GET("Visitas")
    suspend fun getVisitas(): Response<List<Visita>>

    @GET("Visitas/{id}")
    suspend fun getVisita(@Path("id") id: Int): Response<Visita>

    @PUT("Visitas")
    suspend fun editVisita(@Body visita: VisitaEdit)

    @POST("Visitas")
    suspend fun addVisita(@Body visita: VisitaAdd)

    @DELETE("Visitas/{id}")
    suspend fun deleteVisita(@Path("id") id: Int): Response<Any>

    //

    @GET("rondas")
    suspend fun getRondas(): Response<List<Ronda>>

    @GET("rondas/{id}")
    suspend fun getRonda(@Path("id") id: Int): Response<Ronda>

    @PUT("rondas")
    suspend fun editRonda(@Body ronda: Ronda): Response<Ronda>

    @POST("rondas")
    suspend fun addRonda(@Body ronda: Ronda): Response<Ronda>

    @DELETE("rondas/{id}")
    suspend fun deleteRonda(@Path("id") id: Int): Response<Any>

    //

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>

    @PUT("users")
    suspend fun editUser(@Body user: User): Response<User>

    @POST("users")
    suspend fun addUser(@Body user: User): Response<User>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Any>

    //

    @GET("Parcelas")
    suspend fun getParcelas(): Response<List<ParcelaNormal>>

    @GET("Parcelas/{id}")
    suspend fun getParcela(@Path("id") id: Int): Response<ParcelaNormal>

    @PUT("Parcelas")
    suspend fun editParcela(@Body parcela: ParcelaNormal)

    @POST("Parcelas")
    suspend fun addParcela(@Body parcela: ParcelaNormal): Response<ParcelaNormal>

    @DELETE("Parcelas/{id}")
    suspend fun deleteParcela(@Path("id") id: Int)

}
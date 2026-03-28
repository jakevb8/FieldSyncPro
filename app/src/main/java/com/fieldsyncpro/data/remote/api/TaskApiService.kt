package com.fieldsyncpro.data.remote.api

import com.fieldsyncpro.data.remote.dto.TaskDto
import retrofit2.http.*

/** Retrofit interface for the FieldSync Pro backend REST API. */
interface TaskApiService {

    @GET("tasks")
    suspend fun getTasks(): List<TaskDto>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): TaskDto

    @POST("tasks")
    suspend fun createTask(@Body task: TaskDto): TaskDto

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body task: TaskDto
    ): TaskDto

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)
}

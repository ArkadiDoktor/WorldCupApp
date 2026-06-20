package com.worldcup.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.worldcup.app.data.local.WorldCupDatabase
import com.worldcup.app.data.local.dao.FavoriteTeamDao
import com.worldcup.app.data.local.dao.MatchDao
import com.worldcup.app.data.local.dao.ScorerDao
import com.worldcup.app.data.remote.api.FootballApiService
import com.worldcup.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("X-Auth-Token", Constants.API_KEY)
            .build()
        chain.proceed(request)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideFootballApiService(retrofit: Retrofit): FootballApiService =
        retrofit.create(FootballApiService::class.java)

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): WorldCupDatabase =
        Room.databaseBuilder(
            context,
            WorldCupDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideMatchDao(database: WorldCupDatabase): MatchDao = database.matchDao()

    @Singleton
    @Provides
    fun provideFavoriteTeamDao(database: WorldCupDatabase): FavoriteTeamDao =
        database.favoriteTeamDao()

    @Singleton
    @Provides
    fun provideScorerDao(database: WorldCupDatabase): ScorerDao = database.scorerDao()

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

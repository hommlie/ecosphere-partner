package com.ecosphere.partner.feature.tracking.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideTrackingDatabaseReference(
        firebaseDatabase: FirebaseDatabase
    ): DatabaseReference {
        return firebaseDatabase
            .reference
            .child("tracking")
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    @Named("tracking_sessions")
    fun provideTrackingCollection(
        firestore: FirebaseFirestore
    ): CollectionReference {

        return firestore.collection("tracking_sessions")
    }

}
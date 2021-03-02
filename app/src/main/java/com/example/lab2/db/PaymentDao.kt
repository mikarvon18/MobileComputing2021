package com.example.lab2.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import java.util.*

@Dao
interface PaymentDao {
    @Transaction
    @Insert
    fun insert(paymentInfo: PaymentInfo): Long


    @Query("DELETE FROM paymentInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM paymentInfo")
    fun getPaymentInfos(): List<PaymentInfo>

    @Query("SELECT * FROM paymentInfo WHERE date=:date")
    fun getLimitedPaymentInfos(date: String): List<PaymentInfo>

    @Query("SELECT * FROM paymentInfo WHERE uid = :id")
    fun getPaymentInfo(id: Int): List<PaymentInfo>

    @Query("UPDATE paymentInfo SET title=:title, date=:date, locationX=:locationX, locationY=:locationY WHERE uid = :id")
    fun editTable(title: String, date: String, locationX: String, locationY: String, id: Int)

}
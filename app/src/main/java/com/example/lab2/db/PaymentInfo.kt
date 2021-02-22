package com.example.lab2.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "paymentInfo")
data class PaymentInfo(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name="title") var title:String,
    @ColumnInfo(name="date") var date:String,
    @ColumnInfo(name="locationX") var locationX:String,
    @ColumnInfo(name="locationY") var locationY:String,
    @ColumnInfo(name="creationTime") var creationTime:String,
    @ColumnInfo(name="creatorId") var creatorId:String,
    @ColumnInfo(name="reminderSeen") var reminderSeen:String

)
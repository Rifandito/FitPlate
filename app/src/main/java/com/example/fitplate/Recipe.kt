package com.example.fitplate

import android.os.Parcel
import android.os.Parcelable

data class Recipe(
    val imageResId: Int, // Ubah dari imageRes
    val title: String,
    val time: String, // Ubah dari times
    val nutrisi: String,
    val manfaat: String,
    val bahan: String,
    val pembuatan: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(imageResId)
        parcel.writeString(title)
        parcel.writeString(time)
        parcel.writeString(nutrisi)
        parcel.writeString(manfaat)
        parcel.writeString(bahan)
        parcel.writeString(pembuatan)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Recipe> {
        override fun createFromParcel(parcel: Parcel): Recipe {
            return Recipe(parcel)
        }

        override fun newArray(size: Int): Array<Recipe?> {
            return arrayOfNulls(size)
        }
    }
}

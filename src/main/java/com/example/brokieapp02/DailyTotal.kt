package com.example.brokieapp02


import android.os.Parcel
import android.os.Parcelable

data class DailyTotal(val day: String, val total: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(day)
        parcel.writeInt(total)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DailyTotal> {
        override fun createFromParcel(parcel: Parcel): DailyTotal {
            return DailyTotal(parcel)
        }

        override fun newArray(size: Int): Array<DailyTotal?> {
            return arrayOfNulls(size)
        }
    }
}

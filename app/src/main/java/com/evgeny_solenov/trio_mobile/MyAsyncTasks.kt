package com.example.testandro6

import android.os.AsyncTask
import android.os.Parcel
import android.os.Parcelable

open class MyAsyncTasks() : AsyncTask<String, String, String>(), Parcelable {
    constructor(parcel: Parcel) : this() {
    }

    override fun doInBackground(vararg params: String?): String {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyAsyncTasks> {
        override fun createFromParcel(parcel: Parcel): MyAsyncTasks {
            return MyAsyncTasks(parcel)
        }

        override fun newArray(size: Int): Array<MyAsyncTasks?> {
            return arrayOfNulls(size)
        }
    }
}
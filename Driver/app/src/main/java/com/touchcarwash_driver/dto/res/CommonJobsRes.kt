package com.touchcarwash_driver.dto.res
import com.google.gson.annotations.SerializedName


data class CommonJobsRes(
    val `data`: List<Data>,
    val response: Response
) {
    data class Data(
        val accessories: List<Accessory>,
        @SerializedName("customer_details")
        val customerDetails: CustomerDetails,
        val location: Location,
        val orderdetails: Orderdetails,
        val totalamount: String,
        val vehicledetails: Vehicledetails,
        val washtypes: List<Washtype>
    ) {
        data class Accessory(
            val discription: String,
            val offerprice: String,
            val typename: String
        )

        data class CustomerDetails(
            val contact1: String,
            val contact2: String,
            val name: String,
            val pincode: String,
            val place: String
        )

        data class Location(
            val address: Address,
            val addresstype: String
        ) {
            data class Address(
                val address: String,
                val name: String,
                val pincode: String,
                val place: String
            )
        }

        data class Orderdetails(
            val orderdate: String,
            val ordered: String
        )

        data class Vehicledetails(
            val custvehcileid: String,
            val custvehicleimgsig: String,
            val vehiclename: String
        )

        data class Washtype(
            val discription: String,
            val offerprice: String,
            val typename: String
        )
    }

    data class Response(
        val status: String
    )
}
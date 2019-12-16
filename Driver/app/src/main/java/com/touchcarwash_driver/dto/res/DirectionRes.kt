package com.touchcarwash_driver.dto.res
import com.google.gson.annotations.SerializedName


data class DirectionRes(
    @SerializedName("geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
) {
    data class GeocodedWaypoint(
        @SerializedName("geocoder_status")
        val geocoderStatus: String,
        @SerializedName("place_id")
        val placeId: String,
        val types: List<String>
    )

    data class Route(
        val bounds: Bounds,
        val copyrights: String,
        val legs: List<Leg>,
        @SerializedName("overview_polyline")
        val overviewPolyline: OverviewPolyline,
        val summary: String,
        val warnings: List<Any>,
        @SerializedName("waypoint_order")
        val waypointOrder: List<Any>
    ) {
        data class Bounds(
            val northeast: Northeast,
            val southwest: Southwest
        ) {
            data class Northeast(
                val lat: Double,
                val lng: Double
            )

            data class Southwest(
                val lat: Double,
                val lng: Double
            )
        }

        data class Leg(
            val distance: Distance,
            val duration: Duration,
            @SerializedName("end_address")
            val endAddress: String,
            @SerializedName("end_location")
            val endLocation: EndLocation,
            @SerializedName("start_address")
            val startAddress: String,
            @SerializedName("start_location")
            val startLocation: StartLocation,
            val steps: List<Step>,
            @SerializedName("traffic_speed_entry")
            val trafficSpeedEntry: List<Any>,
            @SerializedName("via_waypoint")
            val viaWaypoint: List<Any>
        ) {
            data class Distance(
                val text: String,
                val value: Int
            )

            data class Duration(
                val text: String,
                val value: Int
            )

            data class EndLocation(
                val lat: Double,
                val lng: Double
            )

            data class StartLocation(
                val lat: Double,
                val lng: Double
            )

            data class Step(
                val distance: Distance,
                val duration: Duration,
                @SerializedName("end_location")
                val endLocation: EndLocation,
                @SerializedName("html_instructions")
                val htmlInstructions: String,
                val maneuver: String,
                val polyline: Polyline,
                @SerializedName("start_location")
                val startLocation: StartLocation,
                @SerializedName("travel_mode")
                val travelMode: String
            ) {
                data class Distance(
                    val text: String,
                    val value: Int
                )

                data class Duration(
                    val text: String,
                    val value: Int
                )

                data class EndLocation(
                    val lat: Double,
                    val lng: Double
                )

                data class Polyline(
                    val points: String
                )

                data class StartLocation(
                    val lat: Double,
                    val lng: Double
                )
            }
        }

        data class OverviewPolyline(
            val points: String
        )
    }
}
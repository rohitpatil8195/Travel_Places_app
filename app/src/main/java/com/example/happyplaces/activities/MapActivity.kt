package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.databinding.ActivityMapBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(),OnMapReadyCallback {
    private var mHappyPlaceDetail:HappyPlaceModel ?= null
    private var binding:ActivityMapBinding ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail = intent.getParcelableExtra<Parcelable>(MainActivity.EXTRA_PLACE_DETAILS)
                    as HappyPlaceModel
        }


        if(mHappyPlaceDetail != null){
         var toolbarHappyPlaceMap = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_happy_place_map)
           setSupportActionBar(toolbarHappyPlaceMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title
            binding?.toolbarHappyPlaceMap?.setNavigationOnClickListener {
                onBackPressed()
            }
            val supportMapFragment:SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

         supportMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
         val position =LatLng(mHappyPlaceDetail!!.latitude,mHappyPlaceDetail!!.longitude)
         googleMap!!.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))
          val newLatLongZoom = CameraUpdateFactory.newLatLngZoom(position,15f)
          googleMap.animateCamera(newLatLongZoom)
    }


}
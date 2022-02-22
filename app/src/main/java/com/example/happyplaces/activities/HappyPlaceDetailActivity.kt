package com.example.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding: ActivityHappyPlaceDetailBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailModel:HappyPlaceModel ?=null

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
          //  happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
            happyPlaceDetailModel = intent.getParcelableExtra<Parcelable>(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }


        if(happyPlaceDetailModel != null){
            var tool = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_happy_place_details)
            setSupportActionBar(tool)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            tool.setNavigationOnClickListener {
                onBackPressed()
            }

            var img = Uri.parse(happyPlaceDetailModel.image)
             binding?.ivPlaceImage?.setImageURI(img)

            binding?.tvDescription?.text = happyPlaceDetailModel.description

              binding?.tvLocation?.text= happyPlaceDetailModel.location

        }
    }
}
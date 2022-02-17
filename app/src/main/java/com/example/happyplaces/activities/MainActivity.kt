package com.example.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         val fabButton=findViewById<FloatingActionButton>(R.id.fabHappyFace)
          fabButton.setOnClickListener {
              val intent = Intent(this, AddHappyPlaceActivity::class.java)
              startActivity(intent)
          }
        getHappyPlacesFromLocalDB()
    }

    private fun getHappyPlacesFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
         val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
     if(getHappyPlaceList.size > 0){
          for(i in getHappyPlaceList){
              Log.e("title",i.title)
              Log.e("Description",i.description)
          }
     }
    }
}
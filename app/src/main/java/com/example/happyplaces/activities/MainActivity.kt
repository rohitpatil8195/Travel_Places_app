package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.happyplaces.utils.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity(),HappyPlacesAdapter.onClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         val fabButton=findViewById<FloatingActionButton>(R.id.fabHappyFace)
          fabButton.setOnClickListener {
              val intent = Intent(this, AddHappyPlaceActivity::class.java)
              startActivityForResult(intent,ADD_ACTIVITY_REQUEST_CODE)


          }
        getHappyPlacesFromLocalDB()


    }


    private fun setUpHappyPlacesRecyclerView(
        happyPlaceList: ArrayList<HappyPlaceModel>
    ){
     val adapter = HappyPlacesAdapter(happyPlaceList ,this)
        val rv_happy_places_list = findViewById<RecyclerView>(R.id.rv_happy_places_list)
        rv_happy_places_list?.adapter =adapter


           val editSwipeHandler = object : SwipeToEditCallback(this){
               override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                   val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
                   adapter.updateItem(this@MainActivity,viewHolder.adapterPosition,
                       ADD_ACTIVITY_REQUEST_CODE)
               }
           }
        val editTouchHelper =ItemTouchHelper(editSwipeHandler)
        editTouchHelper.attachToRecyclerView(rv_happy_places_list)


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
                adapter.removeAt(this@MainActivity,viewHolder.adapterPosition)
                getHappyPlacesFromLocalDB()
            }
        }
        val deleteTouchHelper =ItemTouchHelper(deleteSwipeHandler)
        deleteTouchHelper.attachToRecyclerView(rv_happy_places_list)

    }

    private fun getHappyPlacesFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
         val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
        val rv_happy_places_list = findViewById<RecyclerView>(R.id.rv_happy_places_list)
        val tv_no_record = findViewById<TextView>(R.id.tv_no_record)
     if(getHappyPlaceList.size > 0){

         rv_happy_places_list.visibility = View.VISIBLE
         tv_no_record.visibility =View.GONE
         setUpHappyPlacesRecyclerView(getHappyPlaceList)

     }else{
         rv_happy_places_list.visibility = View.GONE
         tv_no_record.visibility =View.VISIBLE
     }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getHappyPlacesFromLocalDB()
            }
        }else{
            Log.e("Activity","Back Pressed or cancelled")
        }
    }



    override fun onItemClick(position: Int, model: ArrayList<HappyPlaceModel>) {
        val model =model[position]
        Log.e("clicked","item clicked at $model")
        val intent = Intent(this,HappyPlaceDetailActivity::class.java)
        intent.putExtra(EXTRA_PLACE_DETAILS,model)
        startActivity(intent)
    }


    companion object{
        var ADD_ACTIVITY_REQUEST_CODE =1
         var EXTRA_PLACE_DETAILS = "extra_place_details"

    }
}
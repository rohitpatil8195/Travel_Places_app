package com.example.happyplaces

import android.app.AlertDialog
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(),View.OnClickListener {
    private var binding:ActivityAddHappyPlaceBinding?=null
     private var cal = Calendar.getInstance()
    private lateinit var dateSetListner: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
              onBackPressed()
        }

        dateSetListner = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDate()
        }
        var et_date = findViewById<AppCompatEditText>(R.id.et_date)
        et_date.setOnClickListener(this)

        var tv_add_image = findViewById<TextView>(R.id.tv_add_image)
        tv_add_image.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
       when(v!!.id){
           R.id.et_date ->{
               DatePickerDialog(this@AddHappyPlaceActivity,
                   dateSetListner,cal.get(Calendar.YEAR),
                   cal.get(Calendar.MONTH),
                   cal.get(Calendar.DAY_OF_MONTH)).show()
           }
           R.id.tv_add_image ->{
            val pictureDialog = AlertDialog.Builder(this)
               pictureDialog.setTitle("Select Action")
             val pictureDialogItems = arrayOf("Select photo from Gallery",
                "Caputure photo from camera")
               pictureDialog.setItems(pictureDialogItems){
                  dialog ,which ->
                      when(which){
                          0 -> choosePhotoFromGallary()
                          1 ->
                           Toast.makeText(this@AddHappyPlaceActivity,"Feature comming Soon",Toast.LENGTH_LONG).show()
                      }

               }
               pictureDialog.show()
           }
       }
    }

    private fun choosePhotoFromGallary() {

    }

    private fun updateDate(){
        var et_date = findViewById<AppCompatEditText>(R.id.et_date)
        val myFormat = "dd.MM.yyyy"
        val sdf =SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }
}
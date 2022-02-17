package com.example.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(),View.OnClickListener {
    private var binding:ActivityAddHappyPlaceBinding?=null
     private var cal = Calendar.getInstance()
    private lateinit var dateSetListner: DatePickerDialog.OnDateSetListener
    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
     private lateinit var resultLauncherCamera:ActivityResultLauncher<Intent>
     private var saveImageToInternalStorage:Uri ?=null
      private var mLatitude:Double = 0.0
    private var mLongitude:Double = 0.0

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
         updateDate()
//        val et_date = findViewById<AppCompatEditText>(R.id.et_date)
//        et_date.setOnClickListener(this)
        binding?.etDate?.setOnClickListener(this)

//        val tv_add_image = findViewById<TextView>(R.id.tv_add_image)
//        tv_add_image.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)

        binding?.btnSave?.setOnClickListener(this)

        registerOnActivityForResult()
        registerOnActivityForResultForCamera()


    }

    private fun registerOnActivityForResult() {
        galleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if(data!=null){

                    val contentUri=data.data
                     //var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)

                    try{
//                        val selectedImageBitmap:Bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
//                        binding.selectedImageImageView.setImageBitmap(selectedImageBitmap)
                        //OR

                        if(Build.VERSION.SDK_INT < 28) {
                           var  bitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                contentUri
                            )
                             saveImageToInternalStorage =saveImageToInternalStorage(bitmap)
                            Log.e("saveImagefromGallary28","${saveImageToInternalStorage}")
                        } else {
                            val src =
                                contentUri?.let { ImageDecoder.createSource(this.contentResolver, it) }
                             var bitmap = ImageDecoder.decodeBitmap(src!!)

                             saveImageToInternalStorage =saveImageToInternalStorage(bitmap)
                            Log.e("saveImagefromaboveapi28","${saveImageToInternalStorage}")

                        }


                        binding?.ivPlaceImage?.setImageURI(contentUri)


                    }
                    catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun registerOnActivityForResultForCamera(){
        resultLauncherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data

                val thumbNail : Bitmap = data!!.extras?.get("data") as Bitmap
                 saveImageToInternalStorage =saveImageToInternalStorage(thumbNail)
                Log.e("saveImageToIntern","${saveImageToInternalStorage}")
                binding?.ivPlaceImage?.setImageBitmap(thumbNail)
            }
        }

    }

    override fun onClick(v: View?) {
       when(v!!.id){
           binding?.etDate?.id->{
               DatePickerDialog(this@AddHappyPlaceActivity,
                   dateSetListner,cal.get(Calendar.YEAR),
                   cal.get(Calendar.MONTH),
                   cal.get(Calendar.DAY_OF_MONTH)).show()
           }
          binding?.tvAddImage?.id ->{
            val pictureDialog = AlertDialog.Builder(this)
               pictureDialog.setTitle("Select Action")
             val pictureDialogItems = arrayOf("Select photo from Gallery",
                "Caputure photo from camera")
               pictureDialog.setItems(pictureDialogItems){
                  dialog ,which ->
                      when(which){
                          0 -> choosePhotoFromGallary()
                          1 ->
                              takePhotoFromCamera()
                      }

               }
               pictureDialog.show()
           }
           binding?.btnSave?.id ->{
               when{
                   binding?.etTitle?.text.isNullOrEmpty() ->{
                       Toast.makeText(this,"Please enter title",Toast.LENGTH_LONG).show()
                   }
                   binding?.etDescription?.text.isNullOrEmpty() ->{
                       Toast.makeText(this,"Please enter description",Toast.LENGTH_LONG).show()
                   }
                   binding?.etLocation?.text.isNullOrEmpty() ->{
                       Toast.makeText(this,"Please enter location",Toast.LENGTH_LONG).show()
                   }
                   saveImageToInternalStorage == null ->{
                       Toast.makeText(this,"Please select an image",Toast.LENGTH_LONG).show()
                   }
                   else ->{
                    val happyPlaceModel = HappyPlaceModel(
                        0,
                        binding?.etTitle?.text.toString(),
                        saveImageToInternalStorage.toString(),
                        binding?.etDescription?.text.toString(),
                        binding?.etDate?.text.toString(),
                        binding?.etLocation?.text.toString(),
                        mLatitude,
                        mLongitude
                    )
                       val dbHandler = DatabaseHandler(this)
                       val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                       if(addHappyPlace > 0){
                           Toast.makeText(this,"Happy place details are inserted successful",Toast.LENGTH_LONG).show()
                           finish()
                       }

                   }
               }

           }
       }
    }

    private fun choosePhotoFromGallary() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report != null) {
                        if(report.areAllPermissionsGranted()){
                            val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            galleryImageResultLauncher.launch(galleryIntent)
                        }
                    }
                }


                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermission()
                }
            }).onSameThread().check()

    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    resultLauncherCamera.launch(cameraIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationalDialogForPermission()
            }
        }).onSameThread().check()
    }



    private fun showRationalDialogForPermission() {
      AlertDialog.Builder(this).setMessage("It looks like you have turned off permission required for this feature. It can be enabled under App settings").
              setPositiveButton("GO TO SETTINGS"){
                 _,_ ->
                 try {
                     val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                     val uri = Uri.fromParts("package",packageName,null)
                     intent.data = uri
                     startActivity(intent)
                 }catch (e:ActivityNotFoundException){
                     e.printStackTrace()
                 }
              }.setNegativeButton("Cancel"){
                  dialog,_->
                dialog.dismiss()
      }.show()
    }

    private fun updateDate(){
        val et_date = findViewById<AppCompatEditText>(R.id.et_date)
        val myFormat = "dd.MM.yyyy"
        val sdf =SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

  private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
    val wrapper = ContextWrapper(applicationContext)
      var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
       file = File(file,"${UUID.randomUUID()}.jpg")
      try {
          val stream:OutputStream =FileOutputStream(file)
           bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
          stream.flush()
          stream.close()
      }catch (e: IOException){
          e.printStackTrace()
      }
      return Uri.parse(file.absolutePath)
  }



    companion object{
        private const val IMAGE_DIRECTORY= "HappyPlacesImages"
    }
}


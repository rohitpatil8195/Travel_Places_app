package com.example.happyplaces.activities

import android.Manifest
import android.R.attr
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
import android.os.Parcelable
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.R.attr.data
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.widget.AutocompleteActivity


class AddHappyPlaceActivity : AppCompatActivity(),View.OnClickListener {
    private var binding:ActivityAddHappyPlaceBinding?=null
     private var cal = Calendar.getInstance()
    private lateinit var dateSetListner: DatePickerDialog.OnDateSetListener
    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
     private lateinit var resultLauncherCamera:ActivityResultLauncher<Intent>
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
     private lateinit var resultForGoogleAutocomplete: ActivityResultLauncher<Intent>
     private var saveImageToInternalStorage:Uri ?=null
      private var mLatitude:Double = 0.0
    private var mLongitude:Double = 0.0
    private var mHappyPlacesDetails:HappyPlaceModel ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
              onBackPressed()
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        dateSetListner = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDate()
        }

         if(!Places.isInitialized()){
             Places.initialize(this@AddHappyPlaceActivity ,resources.getString(R.string.google_maps_api_key))
         }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlacesDetails = intent.getParcelableExtra<Parcelable>(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }


         updateDate()
        if (mHappyPlacesDetails != null){
            supportActionBar?.title ="Edit Happy Place"
            binding?.etTitle?.setText(mHappyPlacesDetails!!.title)
            binding?.etDescription?.setText(mHappyPlacesDetails!!.description)
            binding?.etDate?.setText(mHappyPlacesDetails!!.date)
            binding?.etLocation?.setText(mHappyPlacesDetails!!.location)
            mLatitude =mHappyPlacesDetails!!.latitude
            mLongitude=mHappyPlacesDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(
                mHappyPlacesDetails!!.image
            )
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
            binding?.btnSave?.setText("UPDATE")

        }

        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)


        registerOnActivityForResult()
        registerOnActivityForResultForCamera()
      //  startActivityForgetResult()
        getLocation()



    }

    private fun getLocation():Boolean {
      val locationManager:LocationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)  || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

     @SuppressLint("MissingPermission")
     private fun requestNewLocationData(){
             val locationRequest = LocationRequest.create()?.apply {
                 interval = 10000
                 fastestInterval = 5000
                 priority = LocationRequest.PRIORITY_HIGH_ACCURACY

         }

         mFusedLocationProviderClient.requestLocationUpdates(locationRequest,mLocationCallBack,
             Looper.myLooper())
     }

    private val mLocationCallBack =object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
           val mLastLocation:Location =locationResult!!.lastLocation
            mLatitude = mLastLocation.latitude
            Log.i("currant location","$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.i("currant location","$mLongitude")
            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity,mLatitude,mLongitude)
             addressTask.setAddressListener(object:
                 GetAddressFromLatLng.AddressListener{

                 override fun onAddressFound(address: String?) {
                      binding?.etLocation?.setText(address)
                 }

                 override fun onError(){
                     Log.e("error","Something went wrong")
                 }

             })
            addressTask.getAddress()
        }
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
                        if(mHappyPlacesDetails  == null) 0 else mHappyPlacesDetails!!.id,
                        binding?.etTitle?.text.toString(),
                        saveImageToInternalStorage.toString(),
                        binding?.etDescription?.text.toString(),
                        binding?.etDate?.text.toString(),
                        binding?.etLocation?.text.toString(),
                        mLatitude,
                        mLongitude
                    )
                       val dbHandler = DatabaseHandler(this)
                        if(mHappyPlacesDetails == null){
                       val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                            if(addHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                            if(updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }


                   }
               }

           }
             R.id.et_location ->{
                 try {
                        val fields= listOf(
                            Place.Field.ID,Place.Field.NAME,
                            Place.Field.LAT_LNG,
                            Place.Field.ADDRESS
                        )
                     val intent =
                         Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fields)
                             .build(this)
                     startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                 }catch (e:Exception){
                     e.printStackTrace()
                 }
             }

           R.id.tv_select_current_location ->{
               Log.e("ll","clicked")
               if(!getLocation()){
                   Toast.makeText(this@AddHappyPlaceActivity,"Your location provider is turned off.Please Turn on",Toast.LENGTH_LONG).show()
                   Log.e("off","turned off")
                   val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                   startActivity(intent)
               }else{
                   Dexter.withContext(this).withPermissions(
                       Manifest.permission.ACCESS_FINE_LOCATION,
                       Manifest.permission.ACCESS_COARSE_LOCATION
                   ).withListener(object :MultiplePermissionsListener{
                       override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                           if (report!!.areAllPermissionsGranted()){
                               Log.e("On","turned On")
                               requestNewLocationData()
                               Toast.makeText(this@AddHappyPlaceActivity,"Location Permission is granted",Toast.LENGTH_LONG).show()
                           }
                       }

                       override fun onPermissionRationaleShouldBeShown(
                           p0: MutableList<PermissionRequest>?,
                           p1: PermissionToken?
                       ) {
                           showRationalDialogForPermission()
                       }
                   }).onSameThread().check()

               }

           }
       }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                                binding?.etLocation?.setText(place.address)
                                 mLatitude = place.latLng!!.latitude
                                   mLongitude =place.latLng!!.longitude

                        Log.i("TAG", "Place: ${place.name}, ${place.id}")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i("TAG", "$(status.statusMessage)")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


//    private fun startActivityForgetResult() {
//        resultForGoogleAutocomplete=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ data ->
//          //  Log.e("data","$data")
//
//        val place = data.data
//            Log.e("Places","$place")
////        binding?.etLocation?.setText(place.address)
////        mLatitude = place.latLng!!.latitude
////        mLongitude =place.latLng!!.longitude
//        }
//    }


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
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE =3
        private const val LOCATION_PERMISSION_CODE =2
    }

}

private fun LocationManager.requestLocationUpdates(gpsProvider: String, i: Int, fl: Float, addHappyPlaceActivity: AddHappyPlaceActivity) {

}



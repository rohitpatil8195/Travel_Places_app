package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.databinding.ItemHappyPlaceBinding


open class HappyPlacesAdapter(
     val list: ArrayList<HappyPlaceModel>,
     val listener: onClickListener
): RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {


   inner class ViewHolder(
       val itemHappyPlaceBinding: ItemHappyPlaceBinding,

   )
        :RecyclerView.ViewHolder(itemHappyPlaceBinding.root), View.OnClickListener {
     //  private var onClickListener:onClickListener?= null
        fun bindItem(happyPlaceModel: HappyPlaceModel){
            itemHappyPlaceBinding.ivPlaceImage.setImageURI(Uri.parse((happyPlaceModel.image)))
        itemHappyPlaceBinding.tvTitle.text = happyPlaceModel.title
      itemHappyPlaceBinding.tvDescription.text = happyPlaceModel.description
        }

       init {
            itemView.setOnClickListener(this)
       }

       override fun onClick(v: View?) {
           val position=adapterPosition

           if(position != RecyclerView.NO_POSITION){
           listener.onItemClick(position,list)
           }
       }

   }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false,))
    }


//     fun notifyEditItem(activity:Activity,position: Int,requestCode:Int){
//         val intent =Intent(AddHappyPlaceActivity::class.java)
//         intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
//     }


     fun deleteItem(position: Int){
       //  fun onItemSwipe(position)

         list.removeAt(position)
         notifyDataSetChanged()
     }

      fun editItem(position: Int){

      }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.bindItem(model)
    }


    override fun getItemCount(): Int {
      return list.size
    }


     interface onClickListener{
         fun onItemClick(position: Int, model: ArrayList<HappyPlaceModel>)
     }

    //private class MyViewHolder(view:View):RecyclerView.ViewHolder(view)
}
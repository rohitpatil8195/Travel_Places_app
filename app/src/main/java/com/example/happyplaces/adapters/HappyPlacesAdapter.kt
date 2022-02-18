package com.example.happyplaces.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.databinding.ItemHappyPlaceBinding

 open class HappyPlacesAdapter(
     val list: ArrayList<HappyPlaceModel>
): RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {


   inner class ViewHolder(val itemHappyPlaceBinding: ItemHappyPlaceBinding)
        :RecyclerView.ViewHolder(itemHappyPlaceBinding.root) {
       private var onClickListener:onClickListener?= null
        fun bindItem(happyPlaceModel: HappyPlaceModel){
            itemHappyPlaceBinding.ivPlaceImage.setImageURI(Uri.parse((happyPlaceModel.image)))
        itemHappyPlaceBinding.tvTitle.text = happyPlaceModel.title
      itemHappyPlaceBinding.tvDescription.text = happyPlaceModel.description
        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.bindItem(model)
    }


    override fun getItemCount(): Int {
      return list.size
    }





     interface onClickListener{
         fun onClick(position: Int,model: HappyPlaceModel)
     }

    //private class MyViewHolder(view:View):RecyclerView.ViewHolder(view)
}
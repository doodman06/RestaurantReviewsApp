package com.example.restaurantreviewsapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView


class RestAdapter( private val list: MutableList<String>) : RecyclerView.Adapter<RestAdapter.ViewHolder>() {


    /*
   * Inflate our views using the layout defined in row_layout.xml
   */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.search_rest_row_layout, parent, false)

        return ViewHolder(v)
    }



    /*
     * Bind the data to the child views of the ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = list[position]

        holder.restTitle.text = name


        holder.restCard.setOnClickListener { _ ->
            val intent = Intent(holder.itemView.context, Restaurant::class.java)
            intent.putExtra(holder.itemView.context.getString(R.string.extras_restname), name)
            startActivity(holder.itemView.context, intent, null)

        }





    }

    override fun getItemCount(): Int {

        return list.size
    }

    /*
     * The parent class that handles layout inflation and child view use
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{


        var restTitle = itemView.findViewById<View>(R.id.search_rest_title) as TextView
        var restCard = itemView.findViewById<View>(R.id.search_rest_card) as CardView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {


        }
    }
}


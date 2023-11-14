package com.geded.apartemenkutenant

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkutenant.databinding.LayoutServiceListBinding
import com.squareup.picasso.Picasso

class ServiceListAdapter(val services:ArrayList<Service>, val context: FragmentActivity?):
    RecyclerView.Adapter<ServiceListAdapter.ServiceListViewHolder>(){
    class ServiceListViewHolder(val binding: LayoutServiceListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceListViewHolder {
        val binding = LayoutServiceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return services.size
    }

    override fun onBindViewHolder(holder: ServiceListViewHolder, position: Int) {
        val url = services[position].photo_url
        val price = Helper.formatter(services[position].price)
        var pricePer = ""
        if(services[position].pricePer=="package") {
            pricePer = "Paket"
        }
        else{
            pricePer = "Jam"
        }
        var availability = ""
        if (services[position].availability == 1){
            availability = "Ya"
        }
        else{
            availability = "Tidak"
        }
        with(holder.binding) {
            Picasso.get().load(url).into(imgServiceSL)
            txtSvcNameSL.text = services[position].name
            txtSvcPriceSL.text = "Harga: Rp$price/$pricePer"
            txtSvcAvailSL.text = "Tersedia: $availability"
            txtSvcSoldSL.text = "Terjual: " + services[position].sold.toString()
            txtSvcRatingSL.text = services[position].rating.toString()
        }
        holder.binding.btnDetailSL.setOnClickListener {
            val intent = Intent(this.context, ServiceDetailActivity::class.java)
            intent.putExtra(ServiceDetailActivity.SERVICE_ID, services[position].id)
            context?.startActivity(intent)
        }
    }
}
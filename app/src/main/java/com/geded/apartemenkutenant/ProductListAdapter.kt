package com.geded.apartemenkutenant

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkutenant.databinding.LayoutProductListBinding
import com.squareup.picasso.Picasso

class ProductListAdapter(val products:ArrayList<Product>, val context: FragmentActivity?):
    RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>() {
    class ProductListViewHolder(val binding: LayoutProductListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val binding = LayoutProductListBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ProductListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val url = products[position].photo_url
        val price = Helper.formatter(products[position].price)
        with(holder.binding) {
            Picasso.get().load(url).resize(200, 134).into(imgProductPL)
            txtProNamePL.text = products[position].name
            txtProPricePL.text = "Harga: Rp$price"
            txtProStockPL.text = "Stok: " + products[position].stock.toString()
            txtProSoldPL.text = "Terjual: " + products[position].sold.toString()
            txtProRatingPL.text = products[position].rating.toString()
        }
        holder.binding.btnDetailPL.setOnClickListener {
            val intent = Intent(this.context, ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.PRODUCT_ID, products[position].id.toString())
            context?.startActivity(intent)
        }
    }
}
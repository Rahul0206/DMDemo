package com.example.bluetooth_connection.adapter

import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth_connection.activity.DeviceDetailActivity
import com.example.bluetooth_connection.databinding.RecyclerScanDevicesBinding

class ScanResultAdapter(val context: Context) : RecyclerView.Adapter<ScanResultAdapter.Holder>() {
    class Holder(val binding: RecyclerScanDevicesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            RecyclerScanDevicesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        val model = differConfig.currentList[position]
        holder.binding.address = model.device.address

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.binding.connectable = model.isConnectable
            holder.binding.llCon.visibility = View.VISIBLE
        }

        // call event when clicked recycle view item and go to DeviceDetailActivity screen
        holder.binding.llView.setOnClickListener {
            val intent = Intent(context, DeviceDetailActivity::class.java)
            // put device
            intent.putExtra("model", model)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return differConfig.currentList.size
    }

    // update item in adapter using DiffUtil Class
    private val diffCallback = object : DiffUtil.ItemCallback<ScanResult>() {
        override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem.device.address == newItem.device.address
        }

        override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem.device.address == newItem.device.address
        }

    }
    private var differConfig = AsyncListDiffer(this, diffCallback)

    // setter method that call form main activity when scanning item add/update
    fun submitList(list: List<ScanResult>) = differConfig.submitList(list)

}
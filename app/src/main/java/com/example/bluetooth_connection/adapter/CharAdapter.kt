import android.bluetooth.BluetoothGattCharacteristic
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetooth_connection.databinding.RecyclerCharacteristicBinding

class CharAdapter : RecyclerView.Adapter<CharAdapter.Holder>() {

    class Holder(val binding: RecyclerCharacteristicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            RecyclerCharacteristicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // set device data in this list
        val model = differConfig.currentList[position]
        holder.binding.uuid = model.uuid.toString()
        if (model.service != null) {
            holder.binding.service = model.service.toString()
            holder.binding.serviceUID = "uuid: " + model.service.uuid.toString()
        }
    }

    override fun getItemCount(): Int {
        return differConfig.currentList.size
    }

    // update item in adapter using DiffUtil Class
    private val diffCallback = object : DiffUtil.ItemCallback<BluetoothGattCharacteristic>() {
        override fun areItemsTheSame(
            oldItem: BluetoothGattCharacteristic,
            newItem: BluetoothGattCharacteristic
        ): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(
            oldItem: BluetoothGattCharacteristic,
            newItem: BluetoothGattCharacteristic
        ): Boolean {
            return oldItem.uuid == newItem.uuid
        }

    }
    private var differConfig = AsyncListDiffer(this, diffCallback)

    // setter method that call form device detail activity when connect device
    fun submitList(list: List<BluetoothGattCharacteristic>) = differConfig.submitList(list)
}
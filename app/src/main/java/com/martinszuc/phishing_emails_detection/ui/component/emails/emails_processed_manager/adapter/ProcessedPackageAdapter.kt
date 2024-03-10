import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailProcessedPackageBinding
import com.martinszuc.phishing_emails_detection.utils.StringUtils

class ProcessedPackageAdapter(
    private var packages: List<ProcessedPackageMetadata>,
    private val onDeleteClicked: (String) -> Unit,
    private val onAddClicked: (View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ADD = 0
        private const val VIEW_TYPE_PACKAGE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_package, parent, false)
                object : RecyclerView.ViewHolder(view) {}
            }
            VIEW_TYPE_PACKAGE -> {
                val binding = ItemEmailProcessedPackageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProcessedPackageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProcessedPackageViewHolder) {
            val processedPackage = packages[position - 1] // Adjust for the add item at position 0
            holder.bind(processedPackage)
        } else {
            // Handle the Add item case
            holder.itemView.setOnClickListener {
                onAddClicked(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return packages.size + 1 // Add one for the Add item at the top
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_ADD else VIEW_TYPE_PACKAGE
    }

    fun setItems(newPackages: List<ProcessedPackageMetadata>) {
        packages = newPackages
        notifyDataSetChanged()
    }

    inner class ProcessedPackageViewHolder(private val binding: ItemEmailProcessedPackageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(processedPackage: ProcessedPackageMetadata) {
            binding.tvPackageName.text = processedPackage.packageName
            binding.tvIsPhishy.text = if (processedPackage.isPhishy) "Phishy" else "Safe"
            binding.tvCreationDate.text = StringUtils.formatTimestamp(processedPackage.creationDate)
            binding.tvPackageSize.text = "Size: ${StringUtils.formatBytes(processedPackage.fileSize)}"
            binding.tvNumberOfEmails.text = "Emails: ${processedPackage.numberOfEmails}"

            binding.btnDelete.setOnClickListener {
                onDeleteClicked(processedPackage.fileName)
            }
        }
    }
}

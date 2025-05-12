package com.stafftracker.ui.admin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stafftracker.R
import com.stafftracker.databinding.ItemActiveStaffBinding
import com.stafftracker.model.Staff
import java.text.SimpleDateFormat
import java.util.Locale

class ActiveStaffAdapter : ListAdapter<Staff, ActiveStaffAdapter.StaffViewHolder>(StaffDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemActiveStaffBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StaffViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class StaffViewHolder(private val binding: ItemActiveStaffBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(staff: Staff) {
            binding.tvStaffName.text = staff.name
            binding.tvStaffEmail.text = staff.email
            
            // Format check-in time
            staff.lastCheckInTime?.let { checkInTime ->
                val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(checkInTime)
                binding.tvCheckInTime.text = "Checked in at $formattedTime"
            }
            
            // Load staff photo
            if (staff.photoUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(staff.photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(binding.ivStaffPhoto)
            } else {
                binding.ivStaffPhoto.setImageResource(R.drawable.ic_person)
            }
        }
    }
    
    private class StaffDiffCallback : DiffUtil.ItemCallback<Staff>() {
        override fun areItemsTheSame(oldItem: Staff, newItem: Staff): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Staff, newItem: Staff): Boolean {
            return oldItem == newItem
        }
    }
} 
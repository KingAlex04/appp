package com.stafftracker.ui.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.stafftracker.databinding.FragmentDashboardBinding
import com.stafftracker.ui.admin.AdminViewModel

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AdminViewModel
    private lateinit var activeStaffAdapter: ActiveStaffAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get the shared ViewModel from the activity
        viewModel = ViewModelProvider(requireActivity())[AdminViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()
        
        // Load active staff
        viewModel.loadActiveStaff()
    }
    
    private fun setupRecyclerView() {
        activeStaffAdapter = ActiveStaffAdapter()
        binding.rvActiveStaff.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeStaffAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.activeStaff.observe(viewLifecycleOwner) { staffList ->
            activeStaffAdapter.submitList(staffList)
            
            // Show/hide no active staff message
            if (staffList.isNullOrEmpty()) {
                binding.tvNoActiveStaff.visibility = View.VISIBLE
                binding.rvActiveStaff.visibility = View.GONE
            } else {
                binding.tvNoActiveStaff.visibility = View.GONE
                binding.rvActiveStaff.visibility = View.VISIBLE
            }
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
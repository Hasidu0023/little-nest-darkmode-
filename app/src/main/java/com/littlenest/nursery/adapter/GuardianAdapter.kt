package com.littlenest.nursery.ui.family

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.littlenest.nursery.databinding.ItemGuardianCardBinding
import com.littlenest.nursery.model.Guardian

class GuardianAdapter(
    private var guardians: MutableList<Guardian>,
    private val onEditClick: (Guardian) -> Unit,
    private val onDeleteClick: (Guardian) -> Unit
) : RecyclerView.Adapter<GuardianAdapter.GuardianViewHolder>() {

    inner class GuardianViewHolder(val binding: ItemGuardianCardBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianViewHolder {
        val binding = ItemGuardianCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuardianViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuardianViewHolder, position: Int) {
        val guardian = guardians[position]
        holder.binding.textGuardianName.text = guardian.name
        holder.binding.textGuardianRelation.text = guardian.relation
        // Show default message if mobilePhone is empty
        holder.binding.textGuardianMobile.text =
            if (guardian.mobilePhone.isNullOrBlank()) "[No mobile]" else guardian.mobilePhone
        holder.binding.textGuardianPickupPermission.isChecked = guardian.pickupPermission

        holder.binding.btnEdit.setOnClickListener { onEditClick(guardian) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(guardian) }
    }

    override fun getItemCount() = guardians.size

    fun updateList(newList: List<Guardian>) {
        guardians.clear()
        guardians.addAll(newList)
        notifyDataSetChanged()
    }
}

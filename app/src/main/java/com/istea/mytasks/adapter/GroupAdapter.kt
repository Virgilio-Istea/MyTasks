package com.istea.mytasks.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.model.Group

class GroupAdapter(private val dataSet: ArrayList<Group>, private val onButtonClickListener:(selectedItem: ListenerType) -> Unit) : RecyclerView.Adapter<GroupAdapter.ViewHolder>()
{

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val divider : View = view.findViewById(R.id.group_recycler_divider)
        val name : TextView = view.findViewById(R.id.group_recycler_name)
        val tableRow : TableRow = view.findViewById(R.id.group_recycler)
        val popupMenu : ImageButton = view.findViewById(R.id.group_popup_menu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item,parent,false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    sealed class ListenerType {
        class SelectGroupListener(val group: Group): ListenerType()
        class PopupMenuListener(val group: Group, val view: View): ListenerType()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (dataSet[position].name == "Todos"){
            holder.divider.visibility = View.GONE
            holder.popupMenu.visibility = View.GONE
        }

        if (dataSet[position].name == "Sin Grupo"){
            holder.popupMenu.visibility = View.GONE
        }

        holder.name.text = dataSet[position].name

        holder.tableRow.setOnClickListener {
            onButtonClickListener(ListenerType.SelectGroupListener(dataSet[position]))
        }

        holder.popupMenu.setOnClickListener {
            onButtonClickListener(ListenerType.PopupMenuListener(dataSet[position], it))
        }

    }

}
package com.istea.mytasks.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.istea.mytasks.R
import com.istea.mytasks.model.ExpandableTasks
import com.istea.mytasks.model.Group
import com.istea.mytasks.model.Task

class TaskAdapter (private val context: Context, private val dataSet: ArrayList<ExpandableTasks>, val onButtonClickListener:(selectedItem: ListenerType) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType) {
            ExpandableTasks.PARENT -> {ParentViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.task_parent_item, parent, false))}

            else -> { ChildViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.task_child_item, parent, false))  }

        }
    }

    sealed class ListenerType {
        class SelectTaskListener(val task: Task, val status: String): ListenerType()
        class ToggleTaskCompletionListener(val task: Task, val status: String): ListenerType()
        class PopupMenuListener(val task: Task, val view: View, val status: String): ListenerType()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemViewType(position: Int): Int = dataSet[position].type

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = dataSet[position]

        when(row.type){
            ExpandableTasks.PARENT -> {
                when (row.parent.status) {
                    Group.DONE -> (holder as ParentViewHolder).completed.text = context.getString(R.string.taskStatus, "Listo", row.parent.tasks.count())
                    Group.TODO -> (holder as ParentViewHolder).completed.text = context.getString(R.string.taskStatus,"Para Hacer", row.parent.tasks.count())
                }

                (holder as ParentViewHolder).closeImage.setOnClickListener {
                    toogleVisibility(row,holder,position)
                }

                holder.upArrowImg.setOnClickListener{
                    toogleVisibility(row,holder,position)
                }

                holder.taskType.setOnClickListener {
                    toogleVisibility(row,holder,position)
                }

            }

            ExpandableTasks.CHILD -> {

                (holder as ChildViewHolder).taskName.text = row.child.title

                if (row.status == Group.DONE) {
                    holder.complete.setImageResource(R.drawable.baseline_radio_button_checked_24)
                }
                else {
                    holder.complete.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
                }

                holder.taskName.setOnClickListener {
                    onButtonClickListener(ListenerType.SelectTaskListener(row.child, row.status))
                }

                holder.complete.setOnClickListener{
                    onButtonClickListener(ListenerType.ToggleTaskCompletionListener(row.child, row.status))
                }

                holder.popupMenu.setOnClickListener {
                    onButtonClickListener(ListenerType.PopupMenuListener(row.child, it, row.status))
                }
            }
        }


    }

    class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout = itemView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.parent_container)
        internal var completed : TextView = itemView.findViewById(R.id.done_or_not)
        internal var closeImage : ImageView = itemView.findViewById(R.id.close_arrow)
        internal var upArrowImg : ImageView = itemView.findViewById(R.id.up_arrow)
        internal var taskType : TextView = itemView.findViewById(R.id.done_or_not)
    }

    class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var taskName : TextView = itemView.findViewById(R.id.task_name)
        internal var complete : ImageButton = itemView.findViewById(R.id.radioButton)
        internal val popupMenu : ImageButton = itemView.findViewById(R.id.task_popup_menu)
    }

    private fun expandRow(position: Int){
        val row = dataSet[position]
        var nextPosition = position
        when (row.type) {
            ExpandableTasks.PARENT -> {
                for(child in row.parent.tasks){
                    dataSet.add(++nextPosition, ExpandableTasks(ExpandableTasks.CHILD, child, row.status))
                }
                notifyDataSetChanged()
            }
            ExpandableTasks.CHILD -> {
                notifyDataSetChanged()
            }
        }
    }

    private fun collapseRow(position: Int){
        val row = dataSet[position]
        val nextPosition = position + 1
        when (row.type) {
            ExpandableTasks.PARENT -> {
                outerloop@ while (true) {
                    //  println("Next Position during Collapse $nextPosition size is ${shelfModelList.size} and parent is ${shelfModelList[nextPosition].type}")

                    if (nextPosition == dataSet.size || dataSet[nextPosition].type == ExpandableTasks.PARENT) {
                        break@outerloop
                    }
                    dataSet.removeAt(nextPosition)
                }
                notifyDataSetChanged()
            }
        }
    }

    private fun toogleVisibility(row : ExpandableTasks, holder: ParentViewHolder, position: Int) {
        if (row.isExpanded) {
            row.isExpanded = false
            collapseRow(position)
            holder.layout.setBackgroundColor(Color.WHITE)
            holder.upArrowImg.visibility = View.GONE
            holder.closeImage.visibility = View.VISIBLE
        }else{
            holder.layout.setBackgroundColor(Color.GRAY)
            row.isExpanded = true
            holder.upArrowImg.visibility = View.VISIBLE
            holder.closeImage.visibility = View.GONE
            expandRow(position)
        }
    }

}
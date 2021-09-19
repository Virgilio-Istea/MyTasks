package com.istea.mytasks.model

import java.io.Serializable
import kotlin.collections.ArrayList

data class Group(
        var documentId: String,
        val userId: String,
        val name: String,
        var tasks: ArrayList<Task>
) : Serializable

{
    override fun toString(): String {
        return name
    }
}



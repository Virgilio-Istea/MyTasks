package com.istea.mytasks.model

import java.io.Serializable

data class Group(
        var documentId: String,
        val name: String
) : Serializable

{
    companion object{
        const val TODO = "0"
        const val INPROGRESS = "1"
        const val DONE = "2"
    }
    override fun toString(): String {
        return name
    }
    fun toId(): String {
        return documentId
    }
}



package com.istea.mytasks.model

import java.io.Serializable

data class Group(
        val documentId: String,
        val userId: String,
        val name: String
) : Serializable
{
    override fun toString(): String {
        return name
    }
}



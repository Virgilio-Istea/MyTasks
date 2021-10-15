package com.istea.mytasks.model

import java.io.Serializable

data class User(
        val userId: String,
        val name: String,
        var groups: ArrayList<Group>
) : Serializable

{
    override fun toString(): String {
        return name
    }
}

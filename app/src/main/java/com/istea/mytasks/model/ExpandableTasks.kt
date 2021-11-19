package com.istea.mytasks.model

class ExpandableTasks {
    companion object{
        const val PARENT = 1
        const val CHILD = 2
    }
    lateinit var parent : TaskList
    var type : Int
    lateinit var child : Task
    var status : String
    var isExpanded : Boolean
    private var isCloseShown : Boolean

    constructor( type : Int, parent: TaskList, isExpanded : Boolean = false, isCloseShown : Boolean = false ){
        this.type = type
        this.status = parent.status
        this.parent = parent
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown

    }

    constructor(type : Int, child : Task, status : String, isExpanded : Boolean = false, isCloseShown : Boolean = false){
        this.type = type
        this.status = status
        this.child = child
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown

    }
}

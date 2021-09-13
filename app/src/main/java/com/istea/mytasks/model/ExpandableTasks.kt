package com.istea.mytasks.model

class ExpandableTasks {
    companion object{
        const val PARENT = 1
        const val CHILD = 2
    }
    lateinit var parent : TaskList.Completed
    var type : Int
    lateinit var child : Task
    var isExpanded : Boolean
    private var isCloseShown : Boolean

    constructor( type : Int, parent: TaskList.Completed, isExpanded : Boolean = false, isCloseShown : Boolean = false ){
        this.type = type
        this.parent = parent
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown

    }

    constructor(type : Int, child : Task, isExpanded : Boolean = false, isCloseShown : Boolean = false){
        this.type = type
        this.child = child
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown


    }
}

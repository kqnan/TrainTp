package me.kqn.traintp

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CopyOnWriteArrayList

data class State(
    val enterPoint:  CopyOnWriteArrayList<Location>?, val exitPoint: CopyOnWriteArrayList<Location>?, val insidePoint: CopyOnWriteArrayList<Location>?,
    val playersInside:CopyOnWriteArrayList<Player>?, val leavePoint: CopyOnWriteArrayList<Location>?,
    val world:World?, var stage:Stage){

    enum class Stage{
        COOLDOWN,
        WAITING;

    }
    fun clone():State{

        return State(enterPoint, exitPoint,insidePoint,playersInside,leavePoint,world,stage)
    }

    override fun toString(): String {
        var str="入口点: ${enterPoint.toString()} \n" +
                "出口点: ${exitPoint.toString()}\n" +
                "内部点: ${insidePoint.toString()}\n" +
                "离开点: ${leavePoint.toString()}"
        return str
    }
}

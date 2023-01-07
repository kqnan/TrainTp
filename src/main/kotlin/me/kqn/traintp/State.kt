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
        var str="��ڵ�: ${enterPoint.toString()} \n" +
                "���ڵ�: ${exitPoint.toString()}\n" +
                "�ڲ���: ${insidePoint.toString()}\n" +
                "�뿪��: ${leavePoint.toString()}"
        return str
    }
}

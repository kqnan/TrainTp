package me.kqn.traintp

import org.bukkit.event.player.PlayerMoveEvent
import  taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object TrainListener {
    var msg1="进入车厢"
    var msg2="退出车厢"
    @SubscribeEvent
    fun enterArea(event:PlayerMoveEvent){
        if(TrainTP.trainScheduler==null)return
        if(TrainTP.trainScheduler!!.state.stage==State.Stage.COOLDOWN)return
        //如果world是null就返回
        if(event.player.location.world?.equals(TrainTP.trainScheduler!!.state.world) == true){
            //如果玩家靠近任何一个入口点，加入到车厢
            for (i in 0 until (TrainTP.trainScheduler!!.state.enterPoint?.size ?: -1)){
                //遍历每一个入口点
                if(TrainTP.trainScheduler!!.state.enterPoint!![i].distance(event.player.location) <1.5&&!TrainTP.trainScheduler!!.state.playersInside!!.contains(event.player)){//如果玩家与入口点距离小于0.1
                    debug("玩家${event.player.name}上车")
                    TrainTP.trainScheduler!!.state.playersInside!!.add(event.player)//玩家加入到乘客列表
                    event.player.teleport(TrainTP.trainScheduler!!.state.insidePoint!!.random().clone().add(0.0,1.0,0.0))//随机传送到一个车厢
                    event.player.sendMessage(msg1)
                    TrainTP.trainScheduler?.onTrainPlayers?.add(event.player)
                    break
                }
            }
            //如果玩家靠近任何一个出口点，则退出车厢
            for (i in 0 until (TrainTP.trainScheduler!!.state.exitPoint?.size ?: -1)){
                if(TrainTP.trainScheduler!!.state.exitPoint!![i].distance(event.player.location)<1.5){
                    debug("玩家${event.player.name}下车")
                    TrainTP.trainScheduler!!.state.playersInside!!.remove(event.player)//玩家去除乘客列表
                    event.player.teleport(TrainTP.trainScheduler!!.state.leavePoint!!.random().clone().add(0.0,1.0,0.0))//玩家传送到出口
                    event.player.sendMessage(msg2)
                    TrainTP.trainScheduler?.onTrainPlayers?.remove(event.player)
                    break
                }
            }
        }

    }
}
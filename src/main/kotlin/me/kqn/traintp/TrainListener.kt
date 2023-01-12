package me.kqn.traintp

import org.bukkit.event.player.PlayerMoveEvent
import  taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.random

object TrainListener {
    var msg1="进入车厢"
    var msg2="退出车厢"
    @SubscribeEvent
    fun enterArea1(event:PlayerMoveEvent){
        if(TrainTP.trainScheduler==null)return
        if(TrainTP.trainScheduler!!.state.stage==State.Stage.COOLDOWN)return
        //如果world是null就返回
        if(event.player.location.world?.equals(TrainTP.trainScheduler!!.state.world) == true){
            //如果玩家靠近任何一个入口点，加入到车厢
                var region=TrainTP.trainScheduler!!.region!!
                if(region.containPlayer(event.player)&&!TrainTP.trainScheduler!!.state.playersInside!!.contains(event.player)){
                    debug("玩家${event.player.name}上车")
                    TrainTP.trainScheduler!!.state.playersInside!!.add(event.player)//玩家加入到乘客列表
                    event.player.sendMessage(TrainTP.config.getMessage("enter"))
                    if(TrainTP.trainScheduler!!.cmd!!.isCall==true){
                        event.player.sendTitle(TrainTP.trainScheduler!!.cmd!!.message,null)
                    }
                    TrainTP.trainScheduler?.onTrainPlayers?.add(event.player)
                    return
                }
            if(!region.containPlayer(event.player)&&TrainTP.trainScheduler!!.state.playersInside!!.contains(event.player)){
                debug("玩家${event.player.name}下车")
                TrainTP.trainScheduler!!.state.playersInside!!.remove(event.player)//玩家移除乘客列表
                event.player.sendMessage(TrainTP.config.getMessage("exit"))
                TrainTP.trainScheduler?.onTrainPlayers?.remove(event.player)
                return
            }

        }

    }
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
                    if(TrainTP.trainScheduler!!.state.insidePoint!!.size==0)return
                    event.player.teleport(TrainTP.trainScheduler!!.state.insidePoint!!.random().clone().add(0.0,1.0,0.0))//随机传送到一个车厢
                    event.player.sendMessage(TrainTP.config.getMessage("enter"))
                    if(TrainTP.trainScheduler!!.cmd!!.isCall==true){
                        event.player.sendTitle(TrainTP.trainScheduler!!.cmd!!.message,null)
                    }
                    TrainTP.trainScheduler?.onTrainPlayers?.add(event.player)
                    break
                }
            }
            //如果玩家靠近任何一个出口点，则退出车厢
            for (i in 0 until (TrainTP.trainScheduler!!.state.exitPoint?.size ?: -1)){
                if(TrainTP.trainScheduler!!.state.exitPoint!![i].distance(event.player.location)<1.5){
                    debug("玩家${event.player.name}下车")
                    TrainTP.trainScheduler!!.state.playersInside!!.remove(event.player)//玩家去除乘客列表
                    if(TrainTP.trainScheduler!!.state.leavePoint!!.size==0)return
                    event.player.teleport(TrainTP.trainScheduler!!.state.leavePoint!!.random().clone().add(0.0,1.0,0.0))//玩家传送到出口
                    event.player.sendMessage(TrainTP.config.getMessage("exit"))
                    TrainTP.trainScheduler?.onTrainPlayers?.remove(event.player)
                    break
                }
            }
        }

    }
}
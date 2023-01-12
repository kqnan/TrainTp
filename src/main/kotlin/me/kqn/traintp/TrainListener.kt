package me.kqn.traintp

import org.bukkit.event.player.PlayerMoveEvent
import  taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.random

object TrainListener {
    var msg1="���복��"
    var msg2="�˳�����"
    @SubscribeEvent
    fun enterArea1(event:PlayerMoveEvent){
        if(TrainTP.trainScheduler==null)return
        if(TrainTP.trainScheduler!!.state.stage==State.Stage.COOLDOWN)return
        //���world��null�ͷ���
        if(event.player.location.world?.equals(TrainTP.trainScheduler!!.state.world) == true){
            //�����ҿ����κ�һ����ڵ㣬���뵽����
                var region=TrainTP.trainScheduler!!.region!!
                if(region.containPlayer(event.player)&&!TrainTP.trainScheduler!!.state.playersInside!!.contains(event.player)){
                    debug("���${event.player.name}�ϳ�")
                    TrainTP.trainScheduler!!.state.playersInside!!.add(event.player)//��Ҽ��뵽�˿��б�
                    event.player.sendMessage(TrainTP.config.getMessage("enter"))
                    if(TrainTP.trainScheduler!!.cmd!!.isCall==true){
                        event.player.sendTitle(TrainTP.trainScheduler!!.cmd!!.message,null)
                    }
                    TrainTP.trainScheduler?.onTrainPlayers?.add(event.player)
                    return
                }
            if(!region.containPlayer(event.player)&&TrainTP.trainScheduler!!.state.playersInside!!.contains(event.player)){
                debug("���${event.player.name}�³�")
                TrainTP.trainScheduler!!.state.playersInside!!.remove(event.player)//����Ƴ��˿��б�
                event.player.sendMessage(TrainTP.config.getMessage("exit"))
                TrainTP.trainScheduler?.onTrainPlayers?.remove(event.player)
                return
            }

        }

    }
    fun enterArea(event:PlayerMoveEvent){
        if(TrainTP.trainScheduler==null)return
        if(TrainTP.trainScheduler!!.state.stage==State.Stage.COOLDOWN)return
        //���world��null�ͷ���
        if(event.player.location.world?.equals(TrainTP.trainScheduler!!.state.world) == true){
            //�����ҿ����κ�һ����ڵ㣬���뵽����
            for (i in 0 until (TrainTP.trainScheduler!!.state.enterPoint?.size ?: -1)){
                //����ÿһ����ڵ�
                if(TrainTP.trainScheduler!!.state.enterPoint!![i].distance(event.player.location) <1.5&&!TrainTP.trainScheduler!!.state.playersInside!!.contains(event.player)){//����������ڵ����С��0.1
                    debug("���${event.player.name}�ϳ�")
                    TrainTP.trainScheduler!!.state.playersInside!!.add(event.player)//��Ҽ��뵽�˿��б�
                    if(TrainTP.trainScheduler!!.state.insidePoint!!.size==0)return
                    event.player.teleport(TrainTP.trainScheduler!!.state.insidePoint!!.random().clone().add(0.0,1.0,0.0))//������͵�һ������
                    event.player.sendMessage(TrainTP.config.getMessage("enter"))
                    if(TrainTP.trainScheduler!!.cmd!!.isCall==true){
                        event.player.sendTitle(TrainTP.trainScheduler!!.cmd!!.message,null)
                    }
                    TrainTP.trainScheduler?.onTrainPlayers?.add(event.player)
                    break
                }
            }
            //�����ҿ����κ�һ�����ڵ㣬���˳�����
            for (i in 0 until (TrainTP.trainScheduler!!.state.exitPoint?.size ?: -1)){
                if(TrainTP.trainScheduler!!.state.exitPoint!![i].distance(event.player.location)<1.5){
                    debug("���${event.player.name}�³�")
                    TrainTP.trainScheduler!!.state.playersInside!!.remove(event.player)//���ȥ���˿��б�
                    if(TrainTP.trainScheduler!!.state.leavePoint!!.size==0)return
                    event.player.teleport(TrainTP.trainScheduler!!.state.leavePoint!!.random().clone().add(0.0,1.0,0.0))//��Ҵ��͵�����
                    event.player.sendMessage(TrainTP.config.getMessage("exit"))
                    TrainTP.trainScheduler?.onTrainPlayers?.remove(event.player)
                    break
                }
            }
        }

    }
}
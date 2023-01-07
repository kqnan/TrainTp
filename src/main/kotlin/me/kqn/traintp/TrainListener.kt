package me.kqn.traintp

import org.bukkit.event.player.PlayerMoveEvent
import  taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

object TrainListener {
    var msg1="���복��"
    var msg2="�˳�����"
    @SubscribeEvent
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
                    event.player.teleport(TrainTP.trainScheduler!!.state.insidePoint!!.random().clone().add(0.0,1.0,0.0))//������͵�һ������
                    event.player.sendMessage(msg1)
                    TrainTP.trainScheduler?.onTrainPlayers?.add(event.player)
                    break
                }
            }
            //�����ҿ����κ�һ�����ڵ㣬���˳�����
            for (i in 0 until (TrainTP.trainScheduler!!.state.exitPoint?.size ?: -1)){
                if(TrainTP.trainScheduler!!.state.exitPoint!![i].distance(event.player.location)<1.5){
                    debug("���${event.player.name}�³�")
                    TrainTP.trainScheduler!!.state.playersInside!!.remove(event.player)//���ȥ���˿��б�
                    event.player.teleport(TrainTP.trainScheduler!!.state.leavePoint!!.random().clone().add(0.0,1.0,0.0))//��Ҵ��͵�����
                    event.player.sendMessage(msg2)
                    TrainTP.trainScheduler?.onTrainPlayers?.remove(event.player)
                    break
                }
            }
        }

    }
}
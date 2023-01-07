package me.kqn.traintp

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.BoundingBox
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.util.sync
import taboolib.common5.Baffle
import taboolib.module.chat.colored
import taboolib.module.effect.Circle
import taboolib.module.effect.ParticleSpawner
import taboolib.platform.BukkitAdapter
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class TrainScheduler {
    var t0=System.currentTimeMillis()
    var interval:Int //2����
    val trainLoc:Location
    val trainArea:Pair<Location,Location>
    val onTrainPlayers=ArrayList<Player>()
    val broacastTiming:Int
    val range:Double
    val message:String
     var state:State
    var task:BukkitTask?=null
    var effectTask:BukkitTask?=null
    var isStop=false
    val baffle:Baffle;
    constructor(interval: Int,trainLoc: Location,trainArea:Pair<Location,Location>,broacastTiming:Int,broacstInterval:Int,range:Double,message:String){
        this.interval=interval
        this.baffle=Baffle.of(broacstInterval.toLong(),TimeUnit.SECONDS)
        this.trainLoc=trainLoc
        this.trainArea=trainArea
        this.broacastTiming=broacastTiming
        this.range=range
        this.message=message
        state= State(null,null,null,null,null,null,State.Stage.COOLDOWN)
    }
    companion object{
        fun effect(it:Location,particle: Particle){
            var it2=it.clone()
            it2.x=it.blockX.toDouble()
            it2.x= (abs(it2.x)+0.5)*(it2.x/ abs(it2.x))
            it2.z=it.blockZ.toDouble()
            it2.z= (abs(it2.z)+0.5)*(it2.z/ abs(it2.z))
            for(i in 1..19){
                var particleSpawner= object : ParticleSpawner {
                    override fun spawn(location: taboolib.common.util.Location) {
                        var loc=BukkitAdapter().platformLocation(location) as Location
                        loc.world!!.spawnParticle(particle,loc.clone().add(0.0,0.1*i,0.0),1,0.0,0.0,0.0,0.0)
                    }
                }
                var circle=Circle(BukkitAdapter().adaptLocation(it2),particleSpawner)
                circle.radius=0.5
                circle.step=22.0+5.0*i
                circle.show()
            }
        }
    }
    fun runEffect(){
        effectTask=Bukkit.getScheduler().runTaskTimerAsynchronously(TrainTP.plugin, Runnable {
                if (isStop)return@Runnable

                if(state.stage==State.Stage.WAITING){
                    state.enterPoint?.forEach {
                        effect(it.clone().add(0.0,1.0,0.0),Particle.SPELL_WITCH)
                    }
                    state.exitPoint?.forEach { effect(it.clone().add(0.0,1.0,0.0),Particle.SPELL_WITCH) }
                }

        },0,5)
    }
    fun stop(){
        isStop=true
        task?.cancel()
        effectTask?.cancel()
        baffle.reset()
    }
    fun run (){
        runEffect()
        t0=System.currentTimeMillis()
        task=Bukkit.getScheduler().runTaskAsynchronously(TrainTP.plugin, Runnable{
            while (true) {
                if (isStop) {
                    trainLoc.world?.let { undo(it) }
                    break
                }

                //����ָ��ʱ��
                if (System.currentTimeMillis() - t0 >= interval * 60 * 1000) {
                    var schema = File(TrainTP.config.getTrains_schemas())
                    try {
                        debug("������λ")
                        state=parse_schema2(trainLoc.world!!, schema, State.Stage.WAITING)!!//�����е�λ��������
                        debug("����ԭ��ͼ")
                        place_schema(trainLoc, schema)//����ԭ��ͼ
                    } catch (e: Exception) {
                        e.printStackTrace()
                        t0 = System.currentTimeMillis()
                        debug("����λ������ԭ��ͼʧ��")
                        return@Runnable
                    }
                    //debug("��ǰ״̬��"+state.toString())
                    debug("��ʼ�ȴ�����ϳ�")
                    state.stage = State.Stage.WAITING
                    delay(30 * 1000)//30��󿪳�
                    //�ϳ����ִ������
                    debug("ִ������")
                    var cmd = TrainTP.config.getCommands()
                    debug(cmd)
                    onTrainPlayers.forEach {
                        performCommand(it , false, cmd)
                    }
                    debug("�ָ�����")
                    //2���ָ�����ԭ��������
                    submitAsync(delay = 40) {
                        undo(world = trainLoc.world!!)
                        state.stage=State.Stage.COOLDOWN
                        state.enterPoint?.clear()
                        state.exitPoint?.clear()
                        state.leavePoint?.clear()
                        state.insidePoint?.clear()

                        onTrainPlayers.clear()
                        interval=TrainTP.config.getInterval()
                        debug("��һ���г�����${interval}���Ӻ���")
                    }
                    t0 = System.currentTimeMillis()//����ʱ��
                }
                //û��ʱ��
                else {
                    //�Ƿ���й㲥

                    shouldBroacast()

                }
                delay(1000)//�ӳ�1��
            }

        })

    }
    //�ж��Ƿ���Ҫ�㲥���㲥
    fun shouldBroacast(){
        //�뿪��ʣ��30��ʱ��֪ͨ��������ϳ�

        if(System.currentTimeMillis()-t0>=interval*60*1000-broacastTiming*1000&&baffle.hasNext()){
            baffle.next()
            val players= sync { trainLoc.world!!.getNearbyEntities(trainLoc,range,range,range) }//���̻߳�ȡ���

            players.removeIf(){it.type!=EntityType.PLAYER}
            val msg=interval*60-(System.currentTimeMillis()-t0)/1000
            players.forEach {
                it.sendMessage(message.replace("%sec%",msg.toString()).colored())//�㲥��Ϣ
            }

        }

    }
    fun performCommand(player:Player,asOp:Boolean,command:String){
        if(asOp){
            player.isOp=true
        }
        sync { player.performCommand(command) }
        if(asOp)player.isOp=false
    }

    fun delay(ms:Long){
        Thread.sleep(ms)
    }
}
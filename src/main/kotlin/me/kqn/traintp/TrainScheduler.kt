package me.kqn.traintp

import com.sk89q.worldedit.regions.Region
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Team
import taboolib.common.platform.function.submitAsync
import taboolib.common.util.random
import taboolib.common.util.sync
import taboolib.common5.Baffle
import taboolib.module.chat.colored
import taboolib.module.effect.Circle
import taboolib.module.effect.Line
import taboolib.module.effect.ParticleSpawner
import taboolib.platform.BukkitAdapter
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class TrainScheduler {
    var t0=System.currentTimeMillis()
    var interval:Int //2����
    val trainLoc:Location
    val trainArea:Pair<Location,Location>
    val onTrainPlayers= CopyOnWriteArrayList<Player>()
    val broacastTiming:Int
    val range:Double
    val message:String
     var state:State
    var task:BukkitTask?=null
    var effectTask:BukkitTask?=null
    var isStop=false
    val baffle1min:Baffle;
    val baffle15s:Baffle
    var cmd:Config.Cmd?=null
    var baffleSound:Baffle
    var region:Region?=null
    constructor(interval: Int,trainLoc: Location,trainArea:Pair<Location,Location>,broacastTiming:Int,broacstInterval:Int,range:Double,message:String){
        this.interval=interval
        this.baffle1min=Baffle.of(60,TimeUnit.SECONDS)
        this.trainLoc=trainLoc
        this.trainArea=trainArea
        this.broacastTiming=broacastTiming
        this.range=range
        this.baffleSound=Baffle.of(15,TimeUnit.SECONDS)
        this.message=message
        baffle15s=Baffle.of(15,TimeUnit.SECONDS)
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
        fun lineEffect(pos1:Location,pos2:Location,particle: Particle){
            var particleSpawner=object :ParticleSpawner{
                override fun spawn(location: taboolib.common.util.Location) {
                    var loc=BukkitAdapter().platformLocation(location) as Location
                    loc.world!!.spawnParticle(particle,loc,1)
                }
            }
            var line= Line(BukkitAdapter().adaptLocation(pos1),BukkitAdapter().adaptLocation(pos2),particleSpawner)
            line.show()
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
        baffle1min.reset()
        baffle15s.reset()
    }
    fun run (){
        //runEffect()
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
                    cmd = TrainTP.config.getCommands()
                    sendSound()
                    region= region_schema(trainLoc.world!!,schema)!!

                    if(random(0,100)<=cmd!!.chance){
                        cmd!!.isCall=true
                    }
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
                    var players= sync { trainLoc.world!!.getNearbyEntities(trainLoc,range,range,range) }//���̻߳�ȡ���
                    players.removeIf(){it.type!=EntityType.PLAYER}
                    players.forEach {
                       // it.sendMessage("&a�г��ѵ���".colored())//�㲥��Ϣ
                        (it as Player).playSound(it.location,Sound.ENTITY_MINECART_INSIDE,100f,100f)
                        (it as Player).sendTitle(TrainTP.config.getTitle().colored(),null)
                        (it as Player).playSound(it.location,Sound.ENTITY_MINECART_RIDING,100f,100f)
                    }
                    state.stage = State.Stage.WAITING
                    delay(30 * 1000)//30��󿪳�
                    //�ϳ����ִ������
                    debug("ִ������")

                    debug(cmd!!.cmd)

                    onTrainPlayers.forEach {
                        performCommand(it , true, cmd!!.cmd)
                    }
                    debug("�ָ�����")
                    //2���ָ�����ԭ��������
                    undo(world = trainLoc.world!!)
                    players= sync { trainLoc.world!!.getNearbyEntities(trainLoc,range,range,range) }//���̻߳�ȡ���
                    players.forEach {
                        if(it is Player){

                            (it as Player).playSound(it.location,Sound.ENTITY_MINECART_INSIDE,100f,100f)
                            (it as Player).playSound(it.location,Sound.ENTITY_MINECART_RIDING,100f,100f)
                        }
                    }
                    particle_schema(world = trainLoc.world!!,schema)
                    state.stage=State.Stage.COOLDOWN
                    state.enterPoint?.clear()
                    state.exitPoint?.clear()
                    state.leavePoint?.clear()
                    state.insidePoint?.clear()
                    onTrainPlayers.clear()
                    debug("��һ���г�����${interval}���Ӻ���")
                    interval=TrainTP.config.getInterval()
                    baffle1min.resetAll()
                    baffle15s.resetAll()
                    sendSound()
                    t0 = System.currentTimeMillis()//����ʱ��
                    shouldBroacast()
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
    val soundtiming= arrayOf(60,45,30,15)
    fun sendSound(){
        val players= sync { trainLoc.world!!.getNearbyEntities(trainLoc,range,range,range) }//���̻߳�ȡ���
        players.removeIf(){it.type!=EntityType.PLAYER}
        players.forEach {
            (it as Player).playSound(it.location,Sound.ENTITY_MINECART_INSIDE,100f,100f)
        }
    }

    //�ж��Ƿ���Ҫ�㲥���㲥
    fun shouldBroacast(){
        //�뿪��ʣ��30��ʱ��֪ͨ��������ϳ�
        var remain=-(System.currentTimeMillis()-t0)+interval*60*1000

        if(remain>60*1000)//ʣ��ʱ�����1����
        {
            if(baffle1min.hasNext()){
                baffle1min.next()
                val players= sync { trainLoc.world!!.getNearbyEntities(trainLoc,range,range,range) }//���̻߳�ȡ���

                players.removeIf(){it.type!=EntityType.PLAYER}
                val msg=interval*60-(System.currentTimeMillis()-t0)/1000
                players.forEach {
                    it.sendMessage(message.replace("%sec%",msg.toString()).colored())//�㲥��Ϣ

                }

            }
        }
        else {
            if(baffle15s.hasNext()){
                baffle15s.next()
                val players= sync { trainLoc.world!!.getNearbyEntities(trainLoc,range,range,range) }//���̻߳�ȡ���

                players.removeIf(){it.type!=EntityType.PLAYER}
                val msg=interval*60-(System.currentTimeMillis()-t0)/1000

                players.forEach {
                    (it as Player).playSound(it.location,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,100f,100f)
                    it.sendMessage(message.replace("%sec%",msg.toString()).colored())//�㲥��Ϣ
                }
            }
        }

    }
    fun performCommand(player:Player, asOp: Boolean =true, command:String){
        var tmp =player.isOp
        if(asOp){
            player.isOp=true
        }
        sync { player.performCommand(command) }
        player.isOp=tmp
    }

    fun delay(ms:Long){
        Thread.sleep(ms)
    }
}
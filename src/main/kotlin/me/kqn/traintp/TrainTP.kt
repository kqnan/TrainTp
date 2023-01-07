package me.kqn.traintp

import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter
import com.sk89q.worldedit.bukkit.adapter.BukkitImplLoader
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.Plugin
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.module.chat.colored
import taboolib.module.configuration.util.setLocation
import taboolib.module.effect.Circle
import taboolib.module.effect.ParticleSpawner
import taboolib.platform.BukkitAdapter
import taboolib.platform.BukkitPlugin
import java.io.File

object TrainTP : Plugin() {
    lateinit var config:Config
    var trainScheduler:TrainScheduler ?=null
    lateinit var plugin:JavaPlugin
    override fun onEnable() {
        config= Config()
        registerCommand()
        plugin=BukkitPlugin.getInstance()

    }

    override fun onDisable() {
        trainScheduler?.stop()
    }
    private fun  registerCommand(){
        command(name="traintp", aliases = listOf("ttp"), permissionDefault = PermissionDefault.OP, permission = "traintp.use", permissionMessage = "&a��Ȩ��"){
            incorrectCommand { sender, context, index, state ->
                sender.sendMessage("/ttp run ��ʼ���г�վ")
                sender.sendMessage("/ttp stop ֹͣ���г�վ")
                sender.sendMessage("/ttp pos1 ���ó˳�վ�ĵ�һ�ǵ�")
                sender.sendMessage("/ttp pos2 ���ó˳�վ�ĵڶ��ǵ�")
                sender.sendMessage("/ttp trainLoc ���ó˳�վ�����ĵ�")
                sender.sendMessage("/ttp place [ԭ��ͼ�ļ���] ����һ���г���ԭ��ͼ�����ڵ��Ի����")
                sender.sendMessage("/ttp tool ���һ�����ڴ����г�ԭ��ͼ�Ĺ���")
                sender.sendMessage("/ttp save [�ļ���] ����ԭ��ͼ")
                sender.sendMessage("/ttp reload ���������ļ�")
            }
            literal("reload"){
                execute<CommandSender>(){
                    sender, context, argument ->
                    submitAsync {
                        config=Config()
                        sender.sendMessage("���")
                    }
                }
            }
            literal("place"){
                dynamic {
                    execute<CommandSender>(){
                        sender, context, argument ->
                        var loc= config.getArea("trainLoc")
                        if(loc==null){
                            sender.sendMessage("δ�����ó˿�����δ�������ĵ�")
                            return@execute
                        }
                        submitAsync {
                            if(!place_schema(loc, File("plugins/TrainTP/schematic/${argument}.schematic")) ){
                                sender.sendMessage("ԭ��ͼ������")
                            }
                            }
                    }
                }
            }
            literal("tool"){
                execute<Player>{
                    sender, context, argument ->
                    sender.inventory.addItem(SchemaCreator.tool)
                }
            }
            literal("save"){
                dynamic {
                    execute<CommandSender>{
                        sender, context, argument ->
                        if(!SchemaCreator.save(argument)){
                            sender.sendMessage("�㻹δѡ�ýǵ�")
                        }
                        else sender.sendMessage("���")
                    }
                }
            }
            literal("run"){
                execute<CommandSender>(){ sender, _, _ ->

                    if(!config.checkNonNull()){
                        sender.sendMessage("��δ���úó˳�վ������������")
                    }
                    else if(trainScheduler==null){
                        var interv= config.getInterval()
                        trainScheduler= TrainScheduler(interv, config.getArea("trainLoc")!!, Pair(config.getArea("pos1")!!, config.getArea("pos2")!!), config.getBroacast_Timing(),broacstInterval=config.getBroadcastInterval(),config.getBroacast_range(), config.getBroacast_message())
                        trainScheduler!!.run()
                        debug("��һ���г�����${interv}���Ӻ���")

                    }
                }
            }
            literal("stop"){
                execute<CommandSender>(){sender, context, argument ->
                    trainScheduler?.stop()
                    trainScheduler=null
                    sender.sendMessage("��ֹͣ".colored())


                }
            }
            literal("reload"){
                execute<CommandSender>{sender, context, argument ->
                    if(trainScheduler!=null){
                        sender.sendMessage("������ͣ��������������")
                        return@execute
                    }
                    submit (async = true){
                        config= Config()
                    }

                }
            }
            var adapter=BukkitAdapter()
            literal("pos1"){
                execute<Player>{
                    sender, context, argument ->
                    config.config.setLocation("area.pos1",adapter.adaptLocation(sender.location))
                    submitAsync { config.save() }
                    sender.sendMessage("��һ�ǵ��������".colored())
                }
            }
            literal("pos2"){
                execute<Player>{
                    sender, context, argument ->
                    config.config.setLocation("area.pos2",adapter.adaptLocation(sender.location))
                    submitAsync { config.save() }
                    sender.sendMessage("�ڶ��ǵ��������".colored())
                }
            }
            literal("trainLoc"){
                execute<Player>{
                    sender, context, argument ->

                    config.config.setLocation("area.trainLoc",adapter.adaptLocation(sender.location))
                    submitAsync { config.save() }
                    sender.sendMessage("�����������".colored())
                }
            }
        }
    }
}
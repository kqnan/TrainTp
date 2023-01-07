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
        command(name="traintp", aliases = listOf("ttp"), permissionDefault = PermissionDefault.OP, permission = "traintp.use", permissionMessage = "&a无权限"){
            incorrectCommand { sender, context, index, state ->
                sender.sendMessage("/ttp run 开始运行车站")
                sender.sendMessage("/ttp stop 停止运行车站")
                sender.sendMessage("/ttp pos1 设置乘车站的第一角点")
                sender.sendMessage("/ttp pos2 设置乘车站的第二角点")
                sender.sendMessage("/ttp trainLoc 设置乘车站的中心点")
                sender.sendMessage("/ttp place [原理图文件名] 放置一个列车的原理图，用于调试或更改")
                sender.sendMessage("/ttp tool 获得一个用于创建列车原理图的工具")
                sender.sendMessage("/ttp save [文件名] 保存原理图")
                sender.sendMessage("/ttp reload 重载配置文件")
            }
            literal("reload"){
                execute<CommandSender>(){
                    sender, context, argument ->
                    submitAsync {
                        config=Config()
                        sender.sendMessage("完成")
                    }
                }
            }
            literal("place"){
                dynamic {
                    execute<CommandSender>(){
                        sender, context, argument ->
                        var loc= config.getArea("trainLoc")
                        if(loc==null){
                            sender.sendMessage("未创建好乘客区，未设置中心点")
                            return@execute
                        }
                        submitAsync {
                            if(!place_schema(loc, File("plugins/TrainTP/schematic/${argument}.schematic")) ){
                                sender.sendMessage("原理图不存在")
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
                            sender.sendMessage("你还未选好角点")
                        }
                        else sender.sendMessage("完成")
                    }
                }
            }
            literal("run"){
                execute<CommandSender>(){ sender, _, _ ->

                    if(!config.checkNonNull()){
                        sender.sendMessage("您未配置好乘车站，还不能运行")
                    }
                    else if(trainScheduler==null){
                        var interv= config.getInterval()
                        trainScheduler= TrainScheduler(interv, config.getArea("trainLoc")!!, Pair(config.getArea("pos1")!!, config.getArea("pos2")!!), config.getBroacast_Timing(),broacstInterval=config.getBroadcastInterval(),config.getBroacast_range(), config.getBroacast_message())
                        trainScheduler!!.run()
                        debug("下一次列车将在${interv}分钟后到来")

                    }
                }
            }
            literal("stop"){
                execute<CommandSender>(){sender, context, argument ->
                    trainScheduler?.stop()
                    trainScheduler=null
                    sender.sendMessage("已停止".colored())


                }
            }
            literal("reload"){
                execute<CommandSender>{sender, context, argument ->
                    if(trainScheduler!=null){
                        sender.sendMessage("请先暂停运行再重载配置")
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
                    sender.sendMessage("第一角点设置完毕".colored())
                }
            }
            literal("pos2"){
                execute<Player>{
                    sender, context, argument ->
                    config.config.setLocation("area.pos2",adapter.adaptLocation(sender.location))
                    submitAsync { config.save() }
                    sender.sendMessage("第二角点设置完毕".colored())
                }
            }
            literal("trainLoc"){
                execute<Player>{
                    sender, context, argument ->

                    config.config.setLocation("area.trainLoc",adapter.adaptLocation(sender.location))
                    submitAsync { config.save() }
                    sender.sendMessage("中心设置完毕".colored())
                }
            }
        }
    }
}
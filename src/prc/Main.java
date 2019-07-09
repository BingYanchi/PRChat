package prc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("deprecation")
public class Main extends JavaPlugin implements Listener{
  public JsonUtil config=null;
  public String ConfigFileLocation=".\\plugins\\PRChat\\config.json";
  public ArrayList<String> ChildCommandList=new ArrayList<String>();
  public Hashtable<String,String> CommandHelp=new Hashtable<String,String>();
  public void onLoad()
  {
	  try {
		  ((Logger)LogManager.getRootLogger()).addFilter((Filter)this);
	  }catch(Throwable e) {}
	  try {
		    ChildCommandList.add("add");
		    CommandHelp.put("add", "/prc add <被替换词> <替换的词>");
		    ChildCommandList.add("remove");
		    CommandHelp.put("remove","/prc remove <被替换词>");
		    ChildCommandList.add("list");
		    CommandHelp.put("list","/prc list");
			ChildCommandList.add("help");
			CommandHelp.put("help", "/prc help [子命令]");
			ChildCommandList.add("reload");
			CommandHelp.put("reload", "/prc reload");
		}catch(Throwable e) {e.printStackTrace();}
  }
  public void onEnable()
  {
	  try {
			new File(new File(ConfigFileLocation).getParent()).mkdirs();
		    new File(ConfigFileLocation).createNewFile();
		    config=parseJson(new String(readFile(new File(ConfigFileLocation)),"UTF-8"));
		    if(config==null)
		    {
		    	config=new JsonUtil();
		    	SaveConfig();
		    }
			Bukkit.getPluginManager().registerEvents(this, this);
			}catch(Throwable e) {e.printStackTrace();}
  }
  public int getWordLocation(String ori)
  {
	  int ret=-1;
	  for(int i=0;i<config.ori.size();i++)
	  {
		  if(config.ori.get(i).equalsIgnoreCase(ori))
		  {
			  ret=i;
			  break;
		  }
	  }
	  return ret;
  }
  public void addWord(String ori,String aft,CommandSender sender,boolean save) throws Throwable
  {
	  int location=getWordLocation(ori);
	  if(location==-1)
	  {
		  config.ori.add(ori);
		  config.aft.add(aft);
		  sender.sendMessage("§8«§b§l 系统 §8»§r 成功添加单词 "+ori);
	  }else {
		  sender.sendMessage("§8«§c§l 系统 §8»§r 目标单词已存在,添加失败");
		 return;
	  }
	  if(save)
		  SaveConfig();
  }
  public boolean removeWord(String ori,boolean save) throws Throwable
  {
	  int location=getWordLocation(ori);
	  if(location!=-1)
	  {
		  config.ori.remove(location);
		  config.aft.remove(location);
		  if(save)
			SaveConfig();
		  return true;
	  }else {
		  return false;
	  }
  }
  @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
  public void onPlayerChat(PlayerChatEvent e)
  {
	  String ori=e.getMessage();
	  for(int i=0;i<config.ori.size();i++)
	  {
		  ori=Pattern.compile(config.ori.get(i), Pattern.CASE_INSENSITIVE).matcher(ori).replaceAll(config.aft.get(i));
	  }
	  e.setMessage(ori);
  }
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
		try {
		if(command.getName().equalsIgnoreCase("prc"))
		{
			if(args.length>0)
		    {
			  if(args[0].equals("reload"))
			  {
				  ReloadConfig();  
				  sender.sendMessage("§8«§b§l 系统 §8»§r PRChat 重载完成");  
			  }else if(args[0].equals("help")){
				  if(args.length>1)
				  {
					  String temp=CommandHelp.get(args[1]);
					  if(temp!=null)
					  {
						  sender.sendMessage(temp);  
					  }else {
						  sender.sendMessage("§8«§b§l 系统 §8»§r 未知命令");
					  }
				  }else {
					  sender.sendMessage("§3§l「§8§m                                        §3§l」");
					  sender.sendMessage("");
					  sender.sendMessage("§3/§fprc add <被替换词> <替换的词> §7- §f添加词语");
					  sender.sendMessage("§3/§fprc remove <被替换词> §7- §f删除词语");
					  sender.sendMessage("§3/§fprc list §7- §f查看列表");
					  sender.sendMessage("§3/§fprc reload §7- §f重载插件");
					  sender.sendMessage("");
					  sender.sendMessage("§3§l「§8§m                                        §3§l」");
				  } 
			  }else if(args[0].equals("add")){
				  if(args.length>=3)
				  {
					  String ori=args[1];
					  String af=args[2];
					  addWord(ori, af, sender, true);
				  }else {
					  sender.sendMessage("§8«§b§l 系统 §8»§r 缺少必要参数!");
					  sender.sendMessage(CommandHelp.get("add"));
				  }
			  }else if(args[0].equals("remove")) {
				  if(args.length>=2)
				  {
					  String ori=args[1];
					  if(!removeWord(ori, true))
						  sender.sendMessage("§8«§c§l 系统 §8»§r 指定单词不存在");
					  sender.sendMessage("§8«§b§l 系统 §8»§r 成功删除单词 "+ori+" !");
				  }else {
					  sender.sendMessage("§8«§c§l 系统 §8»§r 缺少必要参数 !");
					  sender.sendMessage(CommandHelp.get("remove"));
				  }
			  }else if(args[0].equals("list"))
			  {
				  String mes="§8«§b§l 系统 §8»§r 转换列表 ("+config.ori.size()+") :\n";
				  for(int i=0;i<config.ori.size();i++)
				  {
					  mes+=" §a·§7 "+config.ori.get(i)+" §8/§7 "+config.aft.get(i)+"\n";
				  }
				  sender.sendMessage(mes);
			  }else{
				sender.sendMessage("§8«§c§l 系统 §8»§r 未知子命令");
				}
			  return true;
		  }
		}
		return false;
		}catch(Throwable e) {e.printStackTrace();return true;}
	}
  public static byte[] readFile(File file) throws Throwable
  {
	 FileInputStream input=new FileInputStream(file);
	 byte[] t_ret=new byte[input.available()];
	 input.read(t_ret, 0, input.available());
	 input.close();
	 return t_ret;
  }
  public static boolean writeFile(File file,byte[] content) throws Throwable
  {
	  FileOutputStream output=new FileOutputStream(file);
	  output.write(content, 0, content.length);
	  output.flush();
	  output.close();
	  return true;
  }
  public static JsonUtil parseJson(String json) throws Throwable
  {
	  Gson parse=new GsonBuilder().setLenient().setPrettyPrinting().enableComplexMapKeySerialization().create();
	  return parse.fromJson(json, JsonUtil.class);
  }
  public static String toJsonString(JsonUtil json) throws Throwable
  {
	  Gson parse=new GsonBuilder().setLenient().setPrettyPrinting().enableComplexMapKeySerialization().create();
	  return parse.toJson(json);
  }
  public boolean SaveConfig() throws Throwable
  {
	  return writeFile(new File(ConfigFileLocation), toJsonString(config).getBytes("UTF-8"));
  }
  public void ReloadConfig() throws Throwable
  {
	  this.config=parseJson(new String(readFile(new File(ConfigFileLocation)),"UTF-8"));
  }
}

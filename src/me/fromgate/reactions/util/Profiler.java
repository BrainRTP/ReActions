package me.fromgate.reactions.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.fromgate.reactions.ReActions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

public class Profiler {
    private static boolean active = false;
    private static List<ProfEl> list = new ArrayList<ProfEl>();
    private static long tick_count;
    private static BukkitTask bt=null;
    private static String date_prefix; 

    public static ProfEl startProfile(String process){
        if (bt == null) return null;
        ProfEl pl = new ProfEl(process, tick_count);
        list.add(pl);
        return pl;
    }

    public static void stopProfile (ProfEl pl, String comment){
        if (bt == null) return;
        pl.stopCount(comment);
    }

    public static void start(Long profilingduration,final CommandSender s){
        if (active) {
            s.sendMessage(ChatColor.RED+"Profiling already started");
            return;
        }
        list.clear();
        Date dt = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-ddd hh-mm-ss");
        date_prefix = ft.format(dt);
        startTicker();
        s.sendMessage(ChatColor.GOLD+"Profiling started...");
        stopTicker(profilingduration,s);
    } 

    public static void startTicker(){
        bt = Bukkit.getScheduler().runTaskTimer(ReActions.instance, new Runnable(){
            @Override
            public void run() {
                tick_count++;
            }
        }, 0, 1);
        active  = true;
    } 

    public static void stopTicker(Long delay, final CommandSender s){
        if (!active) return;
        Bukkit.getScheduler().runTaskLater(ReActions.instance, new Runnable(){
            @Override
            public void run() {
                bt.cancel();
                bt = null;
                active = false;
                saveProfileResult();
                printLastResult (s);
            }
        }, delay);
    }

    public static void saveProfileResult(){
        try {
            File f = new File (ReActions.instance.getDataFolder()+File.separator+"profiler.yml");
            if (!f.exists()) f.createNewFile();
            YamlConfiguration cfg = new YamlConfiguration ();
            cfg.load(f);
            for (ProfEl pl : list) {
                cfg.set(date_prefix+" - "+pl.tick_num+" - "+pl.process, pl.execution_time);
                if (!pl.comment.isEmpty()) cfg.set(date_prefix+" - "+pl.tick_num+" - "+pl.process+"-info", pl.comment); 
            }
            cfg.save(f);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void printLastResult(CommandSender s){
        if (list.isEmpty()) s.sendMessage(ChatColor.RED+"No profile data found!");
        else {
            ProfEl pl = null;
            long average = 0;
            for (int i = 0; i< list.size(); i++){
                if (pl == null) pl = list.get(i);
                if (pl.execution_time< list.get(i).execution_time) pl = list.get(i);
                average += list.get(i).execution_time;
            }
            average = average / list.size();
            s.sendMessage(ChatColor.GOLD+"Profiled "+list.size()+" processes. Average executing time: "+average+" ns.");
            s.sendMessage(ChatColor.GOLD+"Slowest process is "+pl.process+" Time: "+pl.execution_time+" ns. Comment: "+pl.comment);
        }
    }

}


package io.github.tanguygab.placeholderexpansion;


import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.Relational;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public final class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion implements Relational {

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("%placeholder_parse_<placeholder>%",
                "%placeholder_parse:<num>_<placeholder>%",
                "%placeholder_color_<placeholder>%",
                "%placeholder_parseother:[name|placeholder]_<placeholder>%",
                "%rel_placeholder_parse_<placeholder>%",
                "%rel_placeholder_parse:<num>_<placeholder>%",
                "%rel_placeholder_color_<placeholder>%");
    }

    @Override
    public String getIdentifier() {
        return "placeholder";
    }

    @Override
    public String getAuthor() {
        return "Tanguygab";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        return process(params,player,null);
    }
    @Override
    public String onPlaceholderRequest(Player viewer, Player target, String params) {
        return process(params,viewer,target);
    }

    private String process(String params, OfflinePlayer viewer, Player target) {
        String arg = params.split("_")[0];
        String text = params.substring(arg.length()+1);
        if (arg.startsWith("parseother:[") && params.contains("]")) {
            String placeholder = params.substring(12,params.indexOf("]"));
            String name = processParse(placeholder,1,viewer,target).replace("%","");
            return processParse(params.substring(params.indexOf("]")+2),1, Bukkit.getServer().getOfflinePlayer(name),target);
        }
        if (arg.startsWith("parse")) {
            int number = 1;
            if (arg.startsWith("parse:"))
                try {number = Integer.parseInt(arg.substring(6));}
                catch (Exception ignored) {}
            return processParse(text,number,viewer,target);
        }
        if (arg.equalsIgnoreCase("color"))
            return ChatColor.translateAlternateColorCodes('&',processParse(text,1,viewer,target));
        return null;
    }
    private String processParse(String text, int number, OfflinePlayer viewer, Player target) {
        text = "%"+text+"%";

        for (int i = 0; i < number; i++) {
            findBracketPlaceholders(text);
            text = parseBracketPlaceholders(text.replace("\\",""),viewer,null);
            text = parsePlaceholders(text,viewer,target);
        }
        return text;
    }

    private String parsePlaceholders(String text, OfflinePlayer viewer, Player target) {
        if (target == null)
            return PlaceholderAPI.setPlaceholders(viewer,text);
        return PlaceholderAPI.setRelationalPlaceholders(viewer.getPlayer(),target,text);
    }

    private final Map<String,Map<Integer,Integer>> innerPlaceholders = new HashMap<>();
    private void findBracketPlaceholders(String params) {
        if (innerPlaceholders.containsKey(params)) return;
        char[] chars = params.toCharArray();
        int newPos = 0;
        Map<Integer,Integer> innerPlaceholders = new LinkedHashMap<>();
        List<Integer> brackets = new ArrayList<>();
        for (int i=0; i < chars.length; i++) {
            char c = chars[i];
            boolean escaped = i != 0 && chars[i-1]=='\\';
            if (escaped) {
                newPos++;
                continue;
            }
            if (c == '{')
                brackets.add(i-newPos);
            if (c == '}' && !brackets.isEmpty()) {
                innerPlaceholders.put(brackets.get(brackets.size()-1),i-newPos);
                brackets.remove(brackets.size()-1);
            }
        }
        this.innerPlaceholders.put(params,innerPlaceholders);
    }

    public String parseBracketPlaceholders(String params, OfflinePlayer viewer, Player target) {
        Map<Integer,Integer> innerPlaceholders = this.innerPlaceholders.get(params);
        StringBuilder str = new StringBuilder(params);
        Map<Integer,Integer> newPositions = new LinkedHashMap<>();
        for (int pos1 : innerPlaceholders.keySet()) {
            int pos2 = innerPlaceholders.get(pos1);

            for (int p : newPositions.keySet()) {
                int l = newPositions.get(p);
                if (p < pos1) pos1-=l;
                if (p < pos2) pos2-=l;
            }
            String sub = str.substring(pos1,pos2+1);
            String parsed = parsePlaceholders("%"+sub.substring(1,sub.length()-1)+"%",viewer,target);

            str.replace(pos1, pos2 + 1, parsed);

            newPositions.put(pos1,sub.length()-parsed.length());
        }
        return str.toString();
    }
}
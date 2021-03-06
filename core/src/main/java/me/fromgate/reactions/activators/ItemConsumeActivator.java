/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.reactions.activators;

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.event.ItemConsumeEvent;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.item.ItemUtil;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.message.M;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;

public class ItemConsumeActivator extends Activator {

    private String item;


    public ItemConsumeActivator(String name, String group, YamlConfiguration cfg) {
        super(name, group, cfg);
    }

    public ItemConsumeActivator(String name, String item) {
        super(name, "activators");
        this.item = item;
    }

    public boolean activate(Event event) {
        if (!this.item.isEmpty() && ItemUtil.parseItemStack(this.item) != null) {
            if (event instanceof ItemConsumeEvent) {
                ItemConsumeEvent ie = (ItemConsumeEvent) event;
                if (ItemUtil.compareItemStr(ie.getItem(), this.item)) {
                    VirtualItem vi = ItemUtil.itemFromItemStack(ie.getItem());
                    if (vi != null) {
                        Variables.setTempVar("item", vi.toString());
                        Variables.setTempVar("item-str", vi.toDisplayString());
                    }

                    return Actions.executeActivator(ie.getPlayer(), this);
                }
            }

            return false;
        } else {
            M.logOnce(this.name + "activatoritemempty", "Failed to parse item of activator " + this.name);
            return false;
        }
    }

    public boolean isLocatedAt(Location loc) {
        return false;
    }

    public void save(String root, YamlConfiguration cfg) {
        cfg.set(root + ".item", this.item);
    }

    public void load(String root, YamlConfiguration cfg) {
        this.item = cfg.getString(root + ".item");
    }

    public ActivatorType getType() {
        return ActivatorType.ITEM_CONSUME;
    }

    @Override
    public boolean isValid() {
        return !Util.emptySting(item);
    }

    public String toString() {
        StringBuilder sb = (new StringBuilder(this.name)).append(" [").append(this.getType()).append("]");
        if (!this.getFlags().isEmpty()) {
            sb.append(" F:").append(this.getFlags().size());
        }

        if (!this.getActions().isEmpty()) {
            sb.append(" A:").append(this.getActions().size());
        }

        if (!this.getReactions().isEmpty()) {
            sb.append(" R:").append(this.getReactions().size());
        }

        sb.append(" (").append(this.item).append(")");
        return sb.toString();
    }
}

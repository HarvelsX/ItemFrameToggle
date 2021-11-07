package os.versions.v1_13;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import os.ItemFrameToggle;
import os.Triggers.ITrigger;
import os.Triggers.TriggerManagers.BlockTriggerManager;
import os.versions.BaseInteractListener;

public class InteractListener extends BaseInteractListener {

    public InteractListener(ItemFrameToggle plugin, BlockTriggerManager trigger){
        super(plugin, trigger);
    }

    @EventHandler
    public void hideItemFrameWhileSneaking(PlayerInteractAtEntityEvent e) {
        Entity clicked = e.getRightClicked();
        Player p = e.getPlayer();
        if (!(e.getHand().equals(EquipmentSlot.HAND))){
            return;
        }
        if (!isItemFrame(clicked.getType())){
            return;
        }
        if (!(p.isSneaking())){
            return;
        }
        if (plugin.permissionBased && !(p.hasPermission("itemframetoggle.toggle"))){
            return;
        }
        ItemFrame itemFrame = (ItemFrame)clicked;
        if (!(itemFrame.getItem().getType() == Material.AIR && p.getItemInHand().getType() == Material.AIR)){
            itemFrame.setRotation(itemFrame.getRotation().rotateCounterClockwise());
        }
        if (itemFrame.isVisible()){
            plugin.sendMessage(p, plugin.messageOnHide);
        }
        itemFrame.setVisible(!(itemFrame).isVisible());
    }

    @EventHandler
    public void openContainerWhenFrameInvisible(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (e.isCancelled()){
            return;
        }
        if (!(e.getHand() == (EquipmentSlot.HAND))){
            return;
        }
        if (p.isSneaking()){
            return;
        }
        if (!isItemFrame(e.getRightClicked().getType())) {
            return;
        }
        if (((ItemFrame)e.getRightClicked()).isVisible()){
            return;
        }
        if (plugin.permissionBased && !(p.hasPermission("itemframetoggle.toggle"))){
            return;
        }
        Block block = e.getRightClicked().getLocation().getBlock().getRelative(((ItemFrame)e.getRightClicked()).getAttachedFace());
        if (block == null){
            return;
        }
        Material blockType = block.getType();
        if (block.getState() instanceof ShulkerBox){
            blockType = Material.SHULKER_BOX;
        }
        ITrigger t = trigger.getTrigger(blockType);
        if (t != null) {
            if (openContainer(p, block)){
                Inventory i = getInventory(p, block);
                if (hasPermission(p, t.getPermission())){
                    if (i != null){
                        p.openInventory(i);
                    }
                }

            }

            e.setCancelled(true);
        }
    }

    public Inventory getInventory(Player p, Block block){
        if (block.getType().equals(Material.ENDER_CHEST)){
            return p.getEnderChest();
        }
        if (block.getState() instanceof BlockInventoryHolder){
            return ((BlockInventoryHolder) block.getState()).getInventory();
        }
        return null;
    }

    @EventHandler
    public void rotateFrameItemWhileSneaking(EntityDamageByEntityEvent e){
        if (e.isCancelled()){
            return;
        }
        if (!isItemFrame(e.getEntityType())){
            return;
        }
        if (e.getDamager() == null || e.getDamager().getType() != EntityType.PLAYER){
            return;
        }

        Player player = (Player)e.getDamager();


        if (plugin.permissionBased && !(player.hasPermission("itemframetoggle.toggle"))){
            return;
        }

        if (!(player.isSneaking())){
            return;
        }
        e.setCancelled(true);
        rotateItemFrame((ItemFrame)e.getEntity());
    }

    @Override
    public boolean openContainer(Player player, Block block) {
        PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), block, BlockFace.UP);
        Bukkit.getPluginManager().callEvent(playerInteractEvent);
        return !playerInteractEvent.isCancelled();
    }

    @Override
    public void rotateItemFrame(ItemFrame itemFrame){
        itemFrame.setRotation(itemFrame.getRotation().rotateCounterClockwise());
    }
    @Override
    public boolean isItemFrame(EntityType item){
        if (item == null){
            return false;
        }
        return item == EntityType.ITEM_FRAME;
    }
}
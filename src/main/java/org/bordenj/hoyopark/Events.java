package org.bordenj.hoyopark;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Events implements Listener {

    enum YasState {DASHING,IDLE,CASTING}

    private final Main plugin;
    public Events(Main plugin){
        this.plugin=plugin;
    }

    HashMap<Player, Integer> PStacks = new HashMap<>();
    HashMap<Player,YasState> States =  new HashMap<>();

    public void addStates(Player p){
        States.put(p,YasState.IDLE);
    }

    public void cleanup(){
        PStacks.clear();
    }

    public static class LocalFrame {
        public final Location origin;
        public final Vector forward;
        public final Vector right;
        public final Vector up = new Vector(0, 1, 0);

        public LocalFrame(Location origin) {
            this.origin = origin.clone();

            Vector f = origin.getDirection().clone();
            f.setY(0);
            if (f.lengthSquared() < 1e-6) {
                f = new Vector(0, 0, -1);
            }
            f.normalize();
            this.forward = f;
            this.right = f.clone().crossProduct(up);
        }

        public Location point(double fwd, double rightOffset, double upOffset) {
            return origin.clone()
                    .add(forward.clone().multiply(fwd))
                    .add(right.clone().multiply(rightOffset))
                    .add(up.clone().multiply(upOffset));
        }
    }

    public void drawSlam(Player p){
        new BukkitRunnable() {
            int c = 0;
            final Location origin = p.getLocation();
            @Override
            public void run() {
                if(c==5){
                    cancel();
                }
                c++;
                LocalFrame frame = new LocalFrame(origin);

                Location a = frame.point(1,0.3,0);
                Location b = frame.point(1.5,1,0);
                Location c = frame.point(2,1.2,0);
                Location d = frame.point(2,0.8,0);
                Location e = frame.point(3,1.5,0);
                Location f = frame.point(3,0.8,0);
                Location g = frame.point(4,1.5,0);
                Location h = frame.point(4.5,1,0);

                drawLine(a,b,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(b,c,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(c,d,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(d,e,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(e,f,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(f,g,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(g,h,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);

                Location a2 = frame.point(1,-0.3,0);
                Location b2 = frame.point(1.5,-1,0);
                Location c2 = frame.point(2,-1.2,0);
                Location d2 = frame.point(2,-0.8,0);
                Location e2 = frame.point(3,-1.5,0);
                Location f2 = frame.point(3,-0.8,0);
                Location g2 = frame.point(4,-1.5,0);
                Location h2 = frame.point(4.5,-1,0);
                drawLine(a2,b2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(b2,c2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(c2,d2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(d2,e2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(e2,f2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(f2,g2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(g2,h2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);

                Location mid = frame.point(4.2,0,0);
                drawLine(h,mid,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(h2,mid,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);

                Location base1 = frame.point(0.1,0.15,0);
                Location base2 = frame.point(0.1,-0.15,0);
                drawLine(a,base1,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
                drawLine(a2,base2,0.2, new Particle.DustOptions(Color.AQUA,0.7f),0,0,0);
            }
        }.runTaskTimer(plugin,0,2);

    }

    public void drawLine(Location point1, Location point2, double space, Particle.DustOptions dust, double ox, double oy, double oz) {
        World world = point1.getWorld();
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < distance; p1.add(vector)) {
            world.spawnParticle(Particle.DUST, p1.getX(), p1.getY(), p1.getZ(), 3,ox,oy,oz, dust);
            length += space;
        }
    }
    public void drawCircle(Location center, double radius, int points,Particle particle) {
        World world = center.getWorld();
        if (world == null || points <= 0 || radius < 0) return;

        double cx = center.getX();
        double y  = center.getY();
        double cz = center.getZ();

        for (int i = 0; i < points; i++) {
            double theta = (2.0 * Math.PI * i) / points;
            double x = cx + radius * Math.cos(theta);
            double z = cz + radius * Math.sin(theta);
            //world.spawnParticle(Particle.ENCHANTED_HIT, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    @EventHandler
    public void PlayerClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR)||e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(p.getInventory().getItemInMainHand().getType()== Material.MACE){//&&p.getLocation().clone().subtract(0,1,0).getBlock().getType()!=Material.AIR
                e.setCancelled(true);
                drawSlam(p);
            }

        }
    }

    @EventHandler
    public void EntityInteract(PlayerInteractEntityEvent e){
        if(e.getRightClicked() instanceof LivingEntity){
            Player p = e.getPlayer();
            if(p.getInventory().getItemInMainHand().getType()== Material.MACE&&p.getLocation().clone().subtract(0,1,0).getBlock().getType()!=Material.AIR){
                e.setCancelled(true);

            }
        }
    }

    @EventHandler
    public void PlayerSwapHands(PlayerSwapHandItemsEvent e){
        Player p = e.getPlayer();
        if(p.getInventory().getItemInMainHand().getType()==Material.MACE){
            e.setCancelled(true);

        }
    }

    @EventHandler
    public void PlayerDropItem(PlayerDropItemEvent e){
        Player p = e.getPlayer();
        if(e.getItemDrop().getItemStack().getType()==Material.MACE){
            e.setCancelled(true);


        }
    }

    @EventHandler
    public void PlayerSneak(PlayerToggleSneakEvent e){
        Player p = e.getPlayer();

    }

    @EventHandler
    public void TheSlimening(EntitySpawnEvent e){
        if(e.getEntity() instanceof Slime){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void DamageDealt(EntityDamageEvent e){

    }
}

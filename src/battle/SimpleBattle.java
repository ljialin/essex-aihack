package battle;

import asteroids.*;
import math.Vector2d;
import utilities.JEasyFrame;

import java.util.ArrayList;
import java.awt.*;


import static asteroids.Constants.*;

/**
 * Created by simon lucas on 10/06/15.
 * <p>
 * Aim here is to have a simple battle class
 * that enables ships to fish with each other
 * <p>
 * Might start off with just two ships, each with their own types of missile.
 */

public class SimpleBattle {

    // play a time limited game with a strict missile budget for
    // each player
    static int nMissiles = 100;
    static int nTicks = 1000;
    static int pointsPerKill = 10;
    static int releaseVelocity = 5;
    static boolean visible = true;

    ArrayList<BattleController> controllers;

    ArrayList<GameObject> objects;
    ArrayList<PlayerStats> stats;

    NeuroShip s1, s2;
    BattleController p1, p2;
    BattleView view;

    public SimpleBattle() {
        this.objects = new ArrayList<>();
        this.stats = new ArrayList<>();

        if (visible) {
            view = new BattleView(this);
            new JEasyFrame(view, "battle");
        }
    }

    public int playGame(BattleController p1, BattleController p2) {
        this.p1 = p1;
        this.p2 = p2;
        reset();

        stats.add(new PlayerStats(0, 0));
        stats.add(new PlayerStats(0, 0));

        int currentTicks = 0;

        while (currentTicks++ < nTicks) {
            update();
        }

        return 0;
    }

    protected void reset() {
        stats.clear();
        objects.clear();
        s1 = buildShip(250, 250);
        s2 = buildShip(300, 300);
    }

    protected NeuroShip buildShip(int x, int y) {
        Vector2d position = new Vector2d(x, y);
        Vector2d speed = new Vector2d();
        Vector2d direction = new Vector2d(1, 0);

        return new NeuroShip(position, speed, direction );
    }

    public void update() {
        // get the actions from each player

        // apply them to each player's ship, taking actions as necessary
        Action a1 = p1.getAction(this, 0);
        Action a2 = p2.getAction(this, 1);
        update(a1, a2);
    }

    public void update(Action a1, Action a2) {
        // now apply them to the ships
        s1.update(a1);
        s2.update(a2);

        checkCollision(s1);
        checkCollision(s2);

        // and fire any missiles as necessary
        if (a1.shoot) fireMissile(s1.s, s1.d, 0);
        if (a2.shoot) fireMissile(s2.s, s2.d, 1);

        // here need to add the game objects ...
        java.util.List<GameObject> killList = new ArrayList<GameObject>();
        for (GameObject object : objects) {
            object.update();
            if (object.dead()) {
                killList.add(object);
            }
        }

        objects.removeAll(killList);

        if (visible) {
            view.repaint();
            sleep();
        }
    }


    public SimpleBattle clone() {
        SimpleBattle state = new SimpleBattle();
        state.objects = copyObjects();
        state.stats = copyStats();

        state.s1 = s1.copy();
        state.s2 = s2.copy();
        return state;
    }

    protected ArrayList<GameObject> copyObjects() {
        ArrayList<GameObject> objectClone = new ArrayList<GameObject>();
        for (GameObject object : objects) {
            objectClone.add(object.copy());
        }

        return objectClone;
    }

    protected ArrayList<PlayerStats> copyStats() {
        ArrayList<PlayerStats> statsClone = new ArrayList<PlayerStats>();
        for (PlayerStats object : stats) {
            statsClone.add(new PlayerStats(object.nMissiles, object.nPoints));
        }

        return statsClone;
    }

    protected void checkCollision(GameObject actor) {
        // check with all other game objects
        // but use a hack to only consider interesting interactions
        // e.g. asteroids do not collide with themselves
        if (!actor.dead() &&
                (actor instanceof BattleMissile
                        || actor instanceof NeuroShip)) {
            if (actor instanceof BattleMissile) {
                // System.out.println("Missile: " + actor);
            }
            for (GameObject ob : objects) {
                if (overlap(actor, ob)) {
                    // the object is hit, and the actor is also

                    int playerID = (actor == s1 ? 1 : 0);
                    PlayerStats stats = this.stats.get(playerID);
                    stats.nPoints += pointsPerKill;
                    return;
                }
            }
        }
    }

    private boolean overlap(GameObject actor, GameObject ob) {
        if (actor.equals(ob)) {
            return false;
        }
        // otherwise do the default check
        double dist = actor.s.dist(ob.s);
        boolean ret = dist < (actor.r() + ob.r());
        return ret;
    }

    public void sleep() {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void fireMissile(Vector2d s, Vector2d d, int playerId) {
        // need all the usual missile firing code here
        NeuroShip currentShip = playerId == 0 ? s1 : s2;
        PlayerStats stats = this.stats.get(playerId);
        if (stats.nMissiles < nMissiles) {
            Missile m = new Missile(s, new Vector2d(0, 0));
            m.v.add(d, releaseVelocity);
            // make it clear the ship
            m.s.add(m.v, (currentShip.r() + missileRadius) * 1.5 / m.v.mag());
            objects.add(m);
            // System.out.println("Fired: " + m);
            // sounds.fire();
            stats.nMissiles++;
        }
    }

    public void draw(Graphics2D g) {
        // for (Object ob : objects)

        // System.out.println("In draw(): " + n);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillRect(0, 0, size.width, size.height);

        for (GameObject go : objects) {
            go.draw(g);
        }

        s1.draw(g);
        s2.draw(g);
    }

    public ArrayList<GameObject> getObjects()
    {
        return objects;
    }

    public PlayerStats getStats(int playerID) {
        return stats.get(playerID);
    }

    static class PlayerStats {
        int nMissiles;
        int nPoints;

        public PlayerStats(int nMissiles, int nPoints) {
            this.nMissiles = nMissiles;
            this.nPoints = nPoints;
        }

        public String toString() {
            return nMissiles + " : " + nPoints;
        }
    }

}

package controller;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;



class PlayerInfo {
    String username;
    int score;
    String code;


    public PlayerInfo(String username, int score, String code) {
        this.username = username;
        this.score = score;
        this.code=code;
    }
}

@ServerEndpoint(value = "/game/{username}/{code}")
public class GameWebSocket {

    // On stocke tous les joueurs connectés
    public static final Map<Session, PlayerInfo> players = new ConcurrentHashMap<>();
    public static List<PlayerInfo> playersOrder = new ArrayList<>();

    public static int currentPlayerIndex = 0;

    public static void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playersOrder.size();

    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("code") String code) {
        PlayerInfo player = new PlayerInfo(username, 0,code);
        players.put(session, player);
        if(!playersOrder.contains(player)){
            playersOrder.add(player);
        }


        System.out.println("[WebSocket] Nouveau joueur : " + username);

        broadcastPlayerJoined(player);
    }


    @OnClose
    public void onClose(Session session) {
        PlayerInfo leavingPlayer = players.remove(session);

        if (leavingPlayer != null) {
            System.out.println("[WebSocket] Joueur déconnecté : " + leavingPlayer.username);
            broadcastPlayerLeft(leavingPlayer);
        }
    }


    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("[WebSocket] Message reçu : " + message);

        JSONObject json = new JSONObject(message);
        String type = (String) json.get("type");
        PlayerInfo player = players.get(session);
        switch (type) {
            case "move":
                nextTurn();

                broadcastSoldierMoved(player, json.getInt("soldatId"));
                break;
            case "combatVille":
                broadcastCombatVille(player,json.getInt("idVille"),json.getInt("soldatId"));
                break;
            case "combatSoldat":
                broadcastCombatSoldat(player,json.getInt("idSoldat"),json.getInt("SoldatId"));
                break;
            case "destroyForest":
                broadcastDestroyForest(player,json.getInt("idForet"),json.getInt("soldatId"));
                break;
            case "askTour":
                respondTour(session);
                break;
            default:
                System.out.println("[WebSocket] Message inconnu : " + message);
        }



    }

    public static void respondTour(Session session) throws IOException {
        String json = String.format(
                "{\"type\":\"respondTour\",\"username\":\"%s\",\"code\":\"%s\"}",playersOrder.get(currentPlayerIndex).username,playersOrder.get(currentPlayerIndex).code
        );
        session.getBasicRemote().sendText(json);
    }

    public static void broadcastDestroyForest(PlayerInfo player, int soldatId,int idForet){
        String json = String.format(
                "{\"type\":\"destroyForest\",\"username\":\"%s\",\"soldatId\":%d,\"foretId\":%d,\"code\":\"%s\"}",
                player.username, soldatId,idForet,player.code
        );
        broadcastMessage(json);
    }

    public static void broadcastCombatVille(PlayerInfo player,int soldatId, int idVille){
        String json = String.format(
                "{\"type\":\"combatVille\",\"username\":\"%s\",\"soldatId\":%d,\"villeId\":%d,\"code\":\"%s\"}",
                player.username, soldatId, idVille,player.code
        );
        broadcastMessage(json);
    }

    public static void broadcastCombatSoldat(PlayerInfo player,int soldatId, int idSoldat){
        String json = String.format(
                "{\"type\":\"combatSoldat\",\"username\":\"%s\",\"soldatId\":%d,\"soldatEnnemiId\":%d,\"code\":\"%s\"}",
                player.username, soldatId,idSoldat,player.code
        );
        broadcastMessage(json);
    }

    /**
     * Diffuse l'événement "playerJoined" à toutes les sessions.
     */
    public static void broadcastPlayerJoined(PlayerInfo player) {
        String json = String.format(
                "{\"type\":\"playerJoined\",\"username\":\"%s\",\"score\":%d,\"code\":\"%s\"}",
                player.username, player.score,player.code
        );
        broadcastMessage(json);
    }

    /**
     * Diffuse l'événement "playerLeft" à tous.
     */
    public static void broadcastPlayerLeft(PlayerInfo player) {

        String json = String.format(
                "{\"type\":\"playerLeft\",\"username\":\"%s\",\"code\":\"%s\"}",
                player.username,player.code
        );
        broadcastMessage(json);
    }

    public static void broadcastSoldierMoved(PlayerInfo player, int soldatId) {
        String json = String.format(
                "{\"type\":\"move\",\"username\":\"%s\",\"soldatId\":%d,\"code\":\"%s\"}",
                player.username, soldatId,player.code
        );
        broadcastMessage(json);
    }

    /**
     * Diffuse un message JSON à tous les clients connectés.
     */
    public static void broadcastMessage(String message) {
        players.keySet().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                players.remove(session);
                System.out.println("Session fermée");
            }
        });
    }
}













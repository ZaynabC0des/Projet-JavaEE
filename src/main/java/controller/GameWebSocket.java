package controller;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.*;


class PlayerInfo {
    String username;
    int score;
    Integer soldatId;


    public PlayerInfo(String username, int score, Integer soldatId) {
        this.username = username;
        this.score = score;
        this.soldatId= soldatId;
    }
}

@ServerEndpoint(value = "/game/{username}")
public class GameWebSocket {

    // On stocke tous les joueurs connectés
    private static Map<Session, PlayerInfo> players = new ConcurrentHashMap<>();
    private static PlayerInfo playerTour=null;

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        PlayerInfo player = new PlayerInfo(username, 0,0);
        players.put(session, player);
        if(playerTour==null){
            playerTour=player;
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
                if(players.size()!=1) {
                    if (playerTour.username.equals(player.username)) {
                        playerTour = players.values().stream().filter(p -> !p.username.equals(player.username)).findFirst().get();
                    } else {
                        return;
                    }
                }else{
                    playerTour=player;
                }

                broadcastSoldierMoved(player);
                break;
            case "combatVille":
                broadcastCombatVille(player,json.getInt("idVille"));
                break;
            case "combatSoldat":
                broadcastCombatSoldat(player,json.getInt("idSoldat"));
                break;
            case "destroyForest":
                broadcastDestroyForest(player,json.getInt("idForet"));
                break;
            case "askTour":
                respondTour(session);
                break;
            default:
                System.out.println("[WebSocket] Message inconnu : " + message);
        }



    }

    private void respondTour(Session session) throws IOException {
        String json = String.format(
                "{\"type\":\"respondTour\",\"username\":\"%s\"}",playerTour.username
        );
        session.getBasicRemote().sendText(json);
    }

    private void broadcastDestroyForest(PlayerInfo player, int idForet){
        String json = String.format(
                "{\"type\":\"destroyForest\",\"username\":\"%s\",\"soldatId\":%d,\"foretId\":%d}",
                player.username, player.soldatId,idForet
        );
        broadcastMessage(json);
    }

    private void broadcastCombatVille(PlayerInfo player, int idVille){
        String json = String.format(
                "{\"type\":\"combatVille\",\"username\":\"%s\",\"soldatId\":%d,\"villeId\":%d}",
                player.username, player.soldatId, idVille
        );
        broadcastMessage(json);
    }

    private void broadcastCombatSoldat(PlayerInfo player, int idSoldat){
        String json = String.format(
                "{\"type\":\"combatSoldat\",\"username\":\"%s\",\"soldatId\":%d,\"soldatEnnemiId\":%d}",
                player.username, player.soldatId,idSoldat
        );
        broadcastMessage(json);
    }

    /**
     * Diffuse l'événement "playerJoined" à toutes les sessions.
     */
    private void broadcastPlayerJoined(PlayerInfo player) {
        String json = String.format(
                "{\"type\":\"playerJoined\",\"username\":\"%s\",\"score\":%d}",
                player.username, player.score
        );
        broadcastMessage(json);
    }

    /**
     * Diffuse l'événement "playerLeft" à tous.
     */
    private void broadcastPlayerLeft(PlayerInfo player) {
        String json = String.format(
                "{\"type\":\"playerLeft\",\"username\":\"%s\"}",
                player.username
        );
        broadcastMessage(json);
    }

    private void broadcastSoldierMoved(PlayerInfo player) {
        String json = String.format(
                "{\"type\":\"move\",\"username\":\"%s\",\"soldatId\":%d}",
                player.username, player.soldatId
        );
        broadcastMessage(json);
    }

    /**
     * Diffuse un message JSON à tous les clients connectés.
     */
    public void broadcastMessage(String message) {
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













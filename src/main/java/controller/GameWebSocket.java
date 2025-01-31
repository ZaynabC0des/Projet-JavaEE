package controller;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.json.*;


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
    public static List<String> playersOrder = new ArrayList<>();

    public static int currentPlayerIndex = 0;

    public static void nextTurn() {
        if (playersOrder.isEmpty()) {
            currentPlayerIndex = 0;
            return;
        }
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % playersOrder.size();
        } while (getPlayerbyusername(playersOrder.get(currentPlayerIndex)) == null
                && !playersOrder.isEmpty());

        // Diffuser le tour actuel apr�s le changement
        broadcastCurrentTurn();
    }


    private static PlayerInfo getPlayerbyusername(String username){
        for (PlayerInfo player : players.values()) {
            System.out.println(player.username);
            System.out.println(username);
            if (player.username.equals(username)) {
                return player;
            }
        }
        return null;
    }

    // Map qui associe chaque playerId à un Timer
    private static Map<String, java.util.Timer> disconnectTimers = new ConcurrentHashMap<>();

    // Délai en millisecondes (p. ex. 5 secondes)
    private static final long GRACE_PERIOD_MS = 3000;


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("code") String code) {
        if (disconnectTimers.containsKey(username)) {

            System.out.println("[WebSocket] Annulation du départ pour " + username);

            // Annuler le Timer
            disconnectTimers.get(username).cancel();
            disconnectTimers.remove(username);
        }

        PlayerInfo player = new PlayerInfo(username, 0,code);
        players.put(session, player);
        if(!playersOrder.contains(player.username)){
            playersOrder.add(player.username);
        }


        System.out.println("[WebSocket] Nouveau joueur : " + username);

        broadcastPlayerJoined(player);
    }


    @OnClose
    public void onClose(Session session) {
        PlayerInfo leavingPlayer = players.remove(session);

        if (leavingPlayer != null) {
            String playerId = leavingPlayer.username; // ou leavingPlayer.token
            System.out.println("[WebSocket] Fermeture de la session : " + playerId);

            // Créer un TimerTask qui déclenchera "playerLeft" au bout de 5s
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // S’il est dans ce timer après 3s,
                    // c’est qu’il ne s’est pas reconnecté
                    System.out.println("[WebSocket] Joueur vraiment parti : " + playerId);
                    playersOrder.remove(leavingPlayer.username);
                    nextTurn();
                    broadcastPlayerLeft(leavingPlayer);

                    // Supprimer le timer de la map
                    disconnectTimers.remove(playerId);
                }
            };

            // Créer un Timer (on pourrait réutiliser un ThreadPool, etc.)
            java.util.Timer timer = new java.util.Timer();
            timer.schedule(task, GRACE_PERIOD_MS);

            // Stocker ce timer pour éventuellement l’annuler s’il se reconnecte avant
            disconnectTimers.put(playerId, timer);
        }
    }


    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("[WebSocket] Message reçu : " + message);
        System.out.println(playersOrder);
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
                "{\"type\":\"respondTour\",\"username\":\"%s\",\"code\":\"%s\"}",playersOrder.get(currentPlayerIndex), getPlayerbyusername(playersOrder.get(currentPlayerIndex)).code
        );
        session.getBasicRemote().sendText(json);
    }
    

    public static void broadcastCurrentTurn() {
        String json = String.format(
            "{\"type\":\"currentTurn\",\"username\":\"%s\",\"code\":\"%s\"}",
            playersOrder.get(currentPlayerIndex), getPlayerbyusername(playersOrder.get(currentPlayerIndex)).code
        );
        broadcastMessage(json);
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

                System.out.println("Session fermée");
            }
        });
    }
}













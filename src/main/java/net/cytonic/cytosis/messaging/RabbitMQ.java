package net.cytonic.cytosis.messaging;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

public class RabbitMQ {

    public static final String SERVER_DECLARE_QUEUE = "server-declaration";
    public static final String SHUTDOWN_QUEUE = "server-shutdown";
    public static final String CHAT_CHANNEL_QUEUE = "chat-channel";
    private Connection connection;
    private Channel channel;

    public void initializeConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(CytosisSettings.RABBITMQ_HOST);
        factory.setPassword(CytosisSettings.RABBITMQ_PASSWORD);
        factory.setUsername(CytosisSettings.RABBITMQ_USERNAME);
        factory.setPort(CytosisSettings.RABBITMQ_PORT);
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            Logger.error("An error occurred whilst connecting to RabbitMQ!", e);
        }
        Logger.info("Connected to RabbitMQ!");
        try {
            channel = connection.createChannel();
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            Logger.error("An error occurred whilst connecting to RabbitMQ!", e);
        }
    }

    public void initializeQueues() {
        Logger.info("Initializing RabbitMQ queues...");
        try {
            channel.queueDeclare(SERVER_DECLARE_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the 'SERVER_DECLARE_QUEUE'.", e);
        }
        try {
            channel.queueDeclare(SHUTDOWN_QUEUE, false, false, false, null);
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the 'SHUTDOWN_QUEUE'.", e);
        }
        try {
            channel.queueDeclare(CHAT_CHANNEL_QUEUE, false, false, false, null);
            channel.exchangeDeclare("chat-messages", "fanout");
            channel.queueBind(CHAT_CHANNEL_QUEUE, "chat-messages", "");
        } catch (IOException e) {
            Logger.error("An error occurred whilst initializing the 'CHAT_CHANNEL_QUEUE'.", e);
        }
    }

    public void sendServerDeclarationMessage() {
        //formatting: {server-name}|:|{server-ip}|:|{server-port}
        String serverName = System.getenv("HOSTNAME");
        String serverIP;
        try {
            serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Logger.error("An error occurred whilst fetching this server's IP address! Bailing out!", e);
            return;
        }
        String message = STR."\{serverName}|:|\{serverIP}|:|\{CytosisSettings.SERVER_PORT}";
        try {
            channel.basicPublish("", SERVER_DECLARE_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send the server declaration message!", e);
        }
        Logger.info(STR."Server Declaration message sent! '\{message}'.");
    }

    public void sendChatMessage(Component chatMessage, ChatChannel chatChannel) {
        //formatting: {chat-message}|{chat-channel}
        String message = STR."\{JSONComponentSerializer.json().serialize(chatMessage)}|\{chatChannel.name()}";
        try {
            channel.basicPublish("chat-messages", CHAT_CHANNEL_QUEUE, null, message.getBytes());
        } catch (IOException e) {
            Logger.error("An error occurred whilst attempting to send a chat message!", e);
        }
    }

    public void receiveChatMessages() {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String[] thing = new String(delivery.getBody(), "UTF-8").split("\\|");
            Component chatMessage = JSONComponentSerializer.json().deserialize(thing[0]);
            ChatChannel chatChannel = ChatChannel.valueOf(thing[1]);
            switch (chatChannel) {
            case MOD -> // send a message to all players with cytonic.chat.mod permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.mod")) {
                            player.sendMessage(chatMessage);
                        }
                    });

            case STAFF -> // send a message to all players with cytonic.chat.staff permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.staff")) {
                            player.sendMessage(chatMessage);
                        }
            });           
            case ADMIN -> // send a message to all players with cytonic.chat.admin permission
                    Cytosis.getOnlinePlayers().forEach(player -> {
                        if (player.hasPermission("cytonic.chat.admin")) {
                            player.sendMessage(chatMessage);
                        }
            });
            case LEAGUE -> {
            // leagues..
            }

            case PARTY -> {
            // parties..
            }
        }
    };
    channel.basicConsume(CHAT_CHANNEL_QUEUE, true, deliverCallback, consumerTag -> { });
} catch (IOException e) {
    Logger.error("error", e);
}
}
}
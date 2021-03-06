package org.ethereum.net.server;

import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.ethereum.manager.WorldManager;

import org.ethereum.net.eth.SyncManager;
import org.ethereum.net.rlpx.discover.NodeHandler;
import org.ethereum.net.rlpx.discover.NodeManager;
import org.ethereum.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * @author Roman Mandeleil
 * @since 11.11.2014
 */
@Component
public class ChannelManager {

    private static final Logger logger = LoggerFactory.getLogger("net");

    private List<Channel> newPeers = new CopyOnWriteArrayList<>();
    private List<Channel> activePeers = new CopyOnWriteArrayList<>();
    private final Set<String> disconnectedIds = new HashSet<>();
    private final Set<String> reconnectedIds = new HashSet<>();

    private ScheduledExecutorService mainWorker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService reconnectWorker = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    WorldManager worldManager;

    @Autowired
    SyncManager syncManager;

    @Autowired
    NodeManager nodeManager;

    @Autowired
    Ethereum ethereum;

    @PostConstruct
    public void init() {
        mainWorker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                processNewPeers();
            }
        }, 0, 1, TimeUnit.SECONDS);

        reconnectWorker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                processReconnects();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void processReconnects() {
        synchronized (disconnectedIds) {
            for(String nodeId : disconnectedIds) {
                NodeHandler nodeHandler = nodeManager.findById(nodeId);
                logger.info("Peer {}: reconnecting", Utils.getNodeIdShort(nodeId));
                ethereum.connect(nodeHandler.getNode());
            }
            reconnectedIds.addAll(disconnectedIds);
            disconnectedIds.clear();
        }
    }

    private void processNewPeers() {
        List<Channel> processed = new ArrayList<>();
        for(Channel peer : newPeers) {
            if(peer.hasInitPassed()) {
                if(peer.isUseful()) {
                    processUseful(peer);
                }
                processed.add(peer);
            }
        }
        newPeers.removeAll(processed);
    }

    private void processUseful(Channel peer) {
        if(peer.ethHandler.hasStatusSucceeded()) {
            syncManager.addPeer(peer.ethHandler);
            activePeers.add(peer);
        }
    }

    public void sendTransaction(Transaction tx) {
        for (Channel channel : activePeers) {
            channel.sendTransaction(tx);
        }
    }

    public void addChannel(Channel channel) {
        newPeers.add(channel);
    }

    public void notifyDisconnect(Channel channel) {
        if(!activePeers.contains(channel)) {
            return;
        }
        channel.onDisconnect();
        syncManager.removePeer(channel.ethHandler);
        activePeers.remove(channel);
        synchronized (disconnectedIds) {
            if (reconnectedIds.contains(channel.remoteId)) {
                logger.info(
                        "Peer {}: hit too much disconnects, dropping",
                        Utils.getNodeIdShort(channel.remoteId)
                );
                reconnectedIds.remove(channel.remoteId);
            } else {
                logger.info(
                        "Peer {}: disconnected",
                        Utils.getNodeIdShort(channel.remoteId)
                );
                disconnectedIds.add(channel.remoteId);
            }
        }
    }
}

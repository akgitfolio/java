package git.folio;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

class Server {
    private String ip;
    private int load;

    public Server(String ip) {
        this.ip = ip;
        this.load = 0;
    }

    public String getIp() {
        return ip;
    }

    public int getLoad() {
        return load;
    }

    public void incrementLoad() {
        load++;
    }
}

class ServerCache {
    private List<Server> servers;

    public ServerCache() {
        servers = new ArrayList<>();
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    public List<Server> getServers() {
        return servers;
    }
}

abstract class LoadBalancer {
    protected ServerCache serverCache;

    public LoadBalancer(ServerCache serverCache) {
        this.serverCache = serverCache;
    }

    public abstract String getIp();
}

class RandomLoadBalancer extends LoadBalancer {
    private Random random;

    public RandomLoadBalancer(ServerCache serverCache) {
        super(serverCache);
        this.random = new Random();
    }

    @Override
    public String getIp() {
        List<Server> servers = serverCache.getServers();
        int index = random.nextInt(servers.size());
        Server server = servers.get(index);
        server.incrementLoad();
        return server.getIp();
    }
}

class RoundRobinLoadBalancer extends LoadBalancer {
    private int counter;
    private ReentrantLock lock;

    public RoundRobinLoadBalancer(ServerCache serverCache) {
        super(serverCache);
        this.counter = 0;
        this.lock = new ReentrantLock();
    }

    @Override
    public String getIp() {
        List<Server> servers = serverCache.getServers();
        lock.lock();
        try {
            Server server = servers.get(counter);
            counter = (counter + 1) % servers.size();
            server.incrementLoad();
            return server.getIp();
        } finally {
            lock.unlock();
        }
    }
}

public class SimpleLoadBalancer {
    public static void main(String[] args) {
        ServerCache serverCache = new ServerCache();
        serverCache.addServer(new Server("192.168.0.1"));
        serverCache.addServer(new Server("192.168.0.2"));
        serverCache.addServer(new Server("192.168.0.3"));

        LoadBalancer randomLB = new RandomLoadBalancer(serverCache);
        LoadBalancer roundRobinLB = new RoundRobinLoadBalancer(serverCache);

        System.out.println("Random Load Balancer:");
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + i + " sent to: " + randomLB.getIp());
        }

        System.out.println("\nRound Robin Load Balancer:");
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + i + " sent to: " + roundRobinLB.getIp());
        }
    }
}
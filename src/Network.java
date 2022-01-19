import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.PriorityQueue;

class Device extends Thread {

    private String name, type;
    private int workingTime;
    private int connNum;
    private Router router;

    public Device(String name, String type, Router router) throws IOException {
        this.name = name;
        this.type = type;
        this.router = router;
        this.workingTime = (int) (Math.random() * (4005 - 1000 + 1) + 1000);
    }

    public String getDeviceName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setConnNum(int num) {
        this.connNum = num;
    }

    public int getConnNum() {
        return connNum;
    }

    public int getWorkingTime() {
        return workingTime;
    }

    @Override
    public void run() {
        System.out.println(getDeviceName() + " performs online activity");
        try {
            LogWriter.getWriter().write(getDeviceName() + " performs online activity \n");
            sleep(this.getWorkingTime());
            this.end();
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void end() throws InterruptedException, IOException {
        router.removeDevice(this);
    }

}

class Semaphore {

    private int value;
    private ArrayList<Device> waitingList = new ArrayList<Device>();
    private PriorityQueue<Integer> connections;

    public Semaphore() {
        value = 1;
    }

    public Semaphore(int value) throws IOException {
        this.value = value;
        this.connections = new PriorityQueue<>();
        for (int i = 1; i <= value; i++) {
            connections.add(i);
        }

    }

    public synchronized void wait(Device device) throws IOException {
        value--;
        if (value < 0) {
            System.out.println(device.getDeviceName() + " " + device.getType() + " arrived and waiting");
            LogWriter.getWriter().write(device.getDeviceName() + " " + device.getType() + " arrived and waiting \n");
            waitingList.add(device);//<---
        } else {
            System.out.println(device.getDeviceName() + " " + device.getType() + " arrived");
            LogWriter.getWriter().write(device.getDeviceName() + " " + device.getType() + " arrived \n");
            device.setConnNum(connections.peek());
            if (connections.size() > 0) {
                connections.poll();
            }
            System.out.println("connection " + device.getConnNum() + " : " + device.getDeviceName() + " " + device.getType() + " occupied");
            LogWriter.getWriter().write("connection " + device.getConnNum() + " : " + device.getDeviceName() + " " + device.getType() + " occupied \n");
            device.start();
        }
    }

    public synchronized void signal(Device device) throws IOException {
        value++;
        System.out.println(device.getDeviceName() + " " + device.getType() + " Logged out");
        LogWriter.getWriter().write(device.getDeviceName() + " " + device.getType() + " Logged out \n");
        connections.add(device.getConnNum());
        if (value <= 0) {
            System.out.println("connection " + device.getConnNum() + " : " + waitingList.get(0).getDeviceName() + " " + waitingList.get(0).getType() + " occupied");
            LogWriter.getWriter().write("connection " + device.getConnNum() + " : " + waitingList.get(0).getDeviceName() + " " + waitingList.get(0).getType() + " occupied \n");
            waitingList.remove(0).start();
        }
    }
}

class Router {

    private int size;
    private Semaphore semaphore;

    public Router() throws IOException {
        this(5);
    }

    public Router(int size) throws IOException {
        this.size = size;
        this.semaphore = new Semaphore(this.size);
    }

    void addDevice(Device device) throws IOException {
        semaphore.wait(device);
    }

    void removeDevice(Device device) throws IOException {
        semaphore.signal(device);
    }

}

class LogWriter {

    private File file;
    private FileWriter fileWriter;
    private StringBuffer sb;
    private static LogWriter lw = null;

    private LogWriter() throws IOException {
        this.file = new File("log.txt");
        sb = new StringBuffer();
    }

    public static LogWriter getWriter() throws IOException {
        if (lw == null) {
            lw = new LogWriter();
        }
        return lw;
    }

    public void write(String s) throws IOException {
        fileWriter = new FileWriter(file);
        sb.append(s);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }
}

public class Network {

    public static void main(String args[]) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.println("What is the number of WI-FI Connections?");
        int n = scan.nextInt();
        System.out.println("What is the number of devices Clients want to connect?");
        int tc = scan.nextInt();
        ArrayList<Device> devices = new ArrayList<>();

        Router router = new Router(n);
        for (int i = 0; i < tc; i++) {
            String name = scan.next();
            String type = scan.next();
            Device device = new Device(name, type, router);
            devices.add(device);
        }

        for (int i = 0; i < tc; i++) {
            router.addDevice(devices.get(i));
        }

    }
}
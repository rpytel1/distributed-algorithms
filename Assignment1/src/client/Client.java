package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import clock.VectorClock;
import message.Message;
import util.Buffer;
import util.Constant;
import util.IRemoteEntity;

public class Client {
    static int msgNum[];
    static List<AtomicInteger> runs;

    public static void main(String[] args) throws NotBoundException, IOException, InterruptedException {
        Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        BufferedReader br = new BufferedReader(new FileReader("tests/messages.txt"));
        String line = "";
        Message temp;
        runs = new ArrayList<>();

        for (int i = 0; i < registry.list().length; i++) {
            runs.add(new AtomicInteger(0));
        }

        msgNum = new int[registry.list().length];
        for (int i = 0; i < msgNum.length; i++) msgNum[i] = 0;
        while ((line = br.readLine()) != null) {
            String[] split_line = line.split(" ");
            String msgText = split_line[1];
            VectorClock vt = new VectorClock(registry.list().length, Integer.parseInt(split_line[0]));
            int sender = Integer.parseInt(split_line[0]);
            System.out.println(sender);
            int receiver = Integer.parseInt(split_line[2]);
            temp = new Message(msgText, vt, new Buffer(), sender, receiver);

            if (runs.get(sender) != null) {
                runs.add(sender, new AtomicInteger(runs.get(sender).getAndIncrement()));
            }
            msgNum[sender]++;
            ((IRemoteEntity) registry.lookup(registry.list()[sender])).addMessageToBeSent(Integer.parseInt(split_line[3]), temp);
        }
        br.close();

        SchiperEggliSandoz();
    }

    public static void SchiperEggliSandoz() throws NotBoundException, InterruptedException, IOException {
        Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        IRemoteEntity[] RMI_IDS = new IRemoteEntity[registry.list().length];
        for (int i = 0; i < registry.list().length; i++) {
            RMI_IDS[i] = (IRemoteEntity) registry.lookup(registry.list()[i]);
            RMI_IDS[i].setRuns(runs.get(i).get());
        }

        for (int i = 0; i < RMI_IDS.length; i++) {
            IRemoteEntity RDi = RMI_IDS[i];

            int finalI = i;
            new Thread(() -> {
                for (int j = 0; j < msgNum[finalI]; j++) {
                    try {
                        RDi.sendMessage();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }
		/*while(runs.stream().mapToInt(p->p.get()).sum()!=0){
			Thread.sleep(1);
		}*/
}

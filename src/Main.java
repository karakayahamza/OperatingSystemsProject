class Friend implements Runnable {
    private final String name;
    private final TelephoneExchange exchange;

    public Friend(String name, TelephoneExchange exchange) {
        this.name = name;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 6; i++) {
                String receiver = "B" + i;
                String operator = exchange.connectCall(name, receiver);
                System.out.println(name + " is calling " + receiver + " (Operator: " + operator + ")");
                Thread.sleep(1000); // Simulate conversation time
                exchange.endCall(name);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class TelephoneExchange {
    private final Object lock = new Object();
    private boolean isLineBusy;
    private int completedCalls;
    private int operatorIndex;

    public TelephoneExchange() {
        this.isLineBusy = false;
        this.completedCalls = 0;
        this.operatorIndex = 0;
    }

    public String connectCall(String caller, String receiver) throws InterruptedException {
        String operator = "Operator " + (operatorIndex + 1);
        synchronized (lock) {
            while (isLineBusy) {
                lock.wait();
            }
            isLineBusy = true;
            System.out.println(caller + " is connected to " + operator);
        }
        Thread.sleep(1000); // Simulate operator connection time
        operatorIndex = (operatorIndex + 1) % 2; // Alternate between two operators
        return operator;
    }

    public void endCall(String caller) {
        synchronized (lock) {
            System.out.println(caller + " completed the call.");
            completedCalls++;
            isLineBusy = false;
            if (completedCalls == 36) {
                System.out.println("All calls completed");
            }
            lock.notify();
        }
    }

    public int getCompletedCalls() {
        return completedCalls;
    }
}

class TelephoneSimulation {
    public static void main(String[] args) throws InterruptedException {
        TelephoneExchange exchange = new TelephoneExchange();


        Thread[] friendsCityA = new Thread[6];
        for (int i = 0; i < 6; i++) {
            Friend friend = new Friend("A" + (i + 1), exchange);
            friendsCityA[i] = new Thread(friend);
        }

        Thread operatorThread = new Thread(() -> {
            while (exchange.getCompletedCalls() < 36) {
                synchronized (exchange) {
                    try {
                        exchange.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        operatorThread.start();

        for (Thread friend : friendsCityA) {
            friend.start();
        }

        for (Thread friend : friendsCityA) {
            friend.join();
        }

        synchronized (exchange) {
            exchange.notify();
        }

        operatorThread.join();
    }
}

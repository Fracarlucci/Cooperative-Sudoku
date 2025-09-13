package pcd.ass03.part1.simtrafficbase;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Thread that manages all the threads of the simulation
 */
public class ThreadManager {

    private RoadsEnv env;
    private List<CarAgent> carAgents;
    private List<TrafficLight> trafficLights;
    private int nSteps = 0;
    private long currentWallTime;
    private int nCyclesPerSec = 0;
    private long totalTime = 0;
    private final ExecutorService executor;


    public ThreadManager(int nThreadsPerCars, int nThreadsPerTrafficLights, RoadsEnv env) {
        this.carAgents = new LinkedList<>();
        this.trafficLights = new LinkedList<>();
        this.env = env;
        executor = Executors.newFixedThreadPool(nThreadsPerCars+nThreadsPerTrafficLights);
    }

    /**
     * Generate the threads for the cars
     *
     * @param carAgents
     */
    public void generateCars(List<CarAgent> carAgents) {
        this.carAgents.clear();
        this.carAgents = carAgents;
    }

    /**
     * Generate the threads for the traffic lights
     *
     * @param trafficLights
     */
    public void generateTrafficLight(List<TrafficLight> trafficLights) {
        this.trafficLights.clear();
        this.trafficLights = trafficLights;
    }
    /**
     * Start the agents' threads
     *
     * @param dt
     */
    public void startThreads(int dt) {
        carAgents.forEach(ca -> {
            ca.init(env, dt);
            // Submit the carAgent
            executor.submit(ca);
        });

        if (trafficLights != null) {
            trafficLights.forEach(tl -> {
                tl.init();
                // Submit the trafficLights
                executor.submit(tl);
            });
        }

        // Create the thread that will manage the simulation
        new Thread(() -> {
            int actualSteps = 0;
            long startWallTime = System.currentTimeMillis();
            int t = 0;
            long timePerStep = 0;
            long startStepTime = 0;

            while (actualSteps < this.nSteps) {
                if (startStepTime != 0) {
                    timePerStep += System.currentTimeMillis() - startStepTime;
                }
                t += dt;
                currentWallTime = System.currentTimeMillis();

                if (nCyclesPerSec > 0) {
                    // sim.syncWithWallTime(currentWallTime);
                }
                actualSteps++;
                startStepTime = System.currentTimeMillis();
                System.out.println("STEPS: " + actualSteps);
            }
            timePerStep += System.currentTimeMillis() - startStepTime;
            totalTime = System.currentTimeMillis() - startWallTime;
            System.out.println("Finish: " + actualSteps);
            System.out.println("Completed in " + totalTime + "ms");
            // shutdown threads when is finished
            executor.shutdown();
        }).start();
    }

    public void setSteps(int nSteps) {
        this.nSteps = nSteps;
    }

    public void setnCyclesPerSec(int nCyclesPerSec) {
        this.nCyclesPerSec = nCyclesPerSec;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
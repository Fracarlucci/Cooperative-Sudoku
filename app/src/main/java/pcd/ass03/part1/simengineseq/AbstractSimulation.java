package pcd.ass03.part1.simengineseq;

import pcd.ass03.part1.simengineconcur.GUIMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for defining concrete simulations
 */
public abstract class AbstractSimulation {

  /* environment of the simulation */
  private AbstractEnvironment env;

  /* list of the agents */
  private List<AbstractAgent> agents;

  /* simulation listeners */
  private List<SimulationListener> listeners;


  /* logical time step */
  private int dt = 1;

  /* initial logical time */
  private int t0;

  /* in the case of sync with wall-time */
  private boolean toBeInSyncWithWallTime;
  private int nStepsPerSec;

  /* for time statistics*/
  private long currentWallTime;
  private long startWallTime;
  private long endWallTime;
  private long averageTimePerStep;
  // stop = false; for massive test
  private volatile Boolean stop = false;
  private GUIMonitor monitor = new GUIMonitor();

  protected AbstractSimulation() {
    agents = new ArrayList<AbstractAgent>();
    listeners = new ArrayList<SimulationListener>();
    toBeInSyncWithWallTime = false;
  }

  /**
   * Method used to configure the simulation, specifying env and agents
   */
  protected abstract void setup();

  /**
   * Method running the simulation for a number of steps,
   * using a sequential approach
   *
   * @param numSteps
   */
  public void run(int numSteps) {

    startWallTime = System.currentTimeMillis();

    /* initialize the env and the agents inside */
    int t = t0;

    env.setnSteps(numSteps);
    env.init();


    this.notifyReset(t, agents, env);

    long timePerStep = 0;

    endWallTime = System.currentTimeMillis();
    this.averageTimePerStep = timePerStep / numSteps;

  }

  public int getDt() {
    return dt;
  }

  public long getSimulationDuration() {
    return endWallTime - startWallTime;
  }

  public long getAverageTimePerCycle() {
    return averageTimePerStep;
  }

  /* methods for configuring the simulation */
  protected void setupTimings(int t0, int dt) {
    this.dt = dt;
    this.t0 = t0;
  }

  protected void syncWithTime(int nCyclesPerSec) {
    this.toBeInSyncWithWallTime = true;
    this.nStepsPerSec = nCyclesPerSec;
  }

  protected void setupEnvironment(AbstractEnvironment env) {
    this.env = env;
  }

  protected void addAgent(AbstractAgent agent) {
    agents.add(agent);
  }

  /* methods for listeners */

  public void addSimulationListener(SimulationListener l) {
    this.listeners.add(l);
  }

  private void notifyReset(int t0, List<AbstractAgent> agents, AbstractEnvironment env) {
    for (var l : listeners) {
      l.notifyInit(t0, agents, env);
    }
  }

  public void notifyNewStep(int t, AbstractEnvironment env) {
    for (var l : listeners) {
      l.notifyStepDone(t, agents, env);
    }
  }

  /* method to sync with wall time at a specified step rate */
  public void syncWithWallTime(long currentWallTime) {
    try {
      long newWallTime = System.currentTimeMillis();
      long delay = 1000 / this.nStepsPerSec;
      long wallTimeDT = newWallTime - currentWallTime;
      if (wallTimeDT < delay) {
        Thread.sleep(delay - wallTimeDT);
      }
    } catch (Exception ex) {
    }
  }

  public boolean isStopped(){
    this.monitor.requestRead();
    boolean state = this.stop;
    this.monitor.releaseRead();
    return state;
  }

  public void stop(){
    this.monitor.requestWrite();
    this.stop = true;
    this.monitor.releaseWrite();
  }

  public void start(){
    this.monitor.requestWrite();
    this.stop = false;
    this.monitor.releaseWrite();
  }
}

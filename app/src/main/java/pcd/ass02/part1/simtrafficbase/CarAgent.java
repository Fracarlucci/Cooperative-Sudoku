package pcd.ass02.part1.simtrafficbase;

import java.util.Optional;

import pcd.ass02.part1.simengineconcur.Barrier;
import pcd.ass02.part1.simengineseq.*;

/**
 * Base class modeling the skeleton of an agent modeling a car in the traffic environment
 */
public abstract class CarAgent extends AbstractAgent implements Runnable {

  /* car model */
  protected double maxSpeed;
  protected double currentSpeed;
  protected double acceleration;
  protected double deceleration;

  /* percept and action retrieved and submitted at each step */
  protected CarPercept currentPercept;
  protected Optional<Action> selectedAction;

  private final Barrier actBarrier;   // Barrier before doing an action.
  private final Barrier stepBarrier;  // Barrier before doing next step.
  private final AbstractSimulation simulation;  // Barrier before doing next step.


  public CarAgent(String id, RoadsEnv env, Road road,
                  double initialPos,
                  double acc,
                  double dec,
                  double vmax, Barrier actBarrier, Barrier stepBarrier, AbstractSimulation simulation) {
    super(id);
    this.acceleration = acc;
    this.deceleration = dec;
    this.maxSpeed = vmax;
    this.actBarrier = actBarrier;
    this.stepBarrier = stepBarrier;
    this.simulation = simulation;
    env.registerNewCar(this, road, initialPos);
  }

  /**
   * Sense and decide the action to be taken
   * @param dt
   */
  public void senseAndDecide(int dt) {
    AbstractEnvironment env = this.getEnv();
    currentPercept = (CarPercept) env.getCurrentPercepts(getAgentId());

    /* decide */
    selectedAction = Optional.empty();
    decide(dt);
  }

  /**
   * Perform the selected action
   */
  public void act() {
      selectedAction.ifPresent(action -> this.getEnv().doAction(super.getAgentId(), action));
  }

  /**
   * Base method to define the behaviour strategy of the car
   *
   * @param dt
   */
  protected abstract void decide(int dt);

  public double getCurrentSpeed() {
    return currentSpeed;
  }

  @Override
  public void run() {
      while (true) {
        stepBarrier.waitBefore(simulation);
        this.step();
      }
  }
  protected void log(String msg) {
    System.out.println("[CAR " + this.getAgentId() + "] " + msg);
  }


  public void step() {
    actBarrier.waitBefore(simulation);
    this.senseAndDecide(getDt());
    actBarrier.waitBefore(simulation);
    this.act();
  }
}

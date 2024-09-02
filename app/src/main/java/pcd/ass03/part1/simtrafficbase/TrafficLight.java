package pcd.ass03.part1.simtrafficbase;

import pcd.ass03.part1.simengineconcur.Barrier;
import pcd.ass03.part1.simengineseq.AbstractSimulation;

/**
 * Class modeling the structure and behaviour of a traffic light
 *  
 */
public class TrafficLight implements Runnable {


	public static enum TrafficLightState {GREEN, YELLOW, RED}
	private TrafficLightState state, initialState;
	private int currentTimeInState;
	private int redDuration, greenDuration, yellowDuration;
	private P2d pos;

	private final Barrier actBarrier;   // Barrier before doing an action.
	private final Barrier stepBarrier;  // Barrier before doing next step.
	private final AbstractSimulation simulation;  // Barrier before doing next step.
	private final int dt;


	public TrafficLight(P2d pos, TrafficLightState initialState, int greenDuration, int yellowDuration, int redDuration,
						Barrier actBarrier, Barrier stepBarrier, AbstractSimulation simulation, int dt) {
		this.redDuration = redDuration;
		this.greenDuration = greenDuration;
		this.yellowDuration = yellowDuration;
		this.pos = pos;
		this.initialState = initialState;
		this.actBarrier = actBarrier;
		this.stepBarrier = stepBarrier;
		this.simulation = simulation;
		this.dt = dt;
	}

	public void init() {
		state = initialState;
		currentTimeInState = 0;
	}

	@Override
	public void run() {
		while(true) {
			stepBarrier.waitBefore(simulation);
			this.semaphoreStep();
		}
	}

	public void semaphoreStep() {
		this.step(this.dt);
		actBarrier.waitBefore(simulation);
		actBarrier.waitBefore(simulation);
	}

	public void step(int dt) {
		switch (state) {
		case TrafficLightState.GREEN: 
			currentTimeInState += dt;
			if (currentTimeInState >= greenDuration) {
				state = TrafficLightState.YELLOW; 
				currentTimeInState = 0;
			}
			break;
		case TrafficLightState.RED: 
			currentTimeInState += dt;
			if (currentTimeInState >= redDuration) {
				state = TrafficLightState.GREEN; 
				currentTimeInState = 0;
			}
			break;
		case TrafficLightState.YELLOW: 
			currentTimeInState += dt;
			if (currentTimeInState >= yellowDuration) {
				state = TrafficLightState.RED; 
				currentTimeInState = 0;
			}
			break;
		default:
			break;
		}
	}
	
	public boolean isGreen() {
		return state.equals(TrafficLightState.GREEN);
	}
	
	public boolean isRed() {
		return state.equals(TrafficLightState.RED);
	}

	public boolean isYellow() {
		return state.equals(TrafficLightState.YELLOW);
	}
	
	public P2d getPos() {
		return pos;
	}
}

package pcd.ass03.part1.simtrafficbase;

public  class CarAgentInfo {

	private final CarAgent car;
	private double pos;
	private final Road road;
	
	public CarAgentInfo(CarAgent car, Road road, double pos) {
		this.car = car;
		this.road = road;
		this.pos = pos;
	}
	
	public double getPos() {
		return pos;
	}
	
	public void updatePos(double pos) {
		this.pos = pos;
	}
	
	public CarAgent getCar() {
		return car;
	}	
	
	public Road getRoad() {
		return road;
	}
}

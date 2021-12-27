package app;

class ShipActionData {
	public double thrust;
	public double heading;
	boolean canFire;
	
	public ShipActionData(double thrust, double heading, boolean canFire) {
		this.thrust = thrust;
		this.heading = heading;
		this.canFire = canFire;
	}
}
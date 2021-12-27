package lib.utils;

public class GeneralDataPackage {
	private Object[] data_set;
	public GeneralDataPackage(Object...args) {
		data_set = new Object[args.length];
		System.arraycopy(args, 0, data_set, 0, data_set.length);
	}
	public Object[] getData() {
		return data_set;
	}
}

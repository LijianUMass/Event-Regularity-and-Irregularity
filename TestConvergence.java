
public class TestConvergence {

	public static void main(String[] args) {
		int p0 = 0;
		double w = 500;
		int c = 300;
		int k = 3;

		int n = 21; //change this
		double[] p = new double[100];
		p[0] = p0;
		System.out.print("i=[");
		for (int i = 0; i < n; i++) System.out.print(i+" ");
		System.out.println("];");
		System.out.print("p=["+p[0]+" ");
	//	System.out.println("p[" + 0 + "] = " + p[0]);
		for (int i = 1; i < n; i++) {
			p[i] = Math.pow((1 - Math.pow((1 - 1 / w), (c / (1 - p[i - 1])))), k);
		//	System.out.println("p[" + i + "] = " + p[i]);
			System.out.print(p[i]+" ");
		}
		System.out.println("];");
	}
}

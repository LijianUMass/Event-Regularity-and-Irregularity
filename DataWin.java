
public class DataWin {
	public int[] letters;
	public DataWin(int[] lets) {
		letters = new int[lets.length];
		for (int i = 0; i < letters.length; i++) {
			letters[i] = lets[i];
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (int i = 0; i < letters.length; i++) {
			result = prime * result + letters[i];
		}
	//	result = prime * result + Arrays.hashCode(letters);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataWin other = (DataWin) obj;
	//	if (!Arrays.equals(letters, other.letters))
	//		return false;
		for (int i = 0; i < letters.length; i++) {
			if (letters[i] != other.letters[i]) return false;
		}
		return true;
	}
	
}

package cutie;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {
	private final NavigableMap<Double, E> map = new TreeMap<>();
	private final Random random;
	private double total = 0;
	
	public RandomCollection() {
		this(new Random());
	}
	
	public RandomCollection(Random rand) {
		this.random = rand;
	}
	
	public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
    
    // Performs .next() n number of times and calculates the frequencies for each group.
    public Map<E, Integer> getNIterations(int n) {
    	Map<E, Integer> m = new HashMap<>();
    	
    	if (n <= 0) throw new IllegalArgumentException("n should be greater than 0");
    	
    	for (int i = 0; i < n; i++) {
    		E k = this.next();
    		if (m.containsKey(k)) {
    			int temp = m.get(k);
    			m.put(k, temp+1);
    		} else {
    			m.put(k, 1);
    		}
    	}
    	return m;
    }
}

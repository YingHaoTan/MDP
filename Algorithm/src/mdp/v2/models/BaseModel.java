package mdp.v2.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseModel is an abstract base class for all mutable model classes
 * 
 * @author Ying Hao
 * @since 21 Feb 2018
 * @version 2.0
 */
public class BaseModel {
	private Map<String, List<Runnable>> listenermap;
	
	/**
	 * Creates an instance of BaseModel
	 */
	public BaseModel() {
		listenermap = new HashMap<String, List<Runnable>>();
	}
	
	/**
	 * Adds a property listener to the model instance
	 * @param property
	 * @param runnable
	 */
	public boolean addPropertyListener(String property, Runnable runnable) {
		List<Runnable> listeners = listenermap.get(property);
		
		return listeners != null && listeners.add(runnable);
	}
	
	/**
	 * Removes a property listener from the model instance
	 * @param property
	 * @param runnable
	 */
	public boolean removePropertyListener(String property, Runnable runnable) {
		List<Runnable> listeners = listenermap.get(property);
		
		return listeners != null && listeners.remove(runnable);
	}
	
	/**
	 * Notifies listeners when a property is changed
	 * @param property
	 * @param runnable
	 * @throws NullPointerException If the specified property is not managed by BaseModel
	 */
	protected void notifyPropertyChanged(String property) {
		for(Runnable runnable: listenermap.get(property))
			runnable.run();
	}
	
	/**
	 * Registers a property to be managed by BaseModel
	 * @param property
	 */
	protected void registerProperty(String property) {
		if(!listenermap.containsKey(property))
			listenermap.put(property, new ArrayList<>());
	}

}

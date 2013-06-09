package eva.twitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//TODO: Write specific parameter getter/setters
public class Parameters {
	
	private Map<ParameterEnum, String> parameters = new HashMap<ParameterEnum, String>();
	
	public Parameters() {
	    
	}
	
	public Parameters(Parameters otherParameters) {
	    
	    addAll(otherParameters);
	}
	
	public String getParameter(ParameterEnum parameter) {
		
		return parameters.get(parameter);
	}
	
	public void setParameter(ParameterEnum parameter, String value) {
		
		parameters.put(parameter, value);
	}
	
	public Set<ParameterEnum> getParameters() {
		
		return parameters.keySet();
	}
	
	public void addAll(Parameters parameters) {
		
		for(ParameterEnum parameterEnum: parameters.getParameters()) {
			
			this.parameters.put(parameterEnum, parameters.getParameter(parameterEnum));
		}
	}
}

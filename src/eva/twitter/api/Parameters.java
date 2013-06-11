package eva.twitter.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//TODO: Write specific parameter getter/setters
public class Parameters {

    private Set<ParameterEnum> oAuthParameters = null;

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

    public Set<ParameterEnum> getOauthParameterKeys() {

        if (oAuthParameters == null) {

            oAuthParameters = new HashSet<ParameterEnum>();

            for (ParameterEnum parameter : getParameters()) {

                if (parameter.isOauthKey()) {

                    oAuthParameters.add(parameter);
                }
            }
        }

        return oAuthParameters;
    }

    public void addAll(Parameters parameters) {

        oAuthParameters = null;

        for (ParameterEnum parameterEnum : parameters.getParameters()) {

            this.parameters.put(parameterEnum, parameters.getParameter(parameterEnum));
        }
    }
}

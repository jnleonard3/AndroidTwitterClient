/*
 * Copyright (c) 2013, Jon Leonard
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */
package eva.twitter.api;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//TODO: Write specific parameter getter/setters
public class Parameters {

    private Set<ParameterEnum> oAuthParameters = null;

    private Map<ParameterEnum, String> parameters = new TreeMap<ParameterEnum, String>();

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

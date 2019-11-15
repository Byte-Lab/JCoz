/*
 * This file is part of JCoz.
 *
 * JCoz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JCoz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JCoz.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package jcoz.service;

import jcoz.agent.JCozProfilingErrorCodes;

/**
 * generates exceptions based on return codes
 * @author matt
 *
 */
public class JCozExceptionFactory {

    private JCozExceptionFactory(){

    }

    public static final JCozExceptionFactory instance = new JCozExceptionFactory();

    public static JCozExceptionFactory getInstance(){
        return instance;
    }

    public JCozException getJCozExceptionFromErrorCode(int errorCode){
        switch(errorCode){
            case JCozProfilingErrorCodes.NO_PROGRESS_POINT_SET:
                return new NoProgressPointSetException();
            case JCozProfilingErrorCodes.NO_SCOPE_SET:
                return new NoScopeSetException();
            case JCozProfilingErrorCodes.CANNOT_CALL_WHEN_RUNNING:
                return new InvalidWhenProfilerRunningException();
            case JCozProfilingErrorCodes.PROFILER_NOT_RUNNING:
                return new InvalidWhenProfilerNotRunningException();
            default:
                return new JCozException("Unknown Exception occurred: "+errorCode);
        }
    }

}

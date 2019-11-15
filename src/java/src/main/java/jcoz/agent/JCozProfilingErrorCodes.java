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
package jcoz.agent;

/**
 * @author matt
 *
 */
public class JCozProfilingErrorCodes {
    /*
     * error return codes
     */
    public static final int NORMAL_RETURN = 0;
    public static final int NO_PROGRESS_POINT_SET = 1;
    public static final int NO_SCOPE_SET = 2;
    public static final int CANNOT_CALL_WHEN_RUNNING = 3;
    public static final int PROFILER_NOT_RUNNING = 4;
    public static final int INVALID_JAVA_PROCESS = 5;
}

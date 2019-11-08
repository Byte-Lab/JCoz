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
package jcoz;

import java.io.Serializable;

/**
 * 
 * simple Bean for transporting VMDescriptor data
 * @author matt
 *
 */
public class JCozVMDescriptor implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -1232246806223243256L;

    private int pid;
    private String displayName;


    /**
     * @param pid
     * @param displayName
     */
    public JCozVMDescriptor(int pid, String displayName) {
        this.pid = pid;
        this.displayName = displayName;
    }

    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }
    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

}

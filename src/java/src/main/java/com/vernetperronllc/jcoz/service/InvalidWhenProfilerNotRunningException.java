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
package com.vernetperronllc.jcoz.service;

/**
 * @author matt
 *
 */
public class InvalidWhenProfilerNotRunningException extends JCozException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5261057789740229989L;

	public InvalidWhenProfilerNotRunningException(){
		super();
	}

	public InvalidWhenProfilerNotRunningException(String message) {
		super(message);
	}
	
	public InvalidWhenProfilerNotRunningException(Throwable cause){
		super(cause);
	}
	
	public InvalidWhenProfilerNotRunningException(String message, Throwable cause){
		super(message, cause);
	}
}

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
 */
package jcoz.service;

public class ProfileException extends JCozException {
	private static final long serialVersionUID = -1481974051271289571L;

	public ProfileException(){
		super();
	}

	public ProfileException(String message) {
		super(message);
	}
	
	public ProfileException(Throwable cause){
		super(cause);
	}
	
	public ProfileException(String message, Throwable cause){
		super(message, cause);
	}
}

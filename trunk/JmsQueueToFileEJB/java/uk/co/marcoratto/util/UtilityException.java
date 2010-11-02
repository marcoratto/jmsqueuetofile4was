/*
 * Copyright (C) 2010 Marco Ratto
 *
 * This file is part of the project JmsQueueToFile.
 *
 * JmsQueueToFile is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * JmsQueueToFile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JmsQueueToFile; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package uk.co.marcoratto.util;

public class UtilityException extends Exception {

	private static final long serialVersionUID = -1435030565009551809L;

	public UtilityException(String message) {
        super(message);
    }

    public UtilityException(String message, Throwable cause) {
        super(message, cause);
     }

    public UtilityException(Throwable cause) {
        super(cause);
    }
}

/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright © 2024 RemasteredArch
 *
 * Licenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Licenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package net.remasteredarch.licenser;

import java.util.ArrayList;

public class Author {
	String canonicalName;
	ArrayList<String> names;

	public Author(String canonicalName, ArrayList<String> names) {
		this.canonicalName = canonicalName;
		this.names = names;
	}

	public Author(String canonicalName, String... names) {
		this.canonicalName = canonicalName;

		this.names = new ArrayList<>();
		for (String name : names) {
			this.names.add(name);
		}
	}

	@Override
	public String toString() {
		return canonicalName + ": " + names;
	}
}

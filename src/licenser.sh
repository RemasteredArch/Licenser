#! /bin/env bash

# SPDX-License-Identifier: GPL-3.0-or-later
# 
# Copyright © 2024 RemasteredArch
# 
# This file is part of Licenser.
# 
# Licenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
# 
# Licenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>.

reset="\e[0m"
bold="\e[1m"
italic="\e[3m"

help() {
  echo -e "${bold}Licenser${reset} v$(version): Recursively apply a license and copyright header to an existing code base for GPL compliance"
  echo -e "$bold\nOptions${reset}:"
  echo -e "  -h | --help:$italic Displays this help message$reset"
  echo -e "  -v | --version:$italic Displays the version of the program$reset"
  echo -e "$italic\nLicenser is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."
  echo -e "\nLicenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details."
  echo -e "\nYou should have received a copy of the GNU Affero General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>.$reset"
  exit

}

version() {
  echo "0.1"
  exit

}

for arg in $@; do
  case $arg in
    -h | --help )
      help
      ;;

    -v | --version )
      version
      ;;

  esac
done

git log --author-date-order --reverse --date=short --pretty=format:"%an %as" $1

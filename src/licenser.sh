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
  echo -e "${bold}Licenser$reset v$(version): Recursively apply a license and copyright header to an existing codebase for GPL compliance"
  echo -e "$bold\nUsage${reset}: ./licenser.sh$italic [path] [options]"
  echo -e "  Use without specifying a path will report all authors within your current directory (recursively) using git log$reset"
  echo -e "$bold\nOptions${reset}:"
  echo -e "  -h | --help:$italic Displays this help message$reset"
  echo -e "  -v | --version:$italic Displays the version of the program$reset"
  echo -e "$italic\nLicenser is work in progress software.$bold Use at your own risk!$reset"
  echo -e "$italic\n\nLicenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."
  echo -e "\nLicenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details."
  echo -e "\nYou should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>.$reset"
  exit

}

version() {
  echo "0.1"
  exit

}

getYear() {
#  for author in $@; do
  echo "$1" | grep --only-matching -E "LICENSER_DATE=[0-9]{4}-[0-9]{2}-[0-9]{2}" | grep --only-matching -E "[0-9]{4}"
#  done

}

getName() {
  echo "$1" | grep --only-matching -P "(?<=LICENSER_AUTHOR=)"
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

log=$(git log --author-date-order --reverse --date=short --pretty=format:"%an LICENSER_SPLIT %as" $1)

echo -e "${bold}input:$italic"
echo "$log"
#echo -e "${bold}authors:$italic"
#authors=$(echo "$log" | awk -F ' LICENSER_SPLIT ' '{print $1}')
#years=$(echo "$log" | awk -F ' LICENSER_SPLIT ' '{print $2}')
both=$(echo "$log" | awk -F ' LICENSER_SPLIT ' '{print $1, $2}')
#for i in $authors; do
#  echo "$i ${years[$i]}"
#done

#for i in $authors; do
#  year=$(getYear $i)
#  if [[ -n $year ]]; then
#    echo "year: $year"
#  else
#    name=$(getName $i)
#    echo "name: $name"
#  fi
#done

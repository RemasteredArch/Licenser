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

split_text=" LICENSER_SPLIT "

get_log() {
  git log --author-date-order --reverse --date=short --pretty=format:"%an${split_text}%as" $1
}

get_year() {
  echo "$1" | grep --only-matching -E '\b[0-9]{4}'
}

simple_array_contains() { # tests if an array contains a given value, but cannot handle spaces, etc.
  pattern=$1              # usage: [[ $(simple_array_contains $pattern_to_check ${array_name[@]})
  shift
  [[ "$@" =~ $pattern ]] && echo "true" || echo "false"
}

log=$(get_log $1)

authors=($(echo "$log" | awk -F "$split_text" '{print $1}'))
dates=($(echo "$log" | awk -F "$split_text" '{print $2}'))
both=($(echo "$log" | awk -F "$split_text" '{print $1, $2}'))

declare -A map
for ((i = 0 ; i < ${#authors[@]} ; i++)); do
  current_author=${authors[$i]}
  current_year=$(get_year ${dates[$i]})
  if [[ $(simple_array_contains $current_year ${map[$current_author]}) == false ]]; then
    map[$current_author]="${map[$current_author]} $current_year"
  fi
done

for i in ${!map[@]}; do # for every person in the map...
  echo "LIST: $i: ${map[$i]}" # list their name and contributed years
done

#for ((i = 0 ; i < ${#authors[@]} ; i++)); do
#  echo "$i, ${authors[i]}, $(getYear ${dates[i]})"
#done


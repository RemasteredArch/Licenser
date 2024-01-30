authors=("JaxyDog" "JaxyDog" "RemasteredArch" "Test-Person-One" "RemasteredArch" "Test-Person-2")
years=("2023" "2024" "2024" "2024" "2024" "2023")
# expected result:
#   JaxyDog: 2023, 2024
#   RemasteredArch: 2024, 2024
#   Test1: 2024
#   Test2: 2023

array_contains() { # finds strings in simple arrays (cannot handle entries with spaces, etc)
  pattern=$1       # usage: [[ $(array_contains pattern_to_check ${array_name[@]}) == "true" ]]
  shift
  [[ "$@" =~ $pattern ]] && echo "true" || echo "false"
}

declare -A map
for ((i = 0 ; i < ${#authors[@]} ; i++)); do
  current_author=${authors[$i]}
  current_year=${years[$i]}
  if [[ $(array_contains $current_year ${map[$current_author]}) == false ]]; then
    map[$current_author]="${map[$current_author]} $current_year"
  fi
done

for i in ${!map[@]}; do # for every person in the map...
  echo "LIST: $i: ${map[$i]}" # list their name and contributed years
done

#[[ -n "${map[$1]}" ]] && echo "$1 is in array"

authors=("JaxyDog" "JaxyDog" "RemasteredArch" "Test-Person-One" "RemasteredArch" "Test-Person-2")
years=("2023" "2024" "2024" "2024" "2024" "2023")
# expected result:
#   JaxyDog: 2023, 2024
#   RemasteredArch: 2024, 2024
#   Test1: 2024
#   Test2: 2023

declare -A map
for ((i = 0 ; i < ${#authors[@]} ; i++)); do
  currentAuthor=${authors[$i]}
  currentYear=${years[$i]}
  if [[ -n map[$currentAuthor] ]]; then
    map[$currentAuthor]="${map[$currentAuthor]} $currentYear"
  fi
done

for i in ${!map[@]}; do
  echo "LIST: $i: ${map[$i]}"
done

#[[ -n "${map[$1]}" ]] && echo "$1 is in array"

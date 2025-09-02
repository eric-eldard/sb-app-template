# cloc is Perl software for counting file lines which can be installed and run from npm.
# https://github.com/AlDanial/cloc
# https://www.npmjs.com/package/cloc

BOLD_WHITE="\e[1;97m"
RESET="\e[0m"

printf "\n\n${BOLD_WHITE}Lines of application code${RESET}\n\n"
npx cloc --not-match-d="(node_modules|out|generated)" \
 maintenance-page \
 scripts \
 sql \
 src/main \
 pom.xml

printf "\n\n${BOLD_WHITE}Lines of application test code${RESET}\n\n"
npx cloc src/test
